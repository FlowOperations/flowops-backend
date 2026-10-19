package br.com.flowopsbackend.model;

import br.com.flowopsbackend.model.enums.PrioridadeTarefa;
import br.com.flowopsbackend.model.enums.StatusTarefa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tarefas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TarefaEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false)
    private ProjetoEntity projeto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private UsuarioEntity responsavel;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 3000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadeTarefa prioridade;

    @Column(nullable = false)
    private LocalDateTime prazo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusTarefa status;

    public TarefaEntity(ProjetoEntity projeto, String titulo, String descricao,
                        PrioridadeTarefa prioridade, LocalDateTime prazo) {
        this.projeto = projeto;
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.prazo = prazo;
        status = StatusTarefa.PENDENTE;
    }

    public void atualizar(String titulo, String descricao, PrioridadeTarefa prioridade, LocalDateTime prazo) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.prazo = prazo;
    }

    public void atribuirResponsavel(UsuarioEntity responsavel) {
        this.responsavel = responsavel;
    }

    public void iniciar() {
        status = StatusTarefa.EM_ANDAMENTO;
    }

    public void enviarParaAuditoria() {
        status = StatusTarefa.AGUARDANDO_AUDITORIA;
    }

    public void aprovar() {
        status = StatusTarefa.APROVADA;
    }

    public void solicitarCorrecao() {
        status = StatusTarefa.EM_CORRECAO;
    }

    public void reabrir() {
        status = StatusTarefa.EM_ANDAMENTO;
    }
}
