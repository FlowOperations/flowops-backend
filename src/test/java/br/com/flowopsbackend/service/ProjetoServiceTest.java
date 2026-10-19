package br.com.flowopsbackend.service;

import br.com.flowopsbackend.mapper.ProjetoMapper;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjetoServiceTest {

    private TarefaRepository tarefaRepository;
    private ProjetoService service;

    @BeforeEach
    void setUp() {
        tarefaRepository = mock(TarefaRepository.class);
        service = new ProjetoService(
                mock(ProjetoRepository.class), mock(ProjetoMembroRepository.class), mock(EmpresaRepository.class),
                mock(UsuarioRepository.class), mock(VinculoEmpresaRepository.class), tarefaRepository,
                mock(ProjetoMapper.class), mock(AccessControlService.class)
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
}
