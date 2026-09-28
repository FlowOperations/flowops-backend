package br.com.flowopsbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "empresas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String razaoSocial;

    @Column(nullable = false)
    private String nomeFantasia;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(nullable = false)
    private boolean ativa;

    public Empresa(String razaoSocial, String nomeFantasia, String cnpj, boolean ativa) {
        atualizar(razaoSocial, nomeFantasia, cnpj, ativa);
    }

    public void atualizar(String razaoSocial, String nomeFantasia, String cnpj, boolean ativa) {
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.ativa = ativa;
    }
}
