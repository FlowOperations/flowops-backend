package br.com.flowopsbackend.security;

import br.com.flowopsbackend.model.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${flowops.security.jwt.expiration}")
    private long expiracaoSegundos;

    public String gerarToken(UsuarioEntity usuario, Collection<String> authorities) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("flowops-api")
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(expiracaoSegundos))
                .subject(usuario.getCpf())
                .claim("uid", usuario.getId())
                .claim("nome", usuario.getNomeCompleto())
                .claim("authorities", authorities)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public long getExpiracaoSegundos() {
        return expiracaoSegundos;
    }
}
