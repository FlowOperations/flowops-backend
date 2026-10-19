package br.com.flowopsbackend.service;

import br.com.flowopsbackend.dto.request.ProjetoMembroRequest;
import br.com.flowopsbackend.dto.request.ProjetoRequest;
import br.com.flowopsbackend.dto.request.ProjetoStatusRequest;
import br.com.flowopsbackend.dto.response.ProjetoMembroResponse;
import br.com.flowopsbackend.dto.response.ProjetoResponse;
import br.com.flowopsbackend.exception.NegocioException;
import br.com.flowopsbackend.mapper.ProjetoMapper;
import br.com.flowopsbackend.model.EmpresaEntity;
import br.com.flowopsbackend.model.ProjetoEntity;
import br.com.flowopsbackend.model.ProjetoMembroEntity;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.model.enums.PapelEmpresa;
import br.com.flowopsbackend.model.enums.StatusTarefa;
import br.com.flowopsbackend.model.enums.StatusVinculo;
import br.com.flowopsbackend.repository.EmpresaRepository;
import br.com.flowopsbackend.repository.ProjetoMembroRepository;
import br.com.flowopsbackend.repository.ProjetoRepository;
import br.com.flowopsbackend.repository.TarefaRepository;
import br.com.flowopsbackend.repository.UsuarioRepository;
import br.com.flowopsbackend.repository.VinculoEmpresaRepository;
import br.com.flowopsbackend.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final ProjetoMembroRepository membroRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VinculoEmpresaRepository vinculoRepository;
    private final TarefaRepository tarefaRepository;
    private final ProjetoMapper mapper;
    private final AccessControlService accessControlService;

    @Transactional
    public ProjetoResponse criar(ProjetoRequest request) {
        validarDatas(request);
        UsuarioEntity criador = accessControlService.exigirGestaoEmpresa(request.empresaId());
        EmpresaEntity empresa = empresaRepository.findById(request.empresaId())
                .filter(EmpresaEntity::isFlgAtivo)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
        UsuarioEntity gestor = buscarGestorValido(request.gestorResponsavelId(), empresa.getId());
        ProjetoEntity projeto = projetoRepository.save(new ProjetoEntity(empresa, gestor, request.nome(),
                request.descricao(), request.dataInicioPrevista(), request.dataFimPrevista()));
        concederAcessoInterno(projeto, gestor, criador);
        concederAcessoInterno(projeto, criador, criador);
        return resposta(projeto);
    }

    public List<ProjetoResponse> listar(Long empresaId) {
        UsuarioEntity usuario = accessControlService.getUsuarioAtual();
        List<ProjetoEntity> projetos = projetoRepository.findByEmpresaIdOrderByCriadoEmDesc(empresaId);
        LinkedHashMap<Long, ProjetoEntity> permitidos = new LinkedHashMap<>();
        projetos.stream().filter(projeto -> temAcesso(usuario, projeto)).forEach(p -> permitidos.put(p.getId(), p));
        return permitidos.values().stream().map(this::resposta).toList();
    }

    public ProjetoResponse buscarPorId(Long id) {
        ProjetoEntity projeto = buscarEntidade(id);
        accessControlService.exigirAcessoProjeto(projeto);
        return resposta(projeto);
    }

    @Transactional
    public ProjetoResponse atualizar(Long id, ProjetoRequest request) {
        validarDatas(request);
        ProjetoEntity projeto = buscarEntidade(id);
        accessControlService.exigirGestaoProjeto(projeto);
        if (!projeto.getEmpresa().getId().equals(request.empresaId())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "A empresa do projeto não pode ser alterada");
        }
        UsuarioEntity gestor = buscarGestorValido(request.gestorResponsavelId(), projeto.getEmpresa().getId());
        projeto.atualizar(gestor, request.nome(), request.descricao(), request.dataInicioPrevista(), request.dataFimPrevista());
        concederAcessoInterno(projeto, gestor, accessControlService.getUsuarioAtual());
        return resposta(projeto);
    }

    @Transactional
    public ProjetoResponse alterarStatus(Long id, ProjetoStatusRequest request) {
        ProjetoEntity projeto = buscarEntidade(id);
        accessControlService.exigirGestaoProjeto(projeto);
        projeto.alterarStatus(request.status());
        return resposta(projeto);
    }

    public List<ProjetoMembroResponse> listarMembros(Long id) {
        ProjetoEntity projeto = buscarEntidade(id);
        accessControlService.exigirAcessoProjeto(projeto);
        return membroRepository.findByProjetoIdAndAtivoTrue(id).stream().map(mapper::toMembroResponse).toList();
    }

    @Transactional
    public ProjetoMembroResponse adicionarMembro(Long id, ProjetoMembroRequest request) {
        ProjetoEntity projeto = buscarEntidade(id);
        UsuarioEntity concedidoPor = accessControlService.exigirGestaoProjeto(projeto);
        UsuarioEntity usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        validarVinculoAtivo(usuario.getId(), projeto.getEmpresa().getId());
        return mapper.toMembroResponse(concederAcessoInterno(projeto, usuario, concedidoPor));
    }

    @Transactional
    public void removerMembro(Long id, Long usuarioId) {
        ProjetoEntity projeto = buscarEntidade(id);
        accessControlService.exigirGestaoProjeto(projeto);
        if (projeto.getGestorResponsavel().getId().equals(usuarioId)) {
            throw new NegocioException(HttpStatus.CONFLICT, "Gestor responsável não pode ser removido do projeto");
        }
        ProjetoMembroEntity membro = membroRepository.findByProjetoIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Membro não encontrado"));
        membro.remover();
    }

    public ProjetoEntity buscarEntidade(Long id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
    }

    public BigDecimal calcularProgresso(Long projetoId) {
        long total = tarefaRepository.countByProjetoId(projetoId);
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        long aprovadas = tarefaRepository.countByProjetoIdAndStatus(projetoId, StatusTarefa.APROVADA);
        return BigDecimal.valueOf(aprovadas).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private ProjetoMembroEntity concederAcessoInterno(ProjetoEntity projeto, UsuarioEntity usuario, UsuarioEntity por) {
        ProjetoMembroEntity membro = membroRepository.findByProjetoIdAndUsuarioId(projeto.getId(), usuario.getId())
                .map(existente -> {
                    existente.ativar();
                    return existente;
                }).orElseGet(() -> new ProjetoMembroEntity(projeto, usuario, por));
        return membroRepository.save(membro);
    }

    private UsuarioEntity buscarGestorValido(Long usuarioId, Long empresaId) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Gestor responsável não encontrado"));
        boolean valido = vinculoRepository.existsByUsuarioIdAndEmpresaIdAndPapelInAndStatusAndAtivoTrue(
                usuarioId, empresaId, Set.of(PapelEmpresa.GESTOR, PapelEmpresa.COORDENADOR), StatusVinculo.APROVADO);
        if (!valido) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "Responsável deve ser gestor ou coordenador ativo da empresa");
        }
        return usuario;
    }

    private void validarVinculoAtivo(Long usuarioId, Long empresaId) {
        if (vinculoRepository.findByUsuarioIdAndEmpresaIdAndStatusAndAtivoTrue(
                usuarioId, empresaId, StatusVinculo.APROVADO).isEmpty()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "Usuário não possui vínculo ativo com a empresa");
        }
    }

    private boolean temAcesso(UsuarioEntity usuario, ProjetoEntity projeto) {
        return projeto.getGestorResponsavel().getId().equals(usuario.getId())
                || membroRepository.existsByProjetoIdAndUsuarioIdAndAtivoTrue(projeto.getId(), usuario.getId())
                || vinculoRepository.existsByUsuarioIdAndEmpresaIdAndPapelInAndStatusAndAtivoTrue(
                usuario.getId(), projeto.getEmpresa().getId(), Set.of(PapelEmpresa.GESTOR), StatusVinculo.APROVADO);
    }

    private void validarDatas(ProjetoRequest request) {
        if (request.dataFimPrevista().isBefore(request.dataInicioPrevista())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "Data final deve ser igual ou posterior à data inicial");
        }
    }

    private ProjetoResponse resposta(ProjetoEntity projeto) {
        return mapper.toResponse(projeto, calcularProgresso(projeto.getId()));
    }
}
