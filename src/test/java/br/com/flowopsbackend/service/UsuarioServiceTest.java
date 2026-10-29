package br.com.flowopsbackend.service;

import br.com.flowopsbackend.dto.request.CadastroUsuarioAdminRequest;
import br.com.flowopsbackend.dto.response.UsuarioAdministracaoResponse;
import br.com.flowopsbackend.exception.NegocioException;
import br.com.flowopsbackend.mapper.UsuarioMapper;
import br.com.flowopsbackend.model.EmpresaEntity;
import br.com.flowopsbackend.model.ProjetoEntity;
import br.com.flowopsbackend.model.ProjetoMembroEntity;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.model.VinculoEmpresaEntity;
import br.com.flowopsbackend.model.enums.PapelEmpresa;
import br.com.flowopsbackend.model.enums.PerfilSistema;
import br.com.flowopsbackend.model.enums.TipoAcessoUsuario;
import br.com.flowopsbackend.repository.EmpresaRepository;
import br.com.flowopsbackend.repository.ProjetoMembroRepository;
import br.com.flowopsbackend.repository.ProjetoRepository;
import br.com.flowopsbackend.repository.UsuarioRepository;
import br.com.flowopsbackend.repository.VinculoEmpresaRepository;
import br.com.flowopsbackend.security.AccessControlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private VinculoEmpresaRepository vinculoRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private ProjetoRepository projetoRepository;
    @Mock
    private ProjetoMembroRepository projetoMembroRepository;
    @Mock
    private AccessControlService accessControlService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService service;
    private UsuarioEntity admin;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(
                usuarioRepository,
                vinculoRepository,
                empresaRepository,
                projetoRepository,
                projetoMembroRepository,
                new UsuarioMapper(),
                accessControlService,
                passwordEncoder
        );
        admin = usuario("Administrador FlowOps", "00000000000", "admin@flowops.local", PerfilSistema.ADMIN_FLOWOPS, 1L);
        when(accessControlService.exigirAdminFlowOps()).thenReturn(admin);
    }

    @Test
    @SuppressWarnings("unchecked")
    void cadastraCoordenadorComVinculoAprovadoEAcessoAosProjetosSelecionados() {
        EmpresaEntity empresa = empresa(10L);
        ProjetoEntity projeto = projeto(20L, empresa, admin);
        CadastroUsuarioAdminRequest request = request(
                TipoAcessoUsuario.COORDENADOR,
                empresa.getId(),
                "Coordenação técnica",
                Set.of(projeto.getId())
        );
        prepararCadastro(request, empresa, List.of(projeto));

        UsuarioAdministracaoResponse response = service.cadastrarAdministrativamente(request);

        ArgumentCaptor<VinculoEmpresaEntity> vinculoCaptor = ArgumentCaptor.forClass(VinculoEmpresaEntity.class);
        verify(vinculoRepository).save(vinculoCaptor.capture());
        assertThat(vinculoCaptor.getValue().getPapel()).isEqualTo(PapelEmpresa.COORDENADOR);
        assertThat(vinculoCaptor.getValue().isAtivo()).isTrue();

        ArgumentCaptor<List<ProjetoMembroEntity>> membrosCaptor = ArgumentCaptor.forClass(List.class);
        verify(projetoMembroRepository).saveAll(membrosCaptor.capture());
        assertThat(membrosCaptor.getValue())
                .singleElement()
                .satisfies(membro -> {
                    assertThat(membro.getProjeto().getId()).isEqualTo(20L);
                    assertThat(membro.isAtivo()).isTrue();
                });
        assertThat(response.acessos())
                .singleElement()
                .satisfies(acesso -> {
                    assertThat(acesso.empresaId()).isEqualTo(10L);
                    assertThat(acesso.papel()).isEqualTo(PapelEmpresa.COORDENADOR);
                });
    }

    @Test
    void cadastraAdministradorFlowOpsSemVinculoEmpresarial() {
        CadastroUsuarioAdminRequest request = request(TipoAcessoUsuario.ADMIN_FLOWOPS, null, null, Set.of());
        prepararCadastro(request, null, List.of());

        UsuarioAdministracaoResponse response = service.cadastrarAdministrativamente(request);

        assertThat(response.perfilSistema()).isEqualTo(PerfilSistema.ADMIN_FLOWOPS);
        assertThat(response.acessos()).isEmpty();
        verify(vinculoRepository, never()).save(any());
        verify(projetoMembroRepository, never()).saveAll(any());
    }

    @Test
    void rejeitaCpfDuplicadoAntesDeSalvar() {
        CadastroUsuarioAdminRequest request = request(TipoAcessoUsuario.FUNCIONARIO, 10L, "Desenvolvedor", Set.of());
        when(usuarioRepository.existsByCpf(request.cpf())).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrarAdministrativamente(request))
                .isInstanceOf(NegocioException.class)
                .hasMessage("CPF já cadastrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rejeitaProjetoDeOutraEmpresaAntesDeSalvar() {
        EmpresaEntity empresaSelecionada = empresa(10L);
        EmpresaEntity outraEmpresa = empresa(11L);
        ProjetoEntity projetoDeOutraEmpresa = projeto(20L, outraEmpresa, admin);
        CadastroUsuarioAdminRequest request = request(
                TipoAcessoUsuario.COORDENADOR,
                empresaSelecionada.getId(),
                "Coordenação técnica",
                Set.of(projetoDeOutraEmpresa.getId())
        );
        when(empresaRepository.findById(10L)).thenReturn(Optional.of(empresaSelecionada));
        when(projetoRepository.findAllById(request.projetoIds())).thenReturn(List.of(projetoDeOutraEmpresa));

        assertThatThrownBy(() -> service.cadastrarAdministrativamente(request))
                .isInstanceOf(NegocioException.class)
                .hasMessage("Todos os projetos devem pertencer à empresa selecionada");

        verify(usuarioRepository, never()).save(any());
    }

    private void prepararCadastro(CadastroUsuarioAdminRequest request, EmpresaEntity empresa,
                                  List<ProjetoEntity> projetos) {
        when(passwordEncoder.encode(request.senha())).thenReturn("hash-seguro");
        when(usuarioRepository.save(any())).thenAnswer(invocation -> {
            UsuarioEntity usuario = invocation.getArgument(0);
            ReflectionTestUtils.setField(usuario, "id", 99L);
            return usuario;
        });
        if (empresa != null) {
            when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
            when(projetoRepository.findAllById(request.projetoIds())).thenReturn(projetos);
            when(vinculoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        }
    }

    private CadastroUsuarioAdminRequest request(TipoAcessoUsuario tipo, Long empresaId, String cargo,
                                                 Set<Long> projetoIds) {
        return new CadastroUsuarioAdminRequest(
                "Coordenador Teste",
                "90000000001",
                "coordenador@flowops.local",
                "Coordenador123!",
                LocalDate.of(1995, 1, 15),
                tipo,
                empresaId,
                cargo,
                projetoIds
        );
    }

    private UsuarioEntity usuario(String nome, String cpf, String email, PerfilSistema perfil, Long id) {
        UsuarioEntity usuario = new UsuarioEntity(nome, cpf, email, "hash", LocalDate.of(1990, 1, 1), perfil);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private EmpresaEntity empresa(Long id) {
        EmpresaEntity empresa = new EmpresaEntity("Empresa Exemplo Ltda.", "Empresa Exemplo", "12345678000199", true);
        ReflectionTestUtils.setField(empresa, "id", id);
        return empresa;
    }

    private ProjetoEntity projeto(Long id, EmpresaEntity empresa, UsuarioEntity gestor) {
        ProjetoEntity projeto = new ProjetoEntity(
                empresa,
                gestor,
                "Projeto Exemplo",
                "Descrição",
                LocalDate.now(),
                LocalDate.now().plusMonths(1)
        );
        ReflectionTestUtils.setField(projeto, "id", id);
        return projeto;
    }
}
