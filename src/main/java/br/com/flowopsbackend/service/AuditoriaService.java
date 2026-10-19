package br.com.flowopsbackend.service;

import br.com.flowopsbackend.dto.request.CorrecaoRequest;
import br.com.flowopsbackend.dto.response.AuditoriaResponse;
import br.com.flowopsbackend.dto.response.TarefaResponse;
import br.com.flowopsbackend.exception.NegocioException;
import br.com.flowopsbackend.mapper.AuditoriaMapper;
import br.com.flowopsbackend.mapper.TarefaMapper;
import br.com.flowopsbackend.model.AuditoriaEntity;
import br.com.flowopsbackend.model.TarefaEntity;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.model.enums.ResultadoAuditoria;
import br.com.flowopsbackend.model.enums.StatusTarefa;
import br.com.flowopsbackend.repository.AuditoriaRepository;
import br.com.flowopsbackend.repository.TarefaRepository;
import br.com.flowopsbackend.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final TarefaRepository tarefaRepository;
    private final TarefaService tarefaService;
    private final AuditoriaMapper mapper;
    private final TarefaMapper tarefaMapper;
    private final AccessControlService accessControlService;
    private final EmailService emailService;

    public List<TarefaResponse> listarPendentes() {
        UsuarioEntity usuario = accessControlService.getUsuarioAtual();
        return tarefaRepository.findByStatusOrderByAtualizadoEmAsc(StatusTarefa.AGUARDANDO_AUDITORIA).stream()
                .filter(tarefa -> accessControlService.possuiGestaoProjeto(usuario, tarefa.getProjeto()))
                .filter(tarefa -> tarefa.getResponsavel() == null || !tarefa.getResponsavel().getId().equals(usuario.getId()))
                .map(tarefaMapper::toResponse).toList();
    }

    public List<AuditoriaResponse> listarHistorico(Long tarefaId) {
        TarefaEntity tarefa = tarefaService.buscarEntidade(tarefaId);
        accessControlService.exigirAcessoProjeto(tarefa.getProjeto());
        return auditoriaRepository.findByTarefaIdOrderByAuditadaEmDesc(tarefaId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public AuditoriaResponse aprovar(Long tarefaId) {
        TarefaEntity tarefa = tarefaService.buscarEntidade(tarefaId);
        UsuarioEntity auditor = validarAuditoria(tarefa);
        tarefa.aprovar();
        AuditoriaEntity auditoria = auditoriaRepository.save(
                new AuditoriaEntity(tarefa, auditor, ResultadoAuditoria.APROVADA, null));
        notificarResponsavel(tarefa, "Sua tarefa foi aprovada.");
        return mapper.toResponse(auditoria);
    }

    @Transactional
    public AuditoriaResponse solicitarCorrecao(Long tarefaId, CorrecaoRequest request) {
        TarefaEntity tarefa = tarefaService.buscarEntidade(tarefaId);
        UsuarioEntity auditor = validarAuditoria(tarefa);
        tarefa.solicitarCorrecao();
        AuditoriaEntity auditoria = auditoriaRepository.save(new AuditoriaEntity(
                tarefa, auditor, ResultadoAuditoria.CORRECAO_SOLICITADA, request.motivo()));
        notificarResponsavel(tarefa, "Foi solicitada uma correção: " + request.motivo());
        return mapper.toResponse(auditoria);
    }

    private UsuarioEntity validarAuditoria(TarefaEntity tarefa) {
        if (tarefa.getStatus() != StatusTarefa.AGUARDANDO_AUDITORIA) {
            throw new NegocioException(HttpStatus.CONFLICT, "Tarefa não está aguardando auditoria");
        }
        return accessControlService.exigirAuditor(tarefa);
    }

    private void notificarResponsavel(TarefaEntity tarefa, String mensagem) {
        if (tarefa.getResponsavel() != null) {
            emailService.enviar(tarefa.getResponsavel().getEmail(), "Auditoria da tarefa " + tarefa.getTitulo(), mensagem);
        }
    }
}
