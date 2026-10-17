package com.example.demo.repository;

import com.example.demo.entity.EventoPartido;
import com.example.demo.entity.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface EventoPartidoRepository extends JpaRepository<EventoPartido, Long> {

    @Query("""
            select e from EventoPartido e
              join fetch e.jugador j join fetch j.equipo
            where e.partido.id = :partidoId
            order by e.minuto, e.id
            """)
    List<EventoPartido> findDelPartido(@Param("partidoId") Long partidoId);

    /** Todos los eventos de un torneo (para marcadores, tabla, goleadores y suspensiones). */
    @Query("""
            select e from EventoPartido e
              join fetch e.partido p join fetch e.jugador j join fetch j.equipo
            where p.torneo.id = :torneoId
            """)
    List<EventoPartido> findDelTorneo(@Param("torneoId") Long torneoId);

    @Query("""
            select e from EventoPartido e
              join fetch e.partido p join fetch e.jugador j join fetch j.equipo
            where p.torneo.id = :torneoId and e.tipo in :tipos
            """)
    List<EventoPartido> findDelTorneoPorTipos(@Param("torneoId") Long torneoId,
                                               @Param("tipos") Collection<TipoEvento> tipos);
}
