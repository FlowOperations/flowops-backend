package br.com.flowopsbackend.repository;

import br.com.flowopsbackend.model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    List<UsuarioEntity> findByAtivoTrueOrderByNomeCompleto();
    Optional<UsuarioEntity> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
