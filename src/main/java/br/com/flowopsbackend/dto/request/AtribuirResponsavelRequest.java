package br.com.flowopsbackend.dto.request;

import jakarta.validation.constraints.NotNull;

public record AtribuirResponsavelRequest(@NotNull Long usuarioId) {
}
