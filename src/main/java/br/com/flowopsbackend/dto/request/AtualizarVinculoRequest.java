package br.com.flowopsbackend.dto.request;

import br.com.flowopsbackend.model.enums.PapelEmpresa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarVinculoRequest(
        @NotBlank String cargo,
        @NotNull PapelEmpresa papel,
        @NotNull Boolean ativo
) {
}
