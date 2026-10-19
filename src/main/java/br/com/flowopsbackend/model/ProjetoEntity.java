package br.com.flowopsbackend.model;

import br.com.flowopsbackend.model.enums.StatusProjeto;
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

import java.time.LocalDate;

@Entity
@Table(name = "projetos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjetoEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gestor_responsavel_id", nullable = false)
    private UsuarioEntity gestorResponsavel;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, length = 2000)
    private String descricao;

    @Column(name = "data_inicio_prevista", nullable = false)
    private LocalDate dataInicioPrevista;

    @Column(name = "data_fim_prevista", nullable = false)
    private LocalDate dataFimPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusProjeto status;

    public ProjetoEntity(EmpresaEntity empresa, UsuarioEntity gestorResponsavel, String nome, String descricao,
                         LocalDate dataInicioPrevista, LocalDate dataFimPrevista) {
        this.empresa = empresa;
        this.gestorResponsavel = gestorResponsavel;
        this.nome = nome;
        this.descricao = descricao;
        this.dataInicioPrevista = dataInicioPrevista;
        this.dataFimPrevista = dataFimPrevista;
        this.status = StatusProjeto.PLANEJAMENTO;
    }

    public void atualizar(UsuarioEntity gestorResponsavel, String nome, String descricao,
                          LocalDate dataInicioPrevista, LocalDate dataFimPrevista) {
        this.gestorResponsavel = gestorResponsavel;
        this.nome = nome;
        this.descricao = descricao;
        this.dataInicioPrevista = dataInicioPrevista;
        this.dataFimPrevista = dataFimPrevista;
    }

    public void alterarStatus(StatusProjeto status) {
        this.status = status;
    }
}
