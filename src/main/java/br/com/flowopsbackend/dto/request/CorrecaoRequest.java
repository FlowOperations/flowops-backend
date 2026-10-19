package br.com.flowopsbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CorrecaoRequest(@NotBlank @Size(max = 2000) String motivo) {
}
