package com.example.demo.repository;

import com.example.demo.entity.EstadoPartido;
import com.example.demo.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PartidoRepository extends JpaRepository<Partido, Long> {

    @Query("""
            select p from Partido p
              join fetch p.equipoLocal join fetch p.equipoVisitante join fetch p.cancha
            where p.torneo.id = :torneoId
            order by p.nroFecha, p.inicio, p.id
            """)
    List<Partido> findDelTorneo(@Param("torneoId") Long torneoId);

    @Query("""
            select p from Partido p
              join fetch p.equipoLocal join fetch p.equipoVisitante join fetch p.cancha
            where p.id = :id
            """)
    java.util.Optional<Partido> findConDetalle(@Param("id") Long id);

    boolean existsByTorneoIdAndEquipoLocalIdAndEquipoVisitanteIdAndIdNot(
            Long torneoId, Long localId, Long visitanteId, Long id);

    // Regla 1: dos partidos se pisan si inicio1 < fin2 y inicio2 < fin1. Los suspendidos no ocupan la cancha.
    @Query("""
            select count(p) > 0 from Partido p
            where p.cancha.id = :canchaId and p.id <> :excluirId and p.estado <> :suspendido
              and p.inicio < :fin and p.fin > :inicio
            """)
    boolean existeSuperpuestoEnCancha(@Param("canchaId") Long canchaId, @Param("inicio") LocalDateTime inicio,
                                      @Param("fin") LocalDateTime fin, @Param("excluirId") Long excluirId,
                                      @Param("suspendido") EstadoPartido suspendido);

    // Regla 2: lo mismo para el árbitro.
    @Query("""
            select count(p) > 0 from Partido p
            where p.arbitro.id = :arbitroId and p.id <> :excluirId and p.estado <> :suspendido
              and p.inicio < :fin and p.fin > :inicio
            """)
    boolean existeSuperpuestoDeArbitro(@Param("arbitroId") Long arbitroId, @Param("inicio") LocalDateTime inicio,
                                       @Param("fin") LocalDateTime fin, @Param("excluirId") Long excluirId,
                                       @Param("suspendido") EstadoPartido suspendido);
}
