package br.com.flowopsbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
public class EmpresaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razao_social")
    private String razaoSocial;

    @Column(name = "nome_fantasia", nullable = false)
    private String nomeFantasia;

    @Column(name = "cnpj", nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(name = "flg_ativo", nullable = false)
    private boolean flgAtivo;

    public EmpresaEntity(String razaoSocial, String nomeFantasia, String cnpj, boolean flgAtivo) {
        atualizar(razaoSocial, nomeFantasia, cnpj, flgAtivo);
    }

    public void atualizar(String razaoSocial, String nomeFantasia, String cnpj, boolean flgAtivo) {
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.flgAtivo = flgAtivo;
    }
}
