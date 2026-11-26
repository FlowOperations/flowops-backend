package br.com.flowopsbackend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "flowops.bootstrap.admin.enabled=true",
        "flowops.bootstrap.admin.senha=Admin123!",
        "spring.datasource.url=jdbc:h2:mem:flowops-auth;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveAutenticarAdministradorEGerarToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpf": "00000000000",
                                  "senha": "Admin123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.perfilSistema").value("ADMIN_FLOWOPS"));
    }

    @Test
    void administradorDeveListarUsuariosECriarEmpresa() throws Exception {
        String login = autenticarAdministrador();
        String token = JsonPath.read(login, "$.token");
        Integer administradorId = JsonPath.read(login, "$.usuario.id");

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(administradorId))
                .andExpect(jsonPath("$[0].ativo").value(true));

        mockMvc.perform(post("/api/empresas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "razaoSocial": "FlowOps Cliente Ltda",
                                  "nomeFantasia": "Cliente FlowOps",
                                  "cnpj": "12345678000199",
                                  "flgAtivo": true,
                                  "gestorInicialId": %d,
                                  "cargoGestor": "Gestor de projetos"
                                }
                                """.formatted(administradorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeFantasia").value("Cliente FlowOps"))
                .andExpect(jsonPath("$.cnpj").value("12345678000199"));
    }

    private String autenticarAdministrador() throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpf": "00000000000",
                                  "senha": "Admin123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    void tokenUsaIdEmVezDeCpfENaoCarregaNome() throws Exception {
        String login = autenticarAdministrador();
        String token = JsonPath.read(login, "$.token");
        Number id = JsonPath.read(login, "$.usuario.id");
        String claims = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]), java.nio.charset.StandardCharsets.UTF_8);
        assertThat((String) JsonPath.read(claims, "$.sub")).isEqualTo(id.toString());
        assertThat(claims).doesNotContain("00000000000", "Administrador FlowOps", "\"nome\"");
    }

    @Test
    void limitaTentativasDeLogin() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"cpf\":\"99999999999\",\"senha\":\"incorreta\"}"))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"cpf\":\"99999999999\",\"senha\":\"incorreta\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void recusaTokenAssinadoComAntigoSegredoPadrao() throws Exception {
        String login = autenticarAdministrador();
        Number id = JsonPath.read(login, "$.usuario.id");
        var secret = new javax.crypto.spec.SecretKeySpec(
                "flowops-local-development-secret-key-32".getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        var encoder = new org.springframework.security.oauth2.jwt.NimbusJwtEncoder(
                new com.nimbusds.jose.jwk.source.ImmutableSecret<>(secret));
        var service = new br.com.flowopsbackend.security.JwtService(encoder);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "expiracaoSegundos", 3600L);
        var usuario = new br.com.flowopsbackend.model.UsuarioEntity("Teste", "00000000000", "teste@local.com", "hash",
                java.time.LocalDate.of(1990, 1, 1), br.com.flowopsbackend.model.enums.PerfilSistema.ADMIN_FLOWOPS);
        org.springframework.test.util.ReflectionTestUtils.setField(usuario, "id", id.longValue());
        String token = service.gerarToken(usuario, java.util.List.of("ROLE_ADMIN_FLOWOPS"));
        mockMvc.perform(get("/api/usuarios/administracao").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
