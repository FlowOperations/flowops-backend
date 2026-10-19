package br.com.flowopsbackend.dto.request;

import br.com.flowopsbackend.model.enums.StatusProjeto;
import jakarta.validation.constraints.NotNull;

public record ProjetoStatusRequest(@NotNull StatusProjeto status) {
}
