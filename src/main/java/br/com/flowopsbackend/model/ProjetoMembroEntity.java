package br.com.flowopsbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "projetos_membros", uniqueConstraints = @UniqueConstraint(columnNames = {"projeto_id", "usuario_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjetoMembroEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false)
    private ProjetoEntity projeto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "concedido_em", nullable = false)
    private Instant concedidoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concedido_por_id", nullable = false)
    private UsuarioEntity concedidoPor;

    public ProjetoMembroEntity(ProjetoEntity projeto, UsuarioEntity usuario, UsuarioEntity concedidoPor) {
        this.projeto = projeto;
        this.usuario = usuario;
        this.concedidoPor = concedidoPor;
        ativar();
    }

    public void ativar() {
        ativo = true;
        concedidoEm = Instant.now();
    }

    public void remover() {
        ativo = false;
    }
}
