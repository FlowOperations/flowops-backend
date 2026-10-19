package br.com.flowopsbackend.mapper;

import br.com.flowopsbackend.dto.response.UsuarioResponse;
import br.com.flowopsbackend.model.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(UsuarioEntity usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getCpf(),
                usuario.getEmail(),
                usuario.getDataNascimento(),
                usuario.getPerfilSistema(),
                usuario.isAtivo()
        );
    }
}
