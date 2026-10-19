package br.com.flowopsbackend.service;

import br.com.flowopsbackend.dto.request.CadastroUsuarioRequest;
import br.com.flowopsbackend.dto.request.LoginRequest;
import br.com.flowopsbackend.dto.response.AuthResponse;
import br.com.flowopsbackend.dto.response.UsuarioResponse;
import br.com.flowopsbackend.exception.NegocioException;
import br.com.flowopsbackend.mapper.UsuarioMapper;
import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.model.enums.PerfilSistema;
import br.com.flowopsbackend.model.enums.StatusVinculo;
import br.com.flowopsbackend.repository.UsuarioRepository;
import br.com.flowopsbackend.repository.VinculoEmpresaRepository;
import br.com.flowopsbackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final VinculoEmpresaRepository vinculoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;

    @Transactional
    public UsuarioResponse cadastrar(CadastroUsuarioRequest request) {
        if (usuarioRepository.existsByCpf(request.cpf())) {
            throw new NegocioException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new NegocioException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        UsuarioEntity usuario = new UsuarioEntity(
                request.nomeCompleto().trim(),
                request.cpf(),
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.senha()),
                request.dataNascimento(),
                PerfilSistema.USUARIO
        );
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    public AuthResponse autenticar(LoginRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByCpf(request.cpf())
                .filter(UsuarioEntity::isAtivo)
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }
        boolean possuiVinculo = vinculoRepository.existsByUsuarioIdAndStatusAndAtivoTrue(
                usuario.getId(), StatusVinculo.APROVADO);
        if (usuario.getPerfilSistema() != PerfilSistema.ADMIN_FLOWOPS && !possuiVinculo) {
            throw new NegocioException(HttpStatus.FORBIDDEN, "Usuário não possui vínculo empresarial aprovado");
        }
        Set<String> authorities = new LinkedHashSet<>();
        authorities.add("ROLE_" + usuario.getPerfilSistema().name());
        vinculoRepository.findByUsuarioIdAndStatusAndAtivoTrue(usuario.getId(), StatusVinculo.APROVADO)
                .forEach(vinculo -> authorities.add("ROLE_" + vinculo.getPapel().name()));
        String token = jwtService.gerarToken(usuario, authorities);
        return new AuthResponse(token, "Bearer", jwtService.getExpiracaoSegundos(), usuarioMapper.toResponse(usuario));
    }
}
