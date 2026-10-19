package br.com.flowopsbackend.repository;

import br.com.flowopsbackend.model.TarefaEntity;
import br.com.flowopsbackend.model.enums.StatusTarefa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarefaRepository extends JpaRepository<TarefaEntity, Long> {
    List<TarefaEntity> findByProjetoIdOrderByCriadoEmDesc(Long projetoId);
    List<TarefaEntity> findByStatusOrderByAtualizadoEmAsc(StatusTarefa status);
    long countByProjetoId(Long projetoId);
    long countByProjetoIdAndStatus(Long projetoId, StatusTarefa status);
}
