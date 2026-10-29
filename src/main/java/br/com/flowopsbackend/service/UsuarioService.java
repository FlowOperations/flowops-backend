package br.com.flowopsbackend.service;

import br.com.flowopsbackend.dto.request.AtualizarUsuarioRequest;
import br.com.flowopsbackend.dto.request.CadastroUsuarioAdminRequest;
import br.com.flowopsbackend.dto.response.AcessoUsuarioResponse;
import br.com.flowopsbackend.dto.response.UsuarioAdministracaoResponse;
import br.com.flowopsbackend.dto.response.UsuarioResponse;
import br.com.flowopsbackend.exception.NegocioException;
import br.com.flowopsbackend.mapper.UsuarioMapper;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.model.EmpresaEntity;
import br.com.flowopsbackend.model.ProjetoEntity;
import br.com.flowopsbackend.model.ProjetoMembroEntity;
import br.com.flowopsbackend.model.VinculoEmpresaEntity;
import br.com.flowopsbackend.model.enums.StatusVinculo;
import br.com.flowopsbackend.repository.UsuarioRepository;
import br.com.flowopsbackend.repository.EmpresaRepository;
import br.com.flowopsbackend.repository.ProjetoMembroRepository;
import br.com.flowopsbackend.repository.ProjetoRepository;
import br.com.flowopsbackend.repository.VinculoEmpresaRepository;
import br.com.flowopsbackend.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final VinculoEmpresaRepository vinculoRepository;
    private final EmpresaRepository empresaRepository;
    private final ProjetoRepository projetoRepository;
    private final ProjetoMembroRepository projetoMembroRepository;
    private final UsuarioMapper mapper;
    private final AccessControlService accessControlService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse buscarAtual() {
        return mapper.toResponse(accessControlService.getUsuarioAtual());
    }

    public List<UsuarioResponse> listarAtivos() {
        accessControlService.exigirAdminFlowOps();
        return usuarioRepository.findByAtivoTrueOrderByNomeCompleto()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<UsuarioAdministracaoResponse> listarAdministracao() {
        accessControlService.exigirAdminFlowOps();
        return usuarioRepository.findByAtivoTrueOrderByNomeCompleto().stream()
                .map(usuario -> toAdministracaoResponse(
                        usuario,
                        vinculoRepository.findByUsuarioIdAndStatusAndAtivoTrue(
                                usuario.getId(), StatusVinculo.APROVADO)))
                .toList();
    }

    @Transactional
    public UsuarioAdministracaoResponse cadastrarAdministrativamente(CadastroUsuarioAdminRequest request) {
        UsuarioEntity admin = accessControlService.exigirAdminFlowOps();
        validarUnicidade(request);

        EmpresaEntity empresa = null;
        List<ProjetoEntity> projetos = List.of();
        if (request.tipoAcesso().papelEmpresa() != null) {
            if (request.empresaId() == null || request.cargo() == null || request.cargo().isBlank()) {
                throw new NegocioException(HttpStatus.BAD_REQUEST, "Empresa e cargo são obrigatórios para este tipo de acesso");
            }
            empresa = empresaRepository.findById(request.empresaId())
                    .filter(EmpresaEntity::isFlgAtivo)
                    .orElseThrow(() -> new NegocioException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
            projetos = validarProjetos(request.projetoIds(), empresa);
        } else if (request.empresaId() != null || !request.projetoIds().isEmpty()) {
            throw new NegocioException(HttpStatus.BAD_REQUEST,
                    "Administrador FlowOps não deve possuir empresa ou projetos neste cadastro");
        }

        UsuarioEntity usuario = usuarioRepository.save(new UsuarioEntity(
                request.nomeCompleto().trim(),
                request.cpf(),
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.senha()),
                request.dataNascimento(),
                request.tipoAcesso().perfilSistema()
        ));

        List<VinculoEmpresaEntity> acessos = List.of();
        if (empresa != null) {
            VinculoEmpresaEntity vinculo = new VinculoEmpresaEntity(usuario, empresa);
            vinculo.aprovar(request.cargo().trim(), request.tipoAcesso().papelEmpresa(), admin);
            vinculoRepository.save(vinculo);
            acessos = List.of(vinculo);

            if (!projetos.isEmpty()) {
                List<ProjetoMembroEntity> membros = projetos.stream()
                        .map(projeto -> new ProjetoMembroEntity(projeto, usuario, admin))
                        .toList();
                projetoMembroRepository.saveAll(membros);
            }
        }

        return toAdministracaoResponse(usuario, acessos);
    }

    @Transactional
    public UsuarioResponse atualizarAtual(AtualizarUsuarioRequest request) {
        UsuarioEntity usuario = accessControlService.getUsuarioAtual();
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), usuario.getId())) {
            throw new NegocioException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        usuario.atualizarDados(request.nomeCompleto(), request.email().trim().toLowerCase(), request.dataNascimento());
        return mapper.toResponse(usuario);
    }

    private void validarUnicidade(CadastroUsuarioAdminRequest request) {
        if (usuarioRepository.existsByCpf(request.cpf())) {
            throw new NegocioException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new NegocioException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
    }

    private List<ProjetoEntity> validarProjetos(Set<Long> projetoIds, EmpresaEntity empresa) {
        if (projetoIds.isEmpty()) return List.of();

        List<ProjetoEntity> projetos = projetoRepository.findAllById(projetoIds);
        if (projetos.size() != projetoIds.size()) {
            throw new NegocioException(HttpStatus.NOT_FOUND, "Projeto não encontrado");
        }
        if (projetos.stream().anyMatch(projeto -> !projeto.getEmpresa().getId().equals(empresa.getId()))) {
            throw new NegocioException(HttpStatus.BAD_REQUEST,
                    "Todos os projetos devem pertencer à empresa selecionada");
        }
        return projetos;
    }

    private UsuarioAdministracaoResponse toAdministracaoResponse(
            UsuarioEntity usuario, List<VinculoEmpresaEntity> vinculos) {
        List<AcessoUsuarioResponse> acessos = vinculos.stream()
                .map(vinculo -> new AcessoUsuarioResponse(
                        vinculo.getEmpresa().getId(),
                        vinculo.getEmpresa().getNomeFantasia(),
                        vinculo.getPapel(),
                        vinculo.getCargo()))
                .toList();
        return new UsuarioAdministracaoResponse(
                usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getCpf(),
                usuario.getEmail(),
                usuario.getDataNascimento(),
                usuario.getPerfilSistema(),
                usuario.isAtivo(),
                acessos
        );
    }
}
