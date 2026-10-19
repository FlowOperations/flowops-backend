package br.com.flowopsbackend.dto.response;

import br.com.flowopsbackend.model.enums.StatusProjeto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjetoResponse(
        Long id,
        Long empresaId,
        String empresaNome,
        Long gestorResponsavelId,
        String gestorResponsavelNome,
        String nome,
        String descricao,
        LocalDate dataInicioPrevista,
        LocalDate dataFimPrevista,
        StatusProjeto status,
        BigDecimal progresso
) {
}
