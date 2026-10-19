package br.com.flowopsbackend.controller;

import br.com.flowopsbackend.dto.request.CorrecaoRequest;
import br.com.flowopsbackend.dto.response.AuditoriaResponse;
import br.com.flowopsbackend.dto.response.TarefaResponse;
import br.com.flowopsbackend.service.AuditoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auditorias")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService service;

    @GetMapping("/pendentes")
    public List<TarefaResponse> listarPendentes() {
        return service.listarPendentes();
    }

    @GetMapping("/tarefas/{tarefaId}")
    public List<AuditoriaResponse> listarHistorico(@PathVariable Long tarefaId) {
        return service.listarHistorico(tarefaId);
    }

    @PostMapping("/tarefas/{tarefaId}/aprovar")
    public AuditoriaResponse aprovar(@PathVariable Long tarefaId) {
        return service.aprovar(tarefaId);
    }

    @PostMapping("/tarefas/{tarefaId}/correcao")
    public AuditoriaResponse solicitarCorrecao(@PathVariable Long tarefaId,
                                                @Valid @RequestBody CorrecaoRequest request) {
        return service.solicitarCorrecao(tarefaId, request);
    }
}
