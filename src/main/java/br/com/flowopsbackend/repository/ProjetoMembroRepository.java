package br.com.flowopsbackend.repository;

import br.com.flowopsbackend.model.ProjetoMembroEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjetoMembroRepository extends JpaRepository<ProjetoMembroEntity, Long> {
    Optional<ProjetoMembroEntity> findByProjetoIdAndUsuarioId(Long projetoId, Long usuarioId);
    List<ProjetoMembroEntity> findByProjetoIdAndAtivoTrue(Long projetoId);
    List<ProjetoMembroEntity> findByUsuarioIdAndAtivoTrue(Long usuarioId);
    boolean existsByProjetoIdAndUsuarioIdAndAtivoTrue(Long projetoId, Long usuarioId);
}
