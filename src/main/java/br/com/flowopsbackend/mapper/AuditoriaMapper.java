package br.com.flowopsbackend.mapper;

import br.com.flowopsbackend.dto.response.AuditoriaResponse;
import br.com.flowopsbackend.model.AuditoriaEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaMapper {

    public AuditoriaResponse toResponse(AuditoriaEntity auditoria) {
        return new AuditoriaResponse(
                auditoria.getId(), auditoria.getTarefa().getId(), auditoria.getTarefa().getTitulo(),
                auditoria.getAuditor().getId(), auditoria.getAuditor().getNomeCompleto(), auditoria.getResultado(),
                auditoria.getMotivoCorrecao(), auditoria.getAuditadaEm()
        );
    }
}
