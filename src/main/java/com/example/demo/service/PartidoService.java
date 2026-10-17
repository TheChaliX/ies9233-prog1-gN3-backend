package com.example.demo.service;

import com.example.demo.dto.Dtos.*;
import com.example.demo.dto.Requests.CambioEstado;
import com.example.demo.dto.Requests.DatosPartido;
import com.example.demo.entity.*;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.exception.ReglaNegocioException;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PartidoService {

    private final PartidoRepository partidos;
    private final TorneoRepository torneos;
    private final EquipoRepository equipos;
    private final CanchaRepository canchas;
    private final ArbitroRepository arbitros;
    private final EventoPartidoRepository eventos;

    public PartidoService(PartidoRepository partidos, TorneoRepository torneos, EquipoRepository equipos,
                          CanchaRepository canchas, ArbitroRepository arbitros, EventoPartidoRepository eventos) {
        this.partidos = partidos;
        this.torneos = torneos;
        this.equipos = equipos;
        this.canchas = canchas;
        this.arbitros = arbitros;
        this.eventos = eventos;
    }

    // ---------------------------------------------------------------- consultas

    @Transactional(readOnly = true)
    public List<PartidoDto> listarPorTorneo(Long torneoId) {
        if (!torneos.existsById(torneoId)) throw new RecursoNoEncontradoException("Torneo no encontrado");
        Map<Long, List<EventoPartido>> eventosPorPartido = eventos.findDelTorneo(torneoId).stream()
                .collect(Collectors.groupingBy(e -> e.getPartido().getId()));
        return partidos.findDelTorneo(torneoId).stream()
                .map(p -> aDto(p, eventosPorPartido.getOrDefault(p.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PartidoDto obtener(Long id) {
        Partido p = buscar(id);
        return aDto(p, eventos.findDelPartido(id));
    }

    // ---------------------------------------------------------------- alta y reprogramación

    @Transactional
    public PartidoDto programar(DatosPartido d) {
        Torneo torneo = torneos.findById(d.torneoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado"));
        Equipo local = equipos.findById(d.equipoLocalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo local no encontrado"));
        Equipo visitante = equipos.findById(d.equipoVisitanteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipo visitante no encontrado"));
        Cancha cancha = canchas.findById(d.canchaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cancha no encontrada"));
        Arbitro arbitro = arbitros.findById(d.arbitroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Árbitro no encontrado"));

        if (local.getId().equals(visitante.getId())) {
            throw new ReglaNegocioException("Un equipo no puede jugar contra sí mismo");
        }
        if (!local.getTorneo().getId().equals(torneo.getId()) || !visitante.getTorneo().getId().equals(torneo.getId())) {
            throw new ReglaNegocioException("Los dos equipos deben pertenecer al torneo del partido");
        }
        if (partidos.existsByTorneoIdAndEquipoLocalIdAndEquipoVisitanteIdAndIdNot(
                torneo.getId(), local.getId(), visitante.getId(), -1L)) {
            throw ReglaNegocioException.conflicto("Ese cruce (local vs visitante) ya está programado en el torneo");
        }
        validarHorario(d, torneo);
        validarDisponibilidad(d.canchaId(), d.arbitroId(), d, -1L);

        Partido p = partidos.save(new Partido(torneo, d.nroFecha(), d.inicio(), d.fin(), cancha, arbitro, local, visitante));
        return aDto(p, List.of());
    }

    /** Reprogramar solo se permite mientras el partido está PROGRAMADO. */
    @Transactional
    public PartidoDto reprogramar(Long id, DatosPartido d) {
        Partido p = buscar(id);
        if (p.getEstado() != EstadoPartido.PROGRAMADO) {
            throw ReglaNegocioException.conflicto("Solo se puede reprogramar un partido PROGRAMADO");
        }
        Cancha cancha = canchas.findById(d.canchaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cancha no encontrada"));
        Arbitro arbitro = arbitros.findById(d.arbitroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Árbitro no encontrado"));
        validarHorario(d, p.getTorneo());
        validarDisponibilidad(d.canchaId(), d.arbitroId(), d, p.getId());
        p.reprogramar(d.nroFecha(), d.inicio(), d.fin(), cancha, arbitro);
        return aDto(p, List.of());
    }

    private void validarHorario(DatosPartido d, Torneo torneo) {
        if (!d.fin().isAfter(d.inicio())) {
            throw new ReglaNegocioException("El fin del partido debe ser posterior al inicio");
        }
        if (d.inicio().isBefore(torneo.getFechaHoraInicio()) || d.fin().isAfter(torneo.getFechaHoraFin())) {
            throw new ReglaNegocioException("El partido debe jugarse dentro de las fechas del torneo");
        }
    }

    /** Reglas 1 y 2: ni la cancha ni el árbitro pueden tener dos partidos que se pisen en el tiempo. */
    private void validarDisponibilidad(Long canchaId, Long arbitroId, DatosPartido d, Long excluirId) {
        if (partidos.existeSuperpuestoEnCancha(canchaId, d.inicio(), d.fin(), excluirId, EstadoPartido.SUSPENDIDO)) {
            throw ReglaNegocioException.conflicto("La cancha ya tiene un partido en ese horario");
        }
        if (partidos.existeSuperpuestoDeArbitro(arbitroId, d.inicio(), d.fin(), excluirId, EstadoPartido.SUSPENDIDO)) {
            throw ReglaNegocioException.conflicto("El árbitro ya dirige otro partido en ese horario");
        }
    }

    // ---------------------------------------------------------------- máquina de estados

    @Transactional
    public PartidoDto cambiarEstado(Long id, CambioEstado cambio) {
        Partido p = buscar(id);
        EstadoPartido destino = cambio.estado();
        if (!p.getEstado().puedePasarA(destino)) {
            throw ReglaNegocioException.conflicto("Ese cambio de estado no está permitido (" + p.getEstado() + " -> " + destino + ")");
        }
        p.setEstado(destino);
        return aDto(p, eventos.findDelPartido(id));
    }

    // ---------------------------------------------------------------- helpers

    Partido buscar(Long id) {
        return partidos.findConDetalle(id).orElseThrow(() -> new RecursoNoEncontradoException("Partido no encontrado"));
    }

    /** El marcador se deriva de los eventos: nunca se guarda. */
    private PartidoDto aDto(Partido p, List<EventoPartido> evs) {
        int local = 0, visitante = 0;
        for (EventoPartido e : evs) {
            Lado suma = Mapeo.ladoQueSumaGol(p, e);
            if (suma == Lado.LOCAL) local++;
            else if (suma == Lado.VISITANTE) visitante++;
        }
        return new PartidoDto(p.getId(), p.getNroFecha(), p.getInicio(), p.getEquipoLocal().getNombre(),
                p.getEquipoVisitante().getNombre(), local, visitante, p.getEstado(), p.getCancha().getNombre());
    }
}
