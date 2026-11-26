package br.com.flowopsbackend.security;

import br.com.flowopsbackend.exception.NegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoginAttemptService {
    private static final int MAX_TENTATIVAS = 5;
    private static final int MAX_CHAVES = 10_000;
    private static final Duration JANELA = Duration.ofMinutes(15);
    private final Map<String, Tentativa> tentativas = new HashMap<>();
    private final Clock clock;

    public LoginAttemptService() { this(Clock.systemUTC()); }

    LoginAttemptService(Clock clock) { this.clock = clock; }

    public synchronized void exigirPermitido(String cpf) {
        limparExpiradas();
        Tentativa tentativa = tentativas.get(cpf);
        if ((tentativa != null && tentativa.falhas >= MAX_TENTATIVAS)
                || (tentativa == null && tentativas.size() >= MAX_CHAVES)) {
            throw new NegocioException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Aguarde 15 minutos e tente novamente.");
        }
    }

    public synchronized void registrarFalha(String cpf) {
        limparExpiradas();
        Tentativa tentativa = tentativas.get(cpf);
        if (tentativa != null) {
            tentativas.put(cpf, new Tentativa(tentativa.falhas + 1, tentativa.expiraEm));
        } else if (tentativas.size() < MAX_CHAVES) {
            tentativas.put(cpf, new Tentativa(1, clock.instant().plus(JANELA)));
        }
    }

    public synchronized void registrarSucesso(String cpf) { tentativas.remove(cpf); }

    private void limparExpiradas() {
        Instant agora = clock.instant();
        tentativas.values().removeIf(t -> !agora.isBefore(t.expiraEm));
    }

    private record Tentativa(int falhas, Instant expiraEm) { }
}
