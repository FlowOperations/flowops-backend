package br.com.flowopsbackend.repository;

import br.com.flowopsbackend.model.ProjetoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjetoRepository extends JpaRepository<ProjetoEntity, Long> {
    List<ProjetoEntity> findByEmpresaIdOrderByCriadoEmDesc(Long empresaId);
}
