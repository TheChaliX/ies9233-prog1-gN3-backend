package com.example.demo.service;

import com.example.demo.dto.Dtos.EventoDto;
import com.example.demo.dto.Dtos.JugadorDto;
import com.example.demo.dto.Dtos.PlantelDto;
import com.example.demo.dto.Requests.NuevoEvento;
import com.example.demo.entity.*;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.exception.ReglaNegocioException;
import com.example.demo.repository.EventoPartidoRepository;
import com.example.demo.repository.JugadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Detalle del partido: carga y baja de goles y tarjetas. */
@Service
public class EventoService {

    private final EventoPartidoRepository eventos;
    private final JugadorRepository jugadores;
    private final PartidoService partidoService;
    private final SuspensionService suspensiones;

    public EventoService(EventoPartidoRepository eventos, JugadorRepository jugadores,
                         PartidoService partidoService, SuspensionService suspensiones) {
        this.eventos = eventos;
        this.jugadores = jugadores;
        this.partidoService = partidoService;
        this.suspensiones = suspensiones;
    }

    @Transactional(readOnly = true)
    public List<EventoDto> listar(Long partidoId) {
        Partido p = partidoService.buscar(partidoId);
        return eventos.findDelPartido(p.getId()).stream().map(Mapeo::aDto).toList();
    }

    @Transactional(readOnly = true)
    public PlantelDto plantel(Long partidoId) {
        Partido p = partidoService.buscar(partidoId);
        return new PlantelDto(jugadoresDe(p.getEquipoLocal()), jugadoresDe(p.getEquipoVisitante()));
    }

    private List<JugadorDto> jugadoresDe(Equipo e) {
        return jugadores.findByEquipoIdOrderByNumeroCamiseta(e.getId()).stream().map(Mapeo::aDto).toList();
    }

    @Transactional
    public EventoDto crear(Long partidoId, NuevoEvento nuevo) {
        Partido p = partidoService.buscar(partidoId);

        if (p.getEstado() != EstadoPartido.EN_JUEGO) {
            throw ReglaNegocioException.conflicto("El partido no está en juego");
        }
        Jugador jugador = jugadores.findById(nuevo.jugadorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Jugador no encontrado"));

        // Regla 4: solo jugadores inscriptos en alguno de los dos equipos del partido.
        Long equipoId = jugador.getEquipo().getId();
        if (!equipoId.equals(p.getEquipoLocal().getId()) && !equipoId.equals(p.getEquipoVisitante().getId())) {
            throw new ReglaNegocioException("El jugador no pertenece a este partido");
        }

        // Regla 3: un jugador suspendido para esta fecha no puede figurar en el partido.
        suspensiones.motivoSuspension(p.getTorneo().getId(), jugador.getId(), p.getNroFecha()).ifPresent(motivo -> {
            throw new ReglaNegocioException(jugador.getNombre() + " " + jugador.getApellido()
                    + " está suspendido para esta fecha (" + motivo + ")");
        });

        EventoPartido guardado = eventos.save(new EventoPartido(p, jugador, nuevo.tipo(), nuevo.minuto()));
        return Mapeo.aDto(guardado);
    }

    @Transactional
    public void eliminar(Long partidoId, Long eventoId) {
        Partido p = partidoService.buscar(partidoId);
        EventoPartido e = eventos.findById(eventoId)
                .filter(x -> x.getPartido().getId().equals(partidoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento no encontrado"));
        if (p.getEstado() != EstadoPartido.EN_JUEGO) {
            throw ReglaNegocioException.conflicto("Solo se pueden corregir eventos de un partido en juego");
        }
        eventos.delete(e);
    }
}
