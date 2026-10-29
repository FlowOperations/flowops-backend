package br.com.flowopsbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record EmpresaCadastroRequest(
        @NotBlank(message = "Informe a razão social.") String razaoSocial,
        @NotBlank(message = "Informe o nome fantasia.") String nomeFantasia,
        @NotBlank(message = "Informe o CNPJ.")
        @Pattern(regexp = "\\d{14}", message = "O CNPJ deve conter exatamente 14 dígitos.")
        String cnpj,
        @NotNull(message = "Informe se a empresa está ativa.") Boolean flgAtivo,
        @NotNull(message = "Selecione o gestor inicial.") Long gestorInicialId,
        @NotBlank(message = "Informe o cargo do gestor.") String cargoGestor
) {
}
