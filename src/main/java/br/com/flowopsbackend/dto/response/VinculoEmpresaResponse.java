package br.com.flowopsbackend.dto.response;

import br.com.flowopsbackend.model.enums.PapelEmpresa;
import br.com.flowopsbackend.model.enums.StatusVinculo;

import java.time.Instant;

public record VinculoEmpresaResponse(
        Long id,
        Long usuarioId,
        String usuarioNome,
        String usuarioEmail,
        Long empresaId,
        String empresaNome,
        String cargo,
        PapelEmpresa papel,
        StatusVinculo status,
        Instant solicitadoEm,
        Instant analisadoEm,
        boolean ativo
) {
}
