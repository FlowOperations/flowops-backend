package br.com.flowopsbackend.dto.request;

import br.com.flowopsbackend.model.enums.PapelEmpresa;
import jakarta.validation.constraints.NotNull;

public record AnalisarVinculoRequest(
        @NotNull Boolean aprovado,
        String cargo,
        PapelEmpresa papel
) {
}
