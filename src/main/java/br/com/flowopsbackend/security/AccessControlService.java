package br.com.flowopsbackend.security;

import br.com.flowopsbackend.exception.NegocioException;
import br.com.flowopsbackend.model.ProjetoEntity;
import br.com.flowopsbackend.model.TarefaEntity;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.model.enums.PapelEmpresa;
import br.com.flowopsbackend.model.enums.PerfilSistema;
import br.com.flowopsbackend.model.enums.StatusVinculo;
import br.com.flowopsbackend.repository.ProjetoMembroRepository;
import br.com.flowopsbackend.repository.VinculoEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private static final Set<PapelEmpresa> GESTAO = Set.of(PapelEmpresa.GESTOR, PapelEmpresa.COORDENADOR);

    private final CurrentUserService currentUserService;
    private final VinculoEmpresaRepository vinculoRepository;
    private final ProjetoMembroRepository projetoMembroRepository;

    public UsuarioEntity getUsuarioAtual() {
        return currentUserService.getUsuarioAtual();
    }

    public UsuarioEntity exigirAdminFlowOps() {
        UsuarioEntity usuario = getUsuarioAtual();
        if (usuario.getPerfilSistema() != PerfilSistema.ADMIN_FLOWOPS) {
            throw acessoNegado();
        }
        return usuario;
    }

    public UsuarioEntity exigirGestaoEmpresa(Long empresaId) {
        UsuarioEntity usuario = getUsuarioAtual();
        if (!possuiPapel(usuario.getId(), empresaId, GESTAO)) {
            throw acessoNegado();
        }
        return usuario;
    }

    public UsuarioEntity exigirGestorEmpresa(Long empresaId) {
        UsuarioEntity usuario = getUsuarioAtual();
        if (!possuiPapel(usuario.getId(), empresaId, Set.of(PapelEmpresa.GESTOR))) {
            throw acessoNegado();
        }
        return usuario;
    }

    public UsuarioEntity exigirAcessoProjeto(ProjetoEntity projeto) {
        UsuarioEntity usuario = getUsuarioAtual();
        boolean gestor = possuiPapel(usuario.getId(), projeto.getEmpresa().getId(), Set.of(PapelEmpresa.GESTOR));
        boolean membro = projetoMembroRepository.existsByProjetoIdAndUsuarioIdAndAtivoTrue(projeto.getId(), usuario.getId());
        boolean responsavel = projeto.getGestorResponsavel().getId().equals(usuario.getId());
        if (!gestor && !membro && !responsavel) {
            throw acessoNegado();
        }
        return usuario;
    }

    public UsuarioEntity exigirGestaoProjeto(ProjetoEntity projeto) {
        UsuarioEntity usuario = getUsuarioAtual();
        boolean gestor = possuiPapel(usuario.getId(), projeto.getEmpresa().getId(), Set.of(PapelEmpresa.GESTOR));
        boolean coordenador = possuiPapel(usuario.getId(), projeto.getEmpresa().getId(), Set.of(PapelEmpresa.COORDENADOR));
        boolean membro = projetoMembroRepository.existsByProjetoIdAndUsuarioIdAndAtivoTrue(projeto.getId(), usuario.getId());
        boolean responsavel = projeto.getGestorResponsavel().getId().equals(usuario.getId());
        if (!gestor && !responsavel && !(coordenador && membro)) {
            throw acessoNegado();
        }
        return usuario;
    }

    public boolean possuiGestaoProjeto(UsuarioEntity usuario, ProjetoEntity projeto) {
        boolean gestor = possuiPapel(usuario.getId(), projeto.getEmpresa().getId(), Set.of(PapelEmpresa.GESTOR));
        boolean coordenador = possuiPapel(usuario.getId(), projeto.getEmpresa().getId(), Set.of(PapelEmpresa.COORDENADOR));
        boolean membro = projetoMembroRepository.existsByProjetoIdAndUsuarioIdAndAtivoTrue(projeto.getId(), usuario.getId());
        return gestor || projeto.getGestorResponsavel().getId().equals(usuario.getId()) || coordenador && membro;
    }

    public UsuarioEntity exigirResponsavel(TarefaEntity tarefa) {
        UsuarioEntity usuario = getUsuarioAtual();
        if (tarefa.getResponsavel() == null || !tarefa.getResponsavel().getId().equals(usuario.getId())) {
            throw acessoNegado();
        }
        return usuario;
    }

    public UsuarioEntity exigirAuditor(TarefaEntity tarefa) {
        UsuarioEntity usuario = exigirGestaoProjeto(tarefa.getProjeto());
        if (tarefa.getResponsavel() != null && tarefa.getResponsavel().getId().equals(usuario.getId())) {
            throw new NegocioException(HttpStatus.FORBIDDEN, "Responsável pela tarefa não pode auditá-la");
        }
        return usuario;
    }

    private boolean possuiPapel(Long usuarioId, Long empresaId, Set<PapelEmpresa> papeis) {
        return vinculoRepository.existsByUsuarioIdAndEmpresaIdAndPapelInAndStatusAndAtivoTrue(
                usuarioId, empresaId, papeis, StatusVinculo.APROVADO);
    }

    private NegocioException acessoNegado() {
        return new NegocioException(HttpStatus.FORBIDDEN, "Usuário não possui permissão para esta operação");
    }
}
