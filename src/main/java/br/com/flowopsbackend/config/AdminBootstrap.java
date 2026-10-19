package br.com.flowopsbackend.config;

import br.com.flowopsbackend.model.UsuarioEntity;
import br.com.flowopsbackend.model.enums.PerfilSistema;
import br.com.flowopsbackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "flowops.bootstrap.admin.enabled", havingValue = "true")
public class AdminBootstrap implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${flowops.bootstrap.admin.nome}")
    private String nome;

    @Value("${flowops.bootstrap.admin.cpf}")
    private String cpf;

    @Value("${flowops.bootstrap.admin.email}")
    private String email;

    @Value("${flowops.bootstrap.admin.senha}")
    private String senha;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByCpf(cpf)) {
            return;
        }
        UsuarioEntity admin = new UsuarioEntity(
                nome,
                cpf,
                email,
                passwordEncoder.encode(senha),
                LocalDate.of(1990, 1, 1),
                PerfilSistema.ADMIN_FLOWOPS
        );
        usuarioRepository.save(admin);
    }
}
