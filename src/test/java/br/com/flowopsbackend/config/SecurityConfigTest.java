package br.com.flowopsbackend.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SecurityConfigTest {
    @Test
    void exigeSegredoForteDiferenteDoAntigoPadrao() {
        SecurityConfig config = new SecurityConfig(null, null);
        for (String secret : new String[]{"", "curto", "flowops-local-development-secret-key-32"}) {
            assertThatThrownBy(() -> config.jwtSecretKey(secret)).isInstanceOf(IllegalStateException.class);
        }
        assertThat(config.jwtSecretKey("test-only-secret-with-at-least-32-bytes").getAlgorithm()).isEqualTo("HmacSHA256");
    }
}
