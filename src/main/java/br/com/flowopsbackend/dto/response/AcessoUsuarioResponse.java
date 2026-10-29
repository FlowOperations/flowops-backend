package br.com.flowopsbackend.dto.response;

import br.com.flowopsbackend.model.enums.PapelEmpresa;

public record AcessoUsuarioResponse(
        Long empresaId,
        String empresaNome,
        PapelEmpresa papel,
        String cargo
) {
}
