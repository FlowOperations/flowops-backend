package br.com.flowopsbackend.dto.response;

import br.com.flowopsbackend.model.enums.PrioridadeTarefa;
import br.com.flowopsbackend.model.enums.StatusTarefa;

import java.time.LocalDateTime;

public record TarefaResponse(
        Long id,
        Long projetoId,
        Long responsavelId,
        String responsavelNome,
        String titulo,
        String descricao,
        PrioridadeTarefa prioridade,
        LocalDateTime prazo,
        StatusTarefa status
) {
}
