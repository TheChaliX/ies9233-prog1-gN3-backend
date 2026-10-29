package com.example.demo.service;

import com.example.demo.dto.Dtos.*;
import com.example.demo.dto.Requests.*;
import com.example.demo.entity.*;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.exception.ReglaNegocioException;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Alta y consulta de las entidades "de catálogo": torneo, equipo, jugador, cancha y árbitro. */
@Service
public class CatalogoService {

    private final TorneoRepository torneos;
    private final EquipoRepository equipos;
    private final JugadorRepository jugadores;
    private final CanchaRepository canchas;
    private final ArbitroRepository arbitros;

    public CatalogoService(TorneoRepository torneos, EquipoRepository equipos, JugadorRepository jugadores,
                           CanchaRepository canchas, ArbitroRepository arbitros) {
        this.torneos = torneos;
        this.equipos = equipos;
        this.jugadores = jugadores;
        this.canchas = canchas;
        this.arbitros = arbitros;
    }

    // ------------------------------------------------------------ torneos
    @Transactional(readOnly = true)
    public List<TorneoDto> torneos() {
        return torneos.findAll().stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public TorneoDto torneo(Long id) {
        return aDto(torneos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado")));
    }

    @Transactional
    public TorneoDto crearTorneo(NuevoTorneo n) {
        if (!n.fechaHoraFin().isAfter(n.fechaHoraInicio())) {
            throw new ReglaNegocioException("El fin del torneo debe ser posterior al inicio");
        }
        if (torneos.existsByNombreAndTemporada(n.nombre(), n.temporada())) {
            throw ReglaNegocioException.conflicto("Ya existe ese torneo en esa temporada");
        }
        return aDto(torneos.save(new Torneo(n.nombre(), n.temporada(), n.fechaHoraInicio(), n.fechaHoraFin())));
    }

    // ------------------------------------------------------------ equipos
    @Transactional(readOnly = true)
    public List<EquipoDto> equiposDelTorneo(Long torneoId) {
        if (!torneos.existsById(torneoId)) throw new RecursoNoEncontradoException("Torneo no encontrado");
        return equipos.findByTorneoIdOrderByNombre(torneoId).stream().map(this::aDto).toList();
    }

    @Transactional
    public EquipoDto crearEquipo(NuevoEquipo n) {
        Torneo t = torneos.findById(n.torneoId()).orElseThrow(() -> new RecursoNoEncontradoException("Torneo no encontrado"));
        if (equipos.existsByTorneoIdAndNombre(t.getId(), n.nombre())) {
            throw ReglaNegocioException.conflicto("Ya existe un equipo con ese nombre en el torneo");
        }
        return aDto(equipos.save(new Equipo(t, n.nombre(), n.barrio(), n.colorCamiseta(), n.delegado())));
    }

    // ------------------------------------------------------------ jugadores
    @Transactional(readOnly = true)
    public List<JugadorDto> jugadoresDelEquipo(Long equipoId) {
        if (!equipos.existsById(equipoId)) throw new RecursoNoEncontradoException("Equipo no encontrado");
        return jugadores.findByEquipoIdOrderByNumeroCamiseta(equipoId).stream().map(Mapeo::aDto).toList();
    }

    @Transactional
    public JugadorDto crearJugador(NuevoJugador n) {
        Equipo e = equipos.findById(n.equipoId()).orElseThrow(() -> new RecursoNoEncontradoException("Equipo no encontrado"));
        if (jugadores.existsByDni(n.dni())) {
            throw ReglaNegocioException.conflicto("Ya existe un jugador con ese DNI");
        }
        if (jugadores.existsByEquipoIdAndNumeroCamiseta(e.getId(), n.numeroCamiseta())) {
            throw ReglaNegocioException.conflicto("Ese número de camiseta ya está en uso en el equipo");
        }
        return Mapeo.aDto(jugadores.save(new Jugador(e, n.nombre(), n.apellido(), n.dni(), n.numeroCamiseta(), n.posicion())));
    }

    // ------------------------------------------------------------ canchas y árbitros
    @Transactional(readOnly = true)
    public List<CanchaDto> canchas() {
        return canchas.findAll().stream().map(this::aDto).toList();
    }

    @Transactional
    public CanchaDto crearCancha(NuevaCancha n) {
        if (canchas.existsByNombre(n.nombre())) throw ReglaNegocioException.conflicto("Ya existe una cancha con ese nombre");
        return aDto(canchas.save(new Cancha(n.nombre(), n.direccion(), n.barrio(), n.superficie())));
    }

    @Transactional(readOnly = true)
    public List<ArbitroDto> arbitros() {
        return arbitros.findAll().stream().map(this::aDto).toList();
    }

    @Transactional
    public ArbitroDto crearArbitro(NuevoArbitro n) {
        if (arbitros.existsByDni(n.dni())) throw ReglaNegocioException.conflicto("Ya existe un árbitro con ese DNI");
        return aDto(arbitros.save(new Arbitro(n.nombre(), n.apellido(), n.dni(), n.telefono())));
    }

    // ------------------------------------------------------------ mapeos
    private TorneoDto aDto(Torneo t) {
        return new TorneoDto(t.getId(), t.getNombre(), t.getTemporada(), t.getFechaHoraInicio(), t.getFechaHoraFin());
    }
    private EquipoDto aDto(Equipo e) {
        return new EquipoDto(e.getId(), e.getTorneo().getId(), e.getNombre(), e.getBarrio(), e.getColorCamiseta(), e.getDelegado());
    }
    private CanchaDto aDto(Cancha c) {
        return new CanchaDto(c.getId(), c.getNombre(), c.getDireccion(), c.getBarrio(), c.getSuperficie());
    }
    private ArbitroDto aDto(Arbitro a) {
        return new ArbitroDto(a.getId(), a.getNombre(), a.getApellido(), a.getDni(), a.getTelefono());
    }
}
