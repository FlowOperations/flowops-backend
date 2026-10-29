package br.com.flowopsbackend.dto.response;

import br.com.flowopsbackend.model.enums.PerfilSistema;

import java.time.LocalDate;
import java.util.List;

public record UsuarioAdministracaoResponse(
        Long id,
        String nomeCompleto,
        String cpf,
        String email,
        LocalDate dataNascimento,
        PerfilSistema perfilSistema,
        boolean ativo,
        List<AcessoUsuarioResponse> acessos
) {
}
