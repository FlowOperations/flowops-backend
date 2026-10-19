package br.com.flowopsbackend.dto.request;

import jakarta.validation.constraints.NotNull;

public record SolicitarVinculoRequest(@NotNull Long empresaId) {
}
