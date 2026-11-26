package br.com.flowopsbackend.repository;

import br.com.flowopsbackend.model.ProjetoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjetoRepository extends JpaRepository<ProjetoEntity, Long> {
    @EntityGraph(attributePaths = {"empresa", "gestorResponsavel"})
    List<ProjetoEntity> findByEmpresaIdOrderByCriadoEmDesc(Long empresaId);

    @EntityGraph(attributePaths = {"empresa", "gestorResponsavel"})
    @Query("""
            select p from ProjetoEntity p where p.empresa.id = :empresaId and (
              :admin = true or exists (
                select v.id from VinculoEmpresaEntity v where v.usuario.id = :usuarioId
                  and v.empresa = p.empresa and v.ativo = true and v.status = 'APROVADO'
                  and (v.papel = 'GESTOR' or p.gestorResponsavel.id = :usuarioId or exists (
                    select m.id from ProjetoMembroEntity m where m.projeto = p
                      and m.usuario.id = :usuarioId and m.ativo = true))))
            order by p.criadoEm desc, p.id desc
            """)
    List<ProjetoEntity> findAcessiveis(Long empresaId, Long usuarioId, boolean admin);
}
