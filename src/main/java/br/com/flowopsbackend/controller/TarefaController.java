package br.com.flowopsbackend.controller;

import br.com.flowopsbackend.dto.request.AtribuirResponsavelRequest;
import br.com.flowopsbackend.dto.request.TarefaRequest;
import br.com.flowopsbackend.dto.response.TarefaResponse;
import br.com.flowopsbackend.service.TarefaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/tarefas")
@RequiredArgsConstructor
public class TarefaController {

    private final TarefaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaResponse criar(@Valid @RequestBody TarefaRequest request) {
        return service.criar(request);
    }

    @GetMapping
    public List<TarefaResponse> listar(@RequestParam Long projetoId) {
        return service.listar(projetoId);
    }

    @GetMapping("/{id}")
    public TarefaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public TarefaResponse atualizar(@PathVariable Long id, @Valid @RequestBody TarefaRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/responsavel")
    public TarefaResponse atribuir(@PathVariable Long id, @Valid @RequestBody AtribuirResponsavelRequest request) {
        return service.atribuir(id, request);
    }

    @PatchMapping("/{id}/iniciar")
    public TarefaResponse iniciar(@PathVariable Long id) {
        return service.iniciar(id);
    }

    @PatchMapping("/{id}/enviar-auditoria")
    public TarefaResponse enviarParaAuditoria(@PathVariable Long id) {
        return service.enviarParaAuditoria(id);
    }
}
