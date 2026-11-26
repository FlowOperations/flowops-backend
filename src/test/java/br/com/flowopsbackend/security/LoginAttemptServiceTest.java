package br.com.flowopsbackend.security;

import br.com.flowopsbackend.exception.NegocioException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginAttemptServiceTest {
    @Test
    void bloqueiaCincoFalhasLiberaAposQuinzeMinutosEReiniciaNoSucesso() {
        Clock clock = mock(Clock.class);
        Instant agora = Instant.parse("2026-11-26T12:00:00Z");
        when(clock.instant()).thenReturn(agora);
        LoginAttemptService service = new LoginAttemptService(clock);
        for (int i = 0; i < 5; i++) {
            service.exigirPermitido("12345678900");
            service.registrarFalha("12345678900");
        }
        assertThatThrownBy(() -> service.exigirPermitido("12345678900"))
                .isInstanceOf(NegocioException.class).hasMessageContaining("15 minutos");
        assertThatCode(() -> service.exigirPermitido("outro-cpf")).doesNotThrowAnyException();
        when(clock.instant()).thenReturn(agora.plusSeconds(901));
        assertThatCode(() -> service.exigirPermitido("12345678900")).doesNotThrowAnyException();
        service.registrarFalha("12345678900");
        service.registrarSucesso("12345678900");
        assertThatCode(() -> service.exigirPermitido("12345678900")).doesNotThrowAnyException();
    }
}
