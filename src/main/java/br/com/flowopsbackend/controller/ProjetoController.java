package br.com.flowopsbackend.controller;

import br.com.flowopsbackend.dto.request.ProjetoMembroRequest;
import br.com.flowopsbackend.dto.request.ProjetoRequest;
import br.com.flowopsbackend.dto.request.ProjetoStatusRequest;
import br.com.flowopsbackend.dto.response.ProjetoMembroResponse;
import br.com.flowopsbackend.dto.response.ProjetoResponse;
import br.com.flowopsbackend.service.ProjetoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjetoResponse criar(@Valid @RequestBody ProjetoRequest request) {
        return service.criar(request);
    }

    @GetMapping
    public List<ProjetoResponse> listar(@RequestParam Long empresaId) {
        return service.listar(empresaId);
    }

    @GetMapping("/{id}")
    public ProjetoResponse buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ProjetoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProjetoRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/status")
    public ProjetoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody ProjetoStatusRequest request) {
        return service.alterarStatus(id, request);
    }

    @GetMapping("/{id}/membros")
    public List<ProjetoMembroResponse> listarMembros(@PathVariable Long id) {
        return service.listarMembros(id);
    }

    @PostMapping("/{id}/membros")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjetoMembroResponse adicionarMembro(@PathVariable Long id,
                                                  @Valid @RequestBody ProjetoMembroRequest request) {
        return service.adicionarMembro(id, request);
    }

    @DeleteMapping("/{id}/membros/{usuarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerMembro(@PathVariable Long id, @PathVariable Long usuarioId) {
        service.removerMembro(id, usuarioId);
    }
}
