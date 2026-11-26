package br.com.flowopsbackend;

import br.com.flowopsbackend.model.*;
import br.com.flowopsbackend.model.enums.*;
import br.com.flowopsbackend.repository.*;
import br.com.flowopsbackend.security.JwtService;
import br.com.flowopsbackend.service.AuditoriaService;
import br.com.flowopsbackend.service.EmailService;
import br.com.flowopsbackend.dto.request.CorrecaoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flowops-audit;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "flowops.mail.enabled=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditRegressionIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository usuarios;
    @Autowired EmpresaRepository empresas;
    @Autowired VinculoEmpresaRepository vinculos;
    @Autowired ProjetoRepository projetos;
    @Autowired ProjetoMembroRepository membros;
    @Autowired TarefaRepository tarefas;
    @Autowired AuditoriaRepository auditorias;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;
    @Autowired TransactionTemplate tx;
    @Autowired AuditoriaService auditoriaService;
    @Autowired EmailService emails;
    @MockitoBean JavaMailSender mailSender;
    @Autowired org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor emailExecutor;

    private UsuarioEntity admin, gestor, funcionario, coordenador, externo;
    private EmpresaEntity empresa;
    private ProjetoEntity projeto;
    private TarefaEntity tarefa;
    private static final AtomicInteger sequence = new AtomicInteger();

    @AfterEach
    void aguardarEmails() {
        org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(5)).until(() ->
                emailExecutor.getActiveCount() == 0 && emailExecutor.getThreadPoolExecutor().getQueue().isEmpty());
    }

    @BeforeEach
    void preparar() {
        tx.executeWithoutResult(status -> {
            int n = sequence.incrementAndGet();
            admin = usuario(n, "Admin", PerfilSistema.ADMIN_FLOWOPS);
            gestor = usuario(n, "Gestor", PerfilSistema.USUARIO);
            funcionario = usuario(n, "Funcionario", PerfilSistema.USUARIO);
            coordenador = usuario(n, "Coordenador", PerfilSistema.USUARIO);
            externo = usuario(n, "Externo", PerfilSistema.USUARIO);
            empresa = empresas.save(new EmpresaEntity("Empresa " + n, "Empresa " + n, "%014d".formatted(n), true));
            vincular(gestor, PapelEmpresa.GESTOR);
            vincular(funcionario, PapelEmpresa.FUNCIONARIO);
            vincular(coordenador, PapelEmpresa.COORDENADOR);
            projeto = projetos.save(new ProjetoEntity(empresa, gestor, "Projeto " + n, "Teste",
                    LocalDate.now(), LocalDate.now().plusDays(10)));
            for (UsuarioEntity usuario : List.of(gestor, funcionario, coordenador)) {
                membros.save(new ProjetoMembroEntity(projeto, usuario, gestor));
            }
            tarefa = new TarefaEntity(projeto, "Tarefa " + n, "Descrição", PrioridadeTarefa.MEDIA, LocalDateTime.now().plusDays(2));
            tarefa.atribuirResponsavel(funcionario);
            tarefa = tarefas.save(tarefa);
        });
    }

    @Test
    void autocadastroPodeEntrarSolicitarEAguardarAprovacaoSemAcessoAoProjeto() throws Exception {
        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                "nomeCompleto", "Nova Pessoa", "cpf", "52998224725", "email", "nova@teste.local",
                "senha", "Teste123!", "dataNascimento", "1995-01-01"))))
                .andExpect(status().isCreated());
        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"52998224725\",\"senha\":\"Teste123!\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = "Bearer " + JsonPath.read(login, "$.token");
        mvc.perform(get("/api/projetos/" + projeto.getId()).header("Authorization", token)).andExpect(status().isForbidden());
        mvc.perform(post("/api/vinculos/solicitacoes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"empresaId\":" + empresa.getId() + "}")).andExpect(status().isUnauthorized());
        String pedido = mvc.perform(post("/api/vinculos/solicitacoes").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"empresaId\":" + empresa.getId() + "}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("SOLICITADO"))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/vinculos/solicitacoes/minhas").header("Authorization", token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("SOLICITADO"));
        Number vinculoId = JsonPath.read(pedido, "$.id");
        mvc.perform(patch("/api/vinculos/" + vinculoId + "/analise").header("Authorization", bearer(gestor))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aprovado\":true,\"cargo\":\"Analista\",\"papel\":\"FUNCIONARIO\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true));
        // A aprovação empresarial não concede automaticamente acesso a um projeto.
        mvc.perform(get("/api/projetos/" + projeto.getId()).header("Authorization", token)).andExpect(status().isForbidden());
    }

    @Test
    void administradorAcessaProjetoETarefasSemVinculoEmpresarial() throws Exception {
        mvc.perform(get("/api/projetos").param("empresaId", empresa.getId().toString()).header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(projeto.getId()));
        mvc.perform(get("/api/projetos/" + projeto.getId()).header("Authorization", bearer(admin))).andExpect(status().isOk());
        mvc.perform(get("/api/tarefas").param("projetoId", projeto.getId().toString()).header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(tarefa.getId()));
        mvc.perform(post("/api/projetos").header("Authorization", bearer(admin)).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("empresaId", empresa.getId(), "gestorResponsavelId", gestor.getId(),
                        "nome", "Criado pelo admin", "descricao", "Teste", "dataInicioPrevista", LocalDate.now().toString(),
                        "dataFimPrevista", LocalDate.now().plusDays(2).toString())))).andExpect(status().isCreated());
    }

    @Test
    void membroRemovidoNaoPodeExecutarTarefaMesmoContinuandoResponsavel() throws Exception {
        mvc.perform(delete("/api/projetos/" + projeto.getId() + "/membros/" + funcionario.getId())
                .header("Authorization", bearer(gestor))).andExpect(status().isNoContent());
        mvc.perform(patch("/api/tarefas/" + tarefa.getId() + "/iniciar").header("Authorization", bearer(funcionario)))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/tarefas/" + tarefa.getId() + "/enviar-auditoria").header("Authorization", bearer(funcionario)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/projetos").param("empresaId", empresa.getId().toString()).header("Authorization", bearer(funcionario)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void transicoesExigemResponsavelEStatusCorretosEAuditoriaNaoPermiteAutoaprovacao() throws Exception {
        mvc.perform(patch("/api/tarefas/" + tarefa.getId() + "/iniciar").header("Authorization", bearer(coordenador)))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/tarefas/" + tarefa.getId() + "/enviar-auditoria").header("Authorization", bearer(funcionario)))
                .andExpect(status().isConflict());
        mvc.perform(patch("/api/tarefas/" + tarefa.getId() + "/iniciar").header("Authorization", bearer(funcionario)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
        mvc.perform(patch("/api/tarefas/" + tarefa.getId() + "/enviar-auditoria").header("Authorization", bearer(funcionario)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AGUARDANDO_AUDITORIA"));
        mvc.perform(post("/api/auditorias/tarefas/" + tarefa.getId() + "/aprovar").header("Authorization", bearer(funcionario)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/auditorias/pendentes").header("Authorization", bearer(externo)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/auditorias/pendentes").header("Authorization", bearer(coordenador)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(tarefa.getId()));
        mvc.perform(post("/api/auditorias/tarefas/" + tarefa.getId() + "/aprovar").header("Authorization", bearer(coordenador)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/projetos").param("empresaId", empresa.getId().toString()).header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].progresso").value(100.0));
        mvc.perform(post("/api/auditorias/tarefas/" + tarefa.getId() + "/correcao").header("Authorization", bearer(gestor))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Corrigir\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void auditoresConcorrentesProduzemUmaUnicaDecisao() throws Exception {
        tx.executeWithoutResult(status -> tarefas.findById(tarefa.getId()).orElseThrow().enviarParaAuditoria());
        CountDownLatch lidos = new CountDownLatch(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> aprovar = () -> auditarConcorrentemente(lidos, false);
            Callable<Boolean> corrigir = () -> auditarConcorrentemente(lidos, true);
            Future<Boolean> primeiro = pool.submit(aprovar);
            Future<Boolean> segundo = pool.submit(corrigir);
            assertThat(List.of(primeiro.get(15, TimeUnit.SECONDS), segundo.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
            assertThat(auditorias.findByTarefaIdOrderByAuditadaEmDesc(tarefa.getId())).hasSize(1);
        } finally {
            pool.shutdownNow();
            assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void coordenadorResponsavelNaoPodeAuditarPropriaTarefa() throws Exception {
        tx.executeWithoutResult(status -> {
            TarefaEntity atual = tarefas.findById(tarefa.getId()).orElseThrow();
            atual.atribuirResponsavel(coordenador);
            atual.enviarParaAuditoria();
        });
        mvc.perform(post("/api/auditorias/tarefas/" + tarefa.getId() + "/aprovar").header("Authorization", bearer(coordenador)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.mensagem").value("Responsável pela tarefa não pode auditá-la"));
        mvc.perform(get("/api/auditorias/pendentes").header("Authorization", bearer(coordenador)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void vinculoDesativadoRevogaAcessoMesmoComMembroAtivo() throws Exception {
        tx.executeWithoutResult(status -> vinculos.findByUsuarioIdAndEmpresaId(funcionario.getId(), empresa.getId())
                .orElseThrow().atualizarAcesso("Teste", PapelEmpresa.FUNCIONARIO, false));
        mvc.perform(get("/api/projetos/" + projeto.getId()).header("Authorization", bearer(funcionario))).andExpect(status().isForbidden());
        mvc.perform(patch("/api/tarefas/" + tarefa.getId() + "/iniciar").header("Authorization", bearer(funcionario))).andExpect(status().isForbidden());
        mvc.perform(get("/api/projetos").param("empresaId", empresa.getId().toString()).header("Authorization", bearer(funcionario)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void falhaSmtpNaoDesfazAprovacaoEEmailSoSaiAposCommit() {
        doThrow(new MailSendException("SMTP indisponível")).when(mailSender).send(any(SimpleMailMessage.class));
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                admin.getId().toString(), null, List.of()));
        try {
            tx.executeWithoutResult(status -> {
                tarefas.findById(tarefa.getId()).orElseThrow().enviarParaAuditoria();
                auditoriaService.aprovar(tarefa.getId());
                verifyNoInteractions(mailSender);
            });
            verify(mailSender, timeout(3000)).send(any(SimpleMailMessage.class));
            assertThat(tarefas.findById(tarefa.getId()).orElseThrow().getStatus()).isEqualTo(StatusTarefa.APROVADA);
        } finally { SecurityContextHolder.clearContext(); }
    }

    @Test
    void rollbackNaoDisparaEmail() {
        tx.executeWithoutResult(status -> {
            emails.enviar("nao-enviar@teste.local", "Teste", "Rollback");
            status.setRollbackOnly();
        });
        verify(mailSender, after(200).never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void recusaCpfInvalidoEPrazoPassadoEOcultacaoDeSwagger() throws Exception {
        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nomeCompleto":"Teste", "cpf":"11111111111", "email":"teste@local.com",
                 "senha":"Teste123!", "dataNascimento":"1990-01-01"}
                """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.cpf").value("Informe um CPF válido."));
        mvc.perform(post("/api/tarefas").header("Authorization", bearer(admin)).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("projetoId", projeto.getId(), "titulo", "Prazo antigo", "descricao", "Teste",
                        "prioridade", "MEDIA", "prazo", "2020-01-01T10:00:00"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.prazo").isNotEmpty());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isUnauthorized());
        mvc.perform(get("/v3/api-docs").header("Authorization", bearer(admin))).andExpect(status().isNotFound());
    }

    private boolean auditarConcorrentemente(CountDownLatch lidos, boolean correcao) {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                admin.getId().toString(), null, List.of()));
        try {
            tx.executeWithoutResult(status -> {
                tarefas.findById(tarefa.getId()).orElseThrow();
                lidos.countDown();
                try { if (!lidos.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Timeout na barreira"); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
                if (correcao) auditoriaService.solicitarCorrecao(tarefa.getId(), new CorrecaoRequest("Corrigir"));
                else auditoriaService.aprovar(tarefa.getId());
            });
            return true;
        } catch (org.springframework.dao.OptimisticLockingFailureException e) {
            return false;
        } finally { SecurityContextHolder.clearContext(); }
    }

    private UsuarioEntity usuario(int n, String nome, PerfilSistema perfil) {
        int tipo = switch (nome) { case "Admin" -> 1; case "Gestor" -> 2; case "Funcionario" -> 3; case "Coordenador" -> 4; default -> 5; };
        return usuarios.save(new UsuarioEntity(nome, "%011d".formatted(n * 10L + tipo),
                nome + n + "@teste.local", encoder.encode("Teste123!"), LocalDate.of(1990, 1, 1), perfil));
    }

    private void vincular(UsuarioEntity usuario, PapelEmpresa papel) {
        VinculoEmpresaEntity vinculo = new VinculoEmpresaEntity(usuario, empresa);
        vinculo.aprovar("Teste", papel, admin);
        vinculos.save(vinculo);
    }

    private String bearer(UsuarioEntity usuario) {
        return "Bearer " + jwt.gerarToken(usuario, List.of("ROLE_" + usuario.getPerfilSistema()));
    }
}
