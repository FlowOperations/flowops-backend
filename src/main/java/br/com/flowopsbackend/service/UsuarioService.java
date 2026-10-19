package br.com.flowopsbackend.service;

import br.com.flowopsbackend.dto.request.AtualizarUsuarioRequest;
import br.com.flowopsbackend.dto.response.UsuarioResponse;
import br.com.flowopsbackend.exception.NegocioException;
import br.com.flowopsbackend.mapper.UsuarioMapper;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.repository.UsuarioRepository;
import br.com.flowopsbackend.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper mapper;
    private final AccessControlService accessControlService;

    public UsuarioResponse buscarAtual() {
        return mapper.toResponse(accessControlService.getUsuarioAtual());
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
}
