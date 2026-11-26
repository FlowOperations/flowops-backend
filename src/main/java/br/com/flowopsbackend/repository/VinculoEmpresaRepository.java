package br.com.flowopsbackend.repository;

import br.com.flowopsbackend.model.VinculoEmpresaEntity;
import br.com.flowopsbackend.model.enums.PapelEmpresa;
import br.com.flowopsbackend.model.enums.StatusVinculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VinculoEmpresaRepository extends JpaRepository<VinculoEmpresaEntity, Long> {
    @EntityGraph(attributePaths = {"usuario", "empresa"})
    List<VinculoEmpresaEntity> findByUsuarioIdOrderBySolicitadoEmDesc(Long usuarioId);
    @EntityGraph(attributePaths = {"usuario", "empresa"})
    List<VinculoEmpresaEntity> findByUsuarioIdInAndStatusAndAtivoTrue(Collection<Long> usuarioIds, StatusVinculo status);
    Optional<VinculoEmpresaEntity> findByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);
    Optional<VinculoEmpresaEntity> findByUsuarioIdAndEmpresaIdAndStatusAndAtivoTrue(
            Long usuarioId, Long empresaId, StatusVinculo status);
    @EntityGraph(attributePaths = {"usuario", "empresa"})
    List<VinculoEmpresaEntity> findByEmpresaIdAndStatusOrderBySolicitadoEmAsc(Long empresaId, StatusVinculo status);
    @EntityGraph(attributePaths = {"usuario", "empresa"})
    List<VinculoEmpresaEntity> findByEmpresaIdAndStatusAndAtivoTrueOrderByUsuarioNomeCompleto(
            Long empresaId, StatusVinculo status);
    @EntityGraph(attributePaths = {"usuario", "empresa"})
    List<VinculoEmpresaEntity> findByUsuarioIdAndStatusAndAtivoTrue(Long usuarioId, StatusVinculo status);
    List<VinculoEmpresaEntity> findByEmpresaIdAndPapelInAndStatusAndAtivoTrue(
            Long empresaId, Collection<PapelEmpresa> papeis, StatusVinculo status);
    boolean existsByUsuarioIdAndStatusAndAtivoTrue(Long usuarioId, StatusVinculo status);
    boolean existsByUsuarioIdAndEmpresaIdAndPapelInAndStatusAndAtivoTrue(
            Long usuarioId, Long empresaId, Collection<PapelEmpresa> papeis, StatusVinculo status);
}
