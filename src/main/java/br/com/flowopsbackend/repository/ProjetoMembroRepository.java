package br.com.flowopsbackend.repository;

import br.com.flowopsbackend.model.ProjetoMembroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface ProjetoMembroRepository extends JpaRepository<ProjetoMembroEntity, Long> {
    Optional<ProjetoMembroEntity> findByProjetoIdAndUsuarioId(Long projetoId, Long usuarioId);
    @EntityGraph(attributePaths = "usuario")
    List<ProjetoMembroEntity> findByProjetoIdAndAtivoTrue(Long projetoId);
    List<ProjetoMembroEntity> findByUsuarioIdAndAtivoTrue(Long usuarioId);
    boolean existsByProjetoIdAndUsuarioIdAndAtivoTrue(Long projetoId, Long usuarioId);
}
