package br.com.flowopsbackend.dto.response;

public record EmpresaResponse(
        Long id,
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        boolean flgAtivo
) {
}
