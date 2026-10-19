package br.com.flowopsbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "empresas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmpresaEntity extends BaseEntity {

    @Column(name = "razao_social", nullable = false)
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

    public void ativar() {
        flgAtivo = true;
    }

    public void desativar() {
        flgAtivo = false;
    }
}
