package br.com.flowopsbackend.model;

import br.com.flowopsbackend.model.enums.ResultadoAuditoria;
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

import java.time.Instant;

@Entity
@Table(name = "auditorias")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditoriaEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarefa_id", nullable = false)
    private TarefaEntity tarefa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditor_id", nullable = false)
    private UsuarioEntity auditor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResultadoAuditoria resultado;

    @Column(name = "motivo_correcao", length = 2000)
    private String motivoCorrecao;

    @Column(name = "auditada_em", nullable = false)
    private Instant auditadaEm;

    public AuditoriaEntity(TarefaEntity tarefa, UsuarioEntity auditor, ResultadoAuditoria resultado,
                           String motivoCorrecao) {
        this.tarefa = tarefa;
        this.auditor = auditor;
        this.resultado = resultado;
        this.motivoCorrecao = motivoCorrecao;
        this.auditadaEm = Instant.now();
    }
}
