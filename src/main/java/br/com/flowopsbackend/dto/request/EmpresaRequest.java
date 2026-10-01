package br.com.flowopsbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record EmpresaRequest(
        @NotBlank String razaoSocial,
        @NotBlank String nomeFantasia,
        @NotBlank @Pattern(regexp = "\\d{14}") String cnpj,
        @NotNull Boolean flgAtivo
) {
}
