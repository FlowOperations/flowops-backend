package br.com.flowopsbackend.service;

import br.com.flowopsbackend.dto.request.AnalisarVinculoRequest;
import br.com.flowopsbackend.dto.request.AtualizarVinculoRequest;
import br.com.flowopsbackend.dto.request.SolicitarVinculoRequest;
import br.com.flowopsbackend.dto.response.VinculoEmpresaResponse;
import br.com.flowopsbackend.exception.NegocioException;
import br.com.flowopsbackend.mapper.VinculoEmpresaMapper;
import br.com.flowopsbackend.model.EmpresaEntity;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.model.VinculoEmpresaEntity;
import br.com.flowopsbackend.model.enums.StatusVinculo;
import br.com.flowopsbackend.model.enums.PapelEmpresa;
import br.com.flowopsbackend.repository.EmpresaRepository;
import br.com.flowopsbackend.repository.VinculoEmpresaRepository;
import br.com.flowopsbackend.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VinculoEmpresaService {

    private final VinculoEmpresaRepository vinculoRepository;
    private final EmpresaRepository empresaRepository;
    private final VinculoEmpresaMapper mapper;
    private final AccessControlService accessControlService;
    private final EmailService emailService;

    @Transactional
    public VinculoEmpresaResponse solicitar(SolicitarVinculoRequest request) {
        UsuarioEntity usuario = accessControlService.getUsuarioAtual();
        EmpresaEntity empresa = empresaRepository.findById(request.empresaId())
                .filter(EmpresaEntity::isFlgAtivo)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
        VinculoEmpresaEntity vinculo = vinculoRepository.findByUsuarioIdAndEmpresaId(usuario.getId(), empresa.getId())
                .map(existente -> {
                    if (existente.getStatus() != StatusVinculo.REJEITADO) {
                        throw new NegocioException(HttpStatus.CONFLICT, "Já existe uma solicitação ou vínculo com a empresa");
                    }
                    existente.solicitarNovamente();
                    return existente;
                })
                .orElseGet(() -> new VinculoEmpresaEntity(usuario, empresa));
        VinculoEmpresaEntity salvo = vinculoRepository.save(vinculo);
        vinculoRepository.findByEmpresaIdAndPapelInAndStatusAndAtivoTrue(
                        empresa.getId(), Set.of(PapelEmpresa.GESTOR), StatusVinculo.APROVADO)
                .forEach(gestor -> emailService.enviar(gestor.getUsuario().getEmail(),
                        "Nova solicitação de acesso", usuario.getNomeCompleto() + " solicitou acesso à empresa."));
        return mapper.toResponse(salvo);
    }

    public List<VinculoEmpresaResponse> listarMeus() {
        UsuarioEntity usuario = accessControlService.getUsuarioAtual();
        return vinculoRepository.findByUsuarioIdAndStatusAndAtivoTrue(usuario.getId(), StatusVinculo.APROVADO)
                .stream().map(mapper::toResponse).toList();
    }

    public List<VinculoEmpresaResponse> listarPendentes(Long empresaId) {
        accessControlService.exigirGestorEmpresa(empresaId);
        return vinculoRepository.findByEmpresaIdAndStatusOrderBySolicitadoEmAsc(empresaId, StatusVinculo.SOLICITADO)
                .stream().map(mapper::toResponse).toList();
    }

    public List<VinculoEmpresaResponse> listarColaboradores(Long empresaId) {
        accessControlService.exigirGestaoEmpresa(empresaId);
        return vinculoRepository.findByEmpresaIdAndStatusAndAtivoTrueOrderByUsuarioNomeCompleto(
                        empresaId, StatusVinculo.APROVADO)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public VinculoEmpresaResponse analisar(Long id, AnalisarVinculoRequest request) {
        VinculoEmpresaEntity vinculo = buscar(id);
        UsuarioEntity gestor = accessControlService.exigirGestorEmpresa(vinculo.getEmpresa().getId());
        if (vinculo.getStatus() != StatusVinculo.SOLICITADO) {
            throw new NegocioException(HttpStatus.CONFLICT, "Solicitação já analisada");
        }
        if (request.aprovado()) {
            if (request.cargo() == null || request.cargo().isBlank() || request.papel() == null) {
                throw new NegocioException(HttpStatus.BAD_REQUEST, "Cargo e papel são obrigatórios para aprovação");
            }
            vinculo.aprovar(request.cargo(), request.papel(), gestor);
        } else {
            vinculo.rejeitar(gestor);
        }
        emailService.enviar(vinculo.getUsuario().getEmail(), "Solicitação de acesso à FlowOps",
                request.aprovado() ? "Seu acesso foi aprovado." : "Seu acesso não foi aprovado.");
        return mapper.toResponse(vinculo);
    }

    @Transactional
    public VinculoEmpresaResponse atualizar(Long id, AtualizarVinculoRequest request) {
        VinculoEmpresaEntity vinculo = buscar(id);
        accessControlService.exigirGestorEmpresa(vinculo.getEmpresa().getId());
        if (vinculo.getStatus() != StatusVinculo.APROVADO) {
            throw new NegocioException(HttpStatus.CONFLICT, "Somente vínculos aprovados podem ser atualizados");
        }
        vinculo.atualizarAcesso(request.cargo(), request.papel(), request.ativo());
        return mapper.toResponse(vinculo);
    }

    private VinculoEmpresaEntity buscar(Long id) {
        return vinculoRepository.findById(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Vínculo não encontrado"));
    }
}
