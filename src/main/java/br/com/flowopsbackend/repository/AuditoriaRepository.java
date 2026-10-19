package br.com.flowopsbackend.repository;

import br.com.flowopsbackend.model.AuditoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<AuditoriaEntity, Long> {
    List<AuditoriaEntity> findByTarefaIdOrderByAuditadaEmDesc(Long tarefaId);
}
