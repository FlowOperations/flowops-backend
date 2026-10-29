package br.com.flowopsbackend.service;

import br.com.flowopsbackend.mapper.ProjetoMapper;
import br.com.flowopsbackend.model.EmpresaEntity;
import br.com.flowopsbackend.model.ProjetoEntity;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.model.enums.PerfilSistema;
import br.com.flowopsbackend.model.enums.StatusTarefa;
import br.com.flowopsbackend.repository.EmpresaRepository;
import br.com.flowopsbackend.repository.ProjetoMembroRepository;
import br.com.flowopsbackend.repository.ProjetoRepository;
import br.com.flowopsbackend.repository.TarefaRepository;
import br.com.flowopsbackend.repository.UsuarioRepository;
import br.com.flowopsbackend.repository.VinculoEmpresaRepository;
import br.com.flowopsbackend.security.AccessControlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjetoServiceTest {

    private TarefaRepository tarefaRepository;
    private ProjetoRepository projetoRepository;
    private ProjetoMembroRepository membroRepository;
    private VinculoEmpresaRepository vinculoRepository;
    private AccessControlService accessControlService;
    private ProjetoService service;

    @BeforeEach
    void setUp() {
        tarefaRepository = mock(TarefaRepository.class);
        projetoRepository = mock(ProjetoRepository.class);
        membroRepository = mock(ProjetoMembroRepository.class);
        vinculoRepository = mock(VinculoEmpresaRepository.class);
        accessControlService = mock(AccessControlService.class);
        service = new ProjetoService(
                projetoRepository, membroRepository, mock(EmpresaRepository.class),
                mock(UsuarioRepository.class), vinculoRepository, tarefaRepository,
                new ProjetoMapper(), accessControlService
        );
    }

    @Test
    void deveCalcularProgressoComPesoIgual() {
        when(tarefaRepository.countByProjetoId(1L)).thenReturn(4L);
        when(tarefaRepository.countByProjetoIdAndStatus(1L, StatusTarefa.APROVADA)).thenReturn(3L);

        assertThat(service.calcularProgresso(1L)).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    void deveRetornarZeroQuandoProjetoNaoPossuiTarefas() {
        when(tarefaRepository.countByProjetoId(1L)).thenReturn(0L);

        assertThat(service.calcularProgresso(1L)).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    void administradorFlowOpsPodeListarProjetosParaAdministrarUsuarios() {
        UsuarioEntity admin = new UsuarioEntity(
                "Administrador FlowOps", "00000000000", "admin@flowops.local", "hash",
                LocalDate.of(1990, 1, 1), PerfilSistema.ADMIN_FLOWOPS);
        ReflectionTestUtils.setField(admin, "id", 1L);
        EmpresaEntity empresa = new EmpresaEntity(
                "Empresa Exemplo Ltda.", "Empresa Exemplo", "12345678000199", true);
        ReflectionTestUtils.setField(empresa, "id", 10L);
        UsuarioEntity outroGestor = new UsuarioEntity(
                "Gestor da Empresa", "11111111111", "gestor@empresa.local", "hash",
                LocalDate.of(1991, 1, 1), PerfilSistema.USUARIO);
        ReflectionTestUtils.setField(outroGestor, "id", 2L);
        ProjetoEntity projeto = new ProjetoEntity(
                empresa, outroGestor, "Projeto Exemplo", "Descrição",
                LocalDate.now(), LocalDate.now().plusMonths(1));
        ReflectionTestUtils.setField(projeto, "id", 20L);
        when(accessControlService.getUsuarioAtual()).thenReturn(admin);
        when(projetoRepository.findByEmpresaIdOrderByCriadoEmDesc(10L)).thenReturn(List.of(projeto));

        assertThat(service.listar(10L))
                .singleElement()
                .satisfies(response -> assertThat(response.id()).isEqualTo(20L));
    }
}
