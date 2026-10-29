package br.com.flowopsbackend.controller;

import br.com.flowopsbackend.dto.request.AtualizarUsuarioRequest;
import br.com.flowopsbackend.dto.request.CadastroUsuarioAdminRequest;
import br.com.flowopsbackend.dto.response.UsuarioAdministracaoResponse;
import br.com.flowopsbackend.dto.response.UsuarioResponse;
import br.com.flowopsbackend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @GetMapping
    public List<UsuarioResponse> listarAtivos() {
        return service.listarAtivos();
    }

    @GetMapping("/me")
    public UsuarioResponse buscarAtual() {
        return service.buscarAtual();
    }

    @PutMapping("/me")
    public UsuarioResponse atualizarAtual(@Valid @RequestBody AtualizarUsuarioRequest request) {
        return service.atualizarAtual(request);
    }

    @GetMapping("/administracao")
    public List<UsuarioAdministracaoResponse> listarAdministracao() {
        return service.listarAdministracao();
    }

    @PostMapping("/administracao")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioAdministracaoResponse cadastrarAdministrativamente(
            @Valid @RequestBody CadastroUsuarioAdminRequest request) {
        return service.cadastrarAdministrativamente(request);
    }
}
