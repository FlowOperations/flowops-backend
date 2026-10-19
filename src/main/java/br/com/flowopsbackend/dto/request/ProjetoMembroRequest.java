package br.com.flowopsbackend.dto.request;

import jakarta.validation.constraints.NotNull;

public record ProjetoMembroRequest(@NotNull Long usuarioId) {
}
