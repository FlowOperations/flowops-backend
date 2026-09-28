package br.com.flowopsbackend.dto.response;

import br.com.flowopsbackend.model.Empresa;

public record EmpresaResponse(
        Long id,
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        boolean ativa
) {

    public static EmpresaResponse from(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getRazaoSocial(),
                empresa.getNomeFantasia(),
                empresa.getCnpj(),
                empresa.isAtiva()
        );
    }
}
