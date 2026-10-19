package br.com.flowopsbackend.model;

import br.com.flowopsbackend.model.enums.PerfilSistema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsuarioEntity extends BaseEntity {

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_sistema", nullable = false, length = 30)
    private PerfilSistema perfilSistema;

    @Column(nullable = false)
    private boolean ativo;

    public UsuarioEntity(String nomeCompleto, String cpf, String email, String senhaHash,
                         LocalDate dataNascimento, PerfilSistema perfilSistema) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.email = email;
        this.senhaHash = senhaHash;
        this.dataNascimento = dataNascimento;
        this.perfilSistema = perfilSistema;
        this.ativo = true;
    }

    public void atualizarDados(String nomeCompleto, String email, LocalDate dataNascimento) {
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.dataNascimento = dataNascimento;
    }

    public void alterarSenha(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public void definirPerfilSistema(PerfilSistema perfilSistema) {
        this.perfilSistema = perfilSistema;
    }

    public void ativar() {
        ativo = true;
    }

    public void desativar() {
        ativo = false;
    }
}
