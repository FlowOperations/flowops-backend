package br.com.flowopsbackend.service;

import br.com.flowopsbackend.dto.request.AtribuirResponsavelRequest;
import br.com.flowopsbackend.dto.request.TarefaRequest;
import br.com.flowopsbackend.dto.response.TarefaResponse;
import br.com.flowopsbackend.exception.NegocioException;
import br.com.flowopsbackend.mapper.TarefaMapper;
import br.com.flowopsbackend.model.ProjetoEntity;
import br.com.flowopsbackend.model.TarefaEntity;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.model.enums.StatusTarefa;
import br.com.flowopsbackend.model.enums.PapelEmpresa;
import br.com.flowopsbackend.model.enums.StatusVinculo;
import br.com.flowopsbackend.repository.ProjetoMembroRepository;
import br.com.flowopsbackend.repository.TarefaRepository;
import br.com.flowopsbackend.repository.UsuarioRepository;
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
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoMembroRepository membroRepository;
    private final ProjetoService projetoService;
    private final TarefaMapper mapper;
    private final AccessControlService accessControlService;
    private final VinculoEmpresaRepository vinculoRepository;
    private final EmailService emailService;

    @Transactional
    public TarefaResponse criar(TarefaRequest request) {
        ProjetoEntity projeto = projetoService.buscarEntidade(request.projetoId());
        accessControlService.exigirGestaoProjeto(projeto);
        TarefaEntity tarefa = new TarefaEntity(projeto, request.titulo(), request.descricao(),
                request.prioridade(), request.prazo());
        if (request.responsavelId() != null) {
            tarefa.atribuirResponsavel(buscarResponsavel(request.responsavelId(), projeto));
        }
        return mapper.toResponse(tarefaRepository.save(tarefa));
    }

    public List<TarefaResponse> listar(Long projetoId) {
        ProjetoEntity projeto = projetoService.buscarEntidade(projetoId);
        accessControlService.exigirAcessoProjeto(projeto);
        return tarefaRepository.findByProjetoIdOrderByCriadoEmDesc(projetoId).stream().map(mapper::toResponse).toList();
    }

    public TarefaResponse buscar(Long id) {
        TarefaEntity tarefa = buscarEntidade(id);
        accessControlService.exigirAcessoProjeto(tarefa.getProjeto());
        return mapper.toResponse(tarefa);
    }

    @Transactional
    public TarefaResponse atualizar(Long id, TarefaRequest request) {
        TarefaEntity tarefa = buscarEntidade(id);
        accessControlService.exigirGestaoProjeto(tarefa.getProjeto());
        if (!tarefa.getProjeto().getId().equals(request.projetoId())) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "O projeto da tarefa não pode ser alterado");
        }
        exigirStatusEditavel(tarefa);
        tarefa.atualizar(request.titulo(), request.descricao(), request.prioridade(), request.prazo());
        if (request.responsavelId() != null) {
            tarefa.atribuirResponsavel(buscarResponsavel(request.responsavelId(), tarefa.getProjeto()));
        }
        return mapper.toResponse(tarefa);
    }

    @Transactional
    public TarefaResponse atribuir(Long id, AtribuirResponsavelRequest request) {
        TarefaEntity tarefa = buscarEntidade(id);
        accessControlService.exigirGestaoProjeto(tarefa.getProjeto());
        exigirStatusEditavel(tarefa);
        tarefa.atribuirResponsavel(buscarResponsavel(request.usuarioId(), tarefa.getProjeto()));
        return mapper.toResponse(tarefa);
    }

    @Transactional
    public TarefaResponse iniciar(Long id) {
        TarefaEntity tarefa = buscarEntidade(id);
        accessControlService.exigirResponsavel(tarefa);
        if (tarefa.getStatus() != StatusTarefa.PENDENTE && tarefa.getStatus() != StatusTarefa.EM_CORRECAO) {
            throw transicaoInvalida();
        }
        tarefa.iniciar();
        return mapper.toResponse(tarefa);
    }

    @Transactional
    public TarefaResponse enviarParaAuditoria(Long id) {
        TarefaEntity tarefa = buscarEntidade(id);
        accessControlService.exigirResponsavel(tarefa);
        if (tarefa.getStatus() != StatusTarefa.EM_ANDAMENTO) {
            throw transicaoInvalida();
        }
        tarefa.enviarParaAuditoria();
        vinculoRepository.findByEmpresaIdAndPapelInAndStatusAndAtivoTrue(
                        tarefa.getProjeto().getEmpresa().getId(),
                        Set.of(PapelEmpresa.GESTOR, PapelEmpresa.COORDENADOR), StatusVinculo.APROVADO)
                .stream()
                .filter(vinculo -> vinculo.getPapel() == PapelEmpresa.GESTOR
                        || membroRepository.existsByProjetoIdAndUsuarioIdAndAtivoTrue(
                        tarefa.getProjeto().getId(), vinculo.getUsuario().getId()))
                .forEach(vinculo -> emailService.enviar(vinculo.getUsuario().getEmail(),
                        "Tarefa aguardando auditoria", tarefa.getTitulo() + " foi enviada para auditoria."));
        return mapper.toResponse(tarefa);
    }

    public TarefaEntity buscarEntidade(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Tarefa não encontrada"));
    }

    private UsuarioEntity buscarResponsavel(Long usuarioId, ProjetoEntity projeto) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Responsável não encontrado"));
        if (!membroRepository.existsByProjetoIdAndUsuarioIdAndAtivoTrue(projeto.getId(), usuarioId)) {
            throw new NegocioException(HttpStatus.BAD_REQUEST, "Responsável deve possuir acesso ativo ao projeto");
        }
        return usuario;
    }

    private void exigirStatusEditavel(TarefaEntity tarefa) {
        if (tarefa.getStatus() == StatusTarefa.AGUARDANDO_AUDITORIA || tarefa.getStatus() == StatusTarefa.APROVADA) {
            throw new NegocioException(HttpStatus.CONFLICT, "Tarefa não pode ser alterada no status atual");
        }
    }

    private NegocioException transicaoInvalida() {
        return new NegocioException(HttpStatus.CONFLICT, "Transição de status inválida");
    }
}
