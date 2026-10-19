package br.com.flowopsbackend.security;

import br.com.flowopsbackend.exception.NegocioException;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioEntity getUsuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new NegocioException(HttpStatus.UNAUTHORIZED, "Autenticação necessária");
        }
        return usuarioRepository.findByCpf(authentication.getName())
                .filter(UsuarioEntity::isAtivo)
                .orElseThrow(() -> new NegocioException(HttpStatus.UNAUTHORIZED, "Usuário autenticado inválido"));
    }
}
