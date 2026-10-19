package br.com.flowopsbackend.model;

import br.com.flowopsbackend.model.enums.PapelEmpresa;
import br.com.flowopsbackend.model.enums.StatusVinculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "vinculos_empresa", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "empresa_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VinculoEmpresaEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @Column(length = 100)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PapelEmpresa papel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusVinculo status;

    @Column(name = "solicitado_em", nullable = false)
    private Instant solicitadoEm;

    @Column(name = "analisado_em")
    private Instant analisadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analisado_por_id")
    private UsuarioEntity analisadoPor;

    @Column(nullable = false)
    private boolean ativo;

    public VinculoEmpresaEntity(UsuarioEntity usuario, EmpresaEntity empresa) {
        this.usuario = usuario;
        this.empresa = empresa;
        solicitarNovamente();
    }

    public void solicitarNovamente() {
        status = StatusVinculo.SOLICITADO;
        solicitadoEm = Instant.now();
        analisadoEm = null;
        analisadoPor = null;
        ativo = false;
    }

    public void aprovar(String cargo, PapelEmpresa papel, UsuarioEntity analisadoPor) {
        this.cargo = cargo;
        this.papel = papel;
        this.analisadoPor = analisadoPor;
        status = StatusVinculo.APROVADO;
        analisadoEm = Instant.now();
        ativo = true;
    }

    public void rejeitar(UsuarioEntity analisadoPor) {
        this.analisadoPor = analisadoPor;
        status = StatusVinculo.REJEITADO;
        analisadoEm = Instant.now();
        ativo = false;
    }

    public void atualizarAcesso(String cargo, PapelEmpresa papel, boolean ativo) {
        this.cargo = cargo;
        this.papel = papel;
        this.ativo = ativo;
    }
}
