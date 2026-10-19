package br.com.flowopsbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank @Pattern(regexp = "\\d{11}") String cpf,
        @NotBlank String senha
) {
}
