package br.com.flowopsbackend.mapper;

import br.com.flowopsbackend.dto.response.TarefaResponse;
import br.com.flowopsbackend.model.TarefaEntity;
import org.springframework.stereotype.Component;

@Component
public class TarefaMapper {

    public TarefaResponse toResponse(TarefaEntity tarefa) {
        return new TarefaResponse(
                tarefa.getId(),
                tarefa.getProjeto().getId(),
                tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getId(),
                tarefa.getResponsavel() == null ? null : tarefa.getResponsavel().getNomeCompleto(),
                tarefa.getTitulo(), tarefa.getDescricao(), tarefa.getPrioridade(), tarefa.getPrazo(), tarefa.getStatus()
        );
    }
}
