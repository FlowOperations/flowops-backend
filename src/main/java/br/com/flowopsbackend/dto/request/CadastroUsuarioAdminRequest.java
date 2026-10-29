package br.com.flowopsbackend.dto.request;

import br.com.flowopsbackend.model.enums.TipoAcessoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record CadastroUsuarioAdminRequest(
        @NotBlank(message = "Informe o nome completo.") String nomeCompleto,
        @NotBlank(message = "Informe o CPF.")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos.")
        String cpf,
        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail válido.")
        String email,
        @NotBlank(message = "Informe a senha inicial.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String senha,
        @NotNull(message = "Informe a data de nascimento.")
        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate dataNascimento,
        @NotNull(message = "Selecione o tipo de acesso.") TipoAcessoUsuario tipoAcesso,
        Long empresaId,
        String cargo,
        Set<Long> projetoIds
) {
    public CadastroUsuarioAdminRequest {
        projetoIds = projetoIds == null ? Set.of() : Set.copyOf(projetoIds);
    }
}
