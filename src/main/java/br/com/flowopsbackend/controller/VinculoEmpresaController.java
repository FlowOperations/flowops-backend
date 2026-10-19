package br.com.flowopsbackend.controller;

import br.com.flowopsbackend.dto.request.AnalisarVinculoRequest;
import br.com.flowopsbackend.dto.request.AtualizarVinculoRequest;
import br.com.flowopsbackend.dto.request.SolicitarVinculoRequest;
import br.com.flowopsbackend.dto.response.VinculoEmpresaResponse;
import br.com.flowopsbackend.service.VinculoEmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vinculos")
@RequiredArgsConstructor
public class VinculoEmpresaController {

    private final VinculoEmpresaService service;

    @PostMapping("/solicitacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public VinculoEmpresaResponse solicitar(@Valid @RequestBody SolicitarVinculoRequest request) {
        return service.solicitar(request);
    }

    @GetMapping("/meus")
    public List<VinculoEmpresaResponse> listarMeus() {
        return service.listarMeus();
    }

    @GetMapping("/empresas/{empresaId}/pendentes")
    public List<VinculoEmpresaResponse> listarPendentes(@PathVariable Long empresaId) {
        return service.listarPendentes(empresaId);
    }

    @GetMapping("/empresas/{empresaId}/colaboradores")
    public List<VinculoEmpresaResponse> listarColaboradores(@PathVariable Long empresaId) {
        return service.listarColaboradores(empresaId);
    }

    @PatchMapping("/{id}/analise")
    public VinculoEmpresaResponse analisar(@PathVariable Long id, @Valid @RequestBody AnalisarVinculoRequest request) {
        return service.analisar(id, request);
    }

    @PatchMapping("/{id}")
    public VinculoEmpresaResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarVinculoRequest request) {
        return service.atualizar(id, request);
    }
}
