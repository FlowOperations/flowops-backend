package br.com.flowopsbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjetoRequest(
        @NotNull Long empresaId,
        @NotNull Long gestorResponsavelId,
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Size(max = 2000) String descricao,
        @NotNull LocalDate dataInicioPrevista,
        @NotNull LocalDate dataFimPrevista
) {
}
