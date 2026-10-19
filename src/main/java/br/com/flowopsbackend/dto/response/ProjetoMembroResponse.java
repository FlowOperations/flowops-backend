package br.com.flowopsbackend.dto.response;

import java.time.Instant;

public record ProjetoMembroResponse(
        Long id,
        Long usuarioId,
        String nome,
        String email,
        boolean ativo,
        Instant concedidoEm
) {
}
