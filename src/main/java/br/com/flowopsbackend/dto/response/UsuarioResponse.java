package br.com.flowopsbackend.dto.response;

import br.com.flowopsbackend.model.enums.PerfilSistema;

import java.time.LocalDate;

public record UsuarioResponse(
        Long id,
        String nomeCompleto,
        String cpf,
        String email,
        LocalDate dataNascimento,
        PerfilSistema perfilSistema,
        boolean ativo
) {
}
