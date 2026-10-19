package br.com.flowopsbackend.model;

import br.com.flowopsbackend.model.enums.PrioridadeTarefa;
import br.com.flowopsbackend.model.enums.StatusTarefa;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FluxoTarefaTest {

    @Test
    void deveExecutarFluxoAteAprovacao() {
        TarefaEntity tarefa = new TarefaEntity(null, "Implementar login", "Criar autenticação",
                PrioridadeTarefa.ALTA, LocalDateTime.now().plusDays(2));

        assertThat(tarefa.getStatus()).isEqualTo(StatusTarefa.PENDENTE);
        tarefa.iniciar();
        assertThat(tarefa.getStatus()).isEqualTo(StatusTarefa.EM_ANDAMENTO);
        tarefa.enviarParaAuditoria();
        assertThat(tarefa.getStatus()).isEqualTo(StatusTarefa.AGUARDANDO_AUDITORIA);
        tarefa.aprovar();
        assertThat(tarefa.getStatus()).isEqualTo(StatusTarefa.APROVADA);
    }

    @Test
    void deveRetornarParaCorrecaoEReiniciar() {
        TarefaEntity tarefa = new TarefaEntity(null, "Implementar login", "Criar autenticação",
                PrioridadeTarefa.ALTA, LocalDateTime.now().plusDays(2));

        tarefa.iniciar();
        tarefa.enviarParaAuditoria();
        tarefa.solicitarCorrecao();
        assertThat(tarefa.getStatus()).isEqualTo(StatusTarefa.EM_CORRECAO);
        tarefa.iniciar();
        assertThat(tarefa.getStatus()).isEqualTo(StatusTarefa.EM_ANDAMENTO);
    }
}
