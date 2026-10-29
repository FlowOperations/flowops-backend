package br.com.flowopsbackend.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EmpresaCadastroRequestValidationTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void informaEmPortuguesQuaisCamposObrigatoriosPrecisamSerPreenchidos() {
        EmpresaCadastroRequest request = new EmpresaCadastroRequest("", "", "", null, null, "");

        Map<String, Set<String>> mensagens = mensagensPorCampo(request);

        assertThat(mensagens.get("razaoSocial")).contains("Informe a razão social.");
        assertThat(mensagens.get("nomeFantasia")).contains("Informe o nome fantasia.");
        assertThat(mensagens.get("cnpj")).contains("Informe o CNPJ.");
        assertThat(mensagens.get("flgAtivo")).contains("Informe se a empresa está ativa.");
        assertThat(mensagens.get("gestorInicialId")).contains("Selecione o gestor inicial.");
        assertThat(mensagens.get("cargoGestor")).contains("Informe o cargo do gestor.");
    }

    @Test
    void explicaOFormatoDoCnpjSemExporAExpressaoRegular() {
        EmpresaCadastroRequest request = new EmpresaCadastroRequest(
                "Empresa Exemplo Ltda.",
                "Empresa Exemplo",
                "11111111111111111",
                true,
                1L,
                "Gestor de projetos"
        );

        Map<String, Set<String>> mensagens = mensagensPorCampo(request);

        assertThat(mensagens.get("cnpj")).containsExactly("O CNPJ deve conter exatamente 14 dígitos.");
    }

    private Map<String, Set<String>> mensagensPorCampo(EmpresaCadastroRequest request) {
        return VALIDATOR.validate(request).stream()
                .collect(Collectors.groupingBy(
                        violation -> violation.getPropertyPath().toString(),
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toSet())
                ));
    }
}
