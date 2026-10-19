package br.com.flowopsbackend.dto.response;

import br.com.flowopsbackend.model.enums.ResultadoAuditoria;

import java.time.Instant;

public record AuditoriaResponse(
        Long id,
        Long tarefaId,
        String tarefaTitulo,
        Long auditorId,
        String auditorNome,
        ResultadoAuditoria resultado,
        String motivoCorrecao,
        Instant auditadaEm
) {
}
