package br.com.flowopsbackend.repository;

import br.com.flowopsbackend.model.TarefaEntity;
import br.com.flowopsbackend.model.enums.StatusTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Collection;

public interface TarefaRepository extends JpaRepository<TarefaEntity, Long> {
    @EntityGraph(attributePaths = {"projeto", "responsavel"})
    List<TarefaEntity> findByProjetoIdOrderByCriadoEmDesc(Long projetoId);
    List<TarefaEntity> findByStatusOrderByAtualizadoEmAsc(StatusTarefa status);
    long countByProjetoId(Long projetoId);
    long countByProjetoIdAndStatus(Long projetoId, StatusTarefa status);

    @EntityGraph(attributePaths = {"projeto", "responsavel"})
    @Query("""
            select t from TarefaEntity t where t.status = 'AGUARDANDO_AUDITORIA'
              and (t.responsavel is null or t.responsavel.id <> :usuarioId)
              and (:admin = true or exists (
                select v.id from VinculoEmpresaEntity v where v.usuario.id = :usuarioId
                  and v.empresa = t.projeto.empresa and v.ativo = true and v.status = 'APROVADO'
                  and (v.papel = 'GESTOR' or t.projeto.gestorResponsavel.id = :usuarioId
                    or (v.papel = 'COORDENADOR' and exists (
                      select m.id from ProjetoMembroEntity m where m.projeto = t.projeto
                        and m.usuario.id = :usuarioId and m.ativo = true)))))
            order by t.atualizadoEm asc, t.id asc
            """)
    List<TarefaEntity> findAuditaveis(Long usuarioId, boolean admin);

    @Query("""
            select t.projeto.id as projetoId, count(t) as total,
              sum(case when t.status = :aprovada then 1 else 0 end) as aprovadas
            from TarefaEntity t where t.projeto.id in :projetoIds group by t.projeto.id
            """)
    List<Progresso> contarProgresso(Collection<Long> projetoIds, StatusTarefa aprovada);

    interface Progresso {
        Long getProjetoId();
        long getTotal();
        long getAprovadas();
    }
}
