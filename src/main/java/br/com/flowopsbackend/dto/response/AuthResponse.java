package br.com.flowopsbackend.dto.response;

public record AuthResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        UsuarioResponse usuario
) {
}
