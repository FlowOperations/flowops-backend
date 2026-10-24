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
}
