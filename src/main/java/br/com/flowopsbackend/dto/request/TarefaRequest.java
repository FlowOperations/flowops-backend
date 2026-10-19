package br.com.flowopsbackend.dto.request;

import br.com.flowopsbackend.model.enums.PrioridadeTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TarefaRequest(
        @NotNull Long projetoId,
        @NotBlank @Size(max = 255) String titulo,
        @NotBlank @Size(max = 3000) String descricao,
        @NotNull PrioridadeTarefa prioridade,
        @NotNull LocalDateTime prazo,
        Long responsavelId
) {
}
