package br.com.flowopsbackend.service;

import br.com.flowopsbackend.dto.request.EmpresaCadastroRequest;
import br.com.flowopsbackend.dto.request.EmpresaRequest;
import br.com.flowopsbackend.dto.response.EmpresaResponse;
import br.com.flowopsbackend.exception.NegocioException;
import br.com.flowopsbackend.mapper.EmpresaMapper;
import br.com.flowopsbackend.model.EmpresaEntity;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.model.VinculoEmpresaEntity;
import br.com.flowopsbackend.model.enums.PapelEmpresa;
import br.com.flowopsbackend.repository.EmpresaRepository;
import br.com.flowopsbackend.repository.UsuarioRepository;
import br.com.flowopsbackend.repository.VinculoEmpresaRepository;
import br.com.flowopsbackend.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;
    private final UsuarioRepository usuarioRepository;
    private final VinculoEmpresaRepository vinculoRepository;
    private final AccessControlService accessControlService;

    @Transactional
    public EmpresaResponse criar(EmpresaCadastroRequest request) {
        UsuarioEntity admin = accessControlService.exigirAdminFlowOps();
        validarCnpjDisponivel(request.cnpj());
        EmpresaEntity empresaEntity = empresaMapper.toEntity(request);
        EmpresaEntity empresaSalva = empresaRepository.save(empresaEntity);
        UsuarioEntity gestor = usuarioRepository.findById(request.gestorInicialId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Gestor inicial não encontrado"));
        VinculoEmpresaEntity vinculo = vinculoRepository.findByUsuarioIdAndEmpresaId(gestor.getId(), empresaSalva.getId())
                .orElseGet(() -> new VinculoEmpresaEntity(gestor, empresaSalva));
        vinculo.aprovar(request.cargoGestor(), PapelEmpresa.GESTOR, admin);
        vinculoRepository.save(vinculo);
        return empresaMapper.toResponse(empresaSalva);
    }

    public List<EmpresaResponse> listar() {
        return empresaRepository.findByFlgAtivoTrueOrderByNomeFantasia()
                .stream()
                .map(empresaMapper::toResponse)
                .toList();
    }

    public EmpresaResponse buscarPorId(Long id) {
        return empresaMapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public EmpresaResponse atualizar(Long id, EmpresaRequest request) {
        accessControlService.exigirAdminFlowOps();
        EmpresaEntity empresaEntity = buscarEntidade(id);
        validarCnpjDisponivel(request.cnpj(), id);
        empresaMapper.updateEntity(empresaEntity, request);
        return empresaMapper.toResponse(empresaEntity);
    }

    @Transactional
    public void excluir(Long id) {
        accessControlService.exigirAdminFlowOps();
        EmpresaEntity empresaEntity = buscarEntidade(id);
        empresaEntity.desativar();
    }

    private EmpresaEntity buscarEntidade(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
    }

    private void validarCnpjDisponivel(String cnpj) {
        if (empresaRepository.existsByCnpj(cnpj)) {
            throw cnpjDuplicado();
        }
    }

    private void validarCnpjDisponivel(String cnpj, Long id) {
        if (empresaRepository.existsByCnpjAndIdNot(cnpj, id)) {
            throw cnpjDuplicado();
        }
    }

    private NegocioException cnpjDuplicado() {
        return new NegocioException(HttpStatus.CONFLICT, "CNPJ já cadastrado");
    }
}
