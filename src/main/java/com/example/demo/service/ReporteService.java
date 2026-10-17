package com.example.demo.service;

import com.example.demo.dto.Dtos.GoleadorDto;
import com.example.demo.dto.Dtos.PosicionDto;
import com.example.demo.entity.*;
import com.example.demo.exception.RecursoNoEncontradoException;
import com.example.demo.repository.EquipoRepository;
import com.example.demo.repository.EventoPartidoRepository;
import com.example.demo.repository.PartidoRepository;
import com.example.demo.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/** Cálculos derivados: tabla de posiciones y goleadores. Todo sale de los eventos de partidos FINALIZADOS. */
@Service
public class ReporteService {

    private final TorneoRepository torneos;
    private final EquipoRepository equipos;
    private final PartidoRepository partidos;
    private final EventoPartidoRepository eventos;

    public ReporteService(TorneoRepository torneos, EquipoRepository equipos,
                          PartidoRepository partidos, EventoPartidoRepository eventos) {
        this.torneos = torneos;
        this.equipos = equipos;
        this.partidos = partidos;
        this.eventos = eventos;
    }

    private static final class Fila {
        final String equipo;
        int pj, pg, pe, pp, gf, gc;
        Fila(String equipo) { this.equipo = equipo; }
        int pts() { return pg * 3 + pe; }
        int dg() { return gf - gc; }
    }

    /**
     * Puntos: 3 por victoria, 1 por empate. Desempate: diferencia de gol, luego goles a favor,
     * y por último orden alfabético (así el orden es siempre determinista).
     */
    @Transactional(readOnly = true)
    public List<PosicionDto> posiciones(Long torneoId) {
        verificarTorneo(torneoId);
        Map<Long, Fila> filas = new LinkedHashMap<>();
        for (Equipo e : equipos.findByTorneoIdOrderByNombre(torneoId)) filas.put(e.getId(), new Fila(e.getNombre()));

        Map<Long, List<EventoPartido>> porPartido = eventos.findDelTorneo(torneoId).stream()
                .collect(Collectors.groupingBy(e -> e.getPartido().getId()));

        for (Partido p : partidos.findDelTorneo(torneoId)) {
            if (p.getEstado() != EstadoPartido.FINALIZADO) continue;
            int gl = 0, gv = 0;
            for (EventoPartido e : porPartido.getOrDefault(p.getId(), List.of())) {
                Lado suma = Mapeo.ladoQueSumaGol(p, e);
                if (suma == Lado.LOCAL) gl++;
                else if (suma == Lado.VISITANTE) gv++;
            }
            Fila local = filas.get(p.getEquipoLocal().getId());
            Fila visitante = filas.get(p.getEquipoVisitante().getId());
            local.pj++; visitante.pj++;
            local.gf += gl; local.gc += gv;
            visitante.gf += gv; visitante.gc += gl;
            if (gl > gv) { local.pg++; visitante.pp++; }
            else if (gl < gv) { visitante.pg++; local.pp++; }
            else { local.pe++; visitante.pe++; }
        }

        List<Fila> ordenadas = filas.values().stream()
                .sorted(Comparator.comparingInt(Fila::pts).reversed()
                        .thenComparing(Comparator.comparingInt(Fila::dg).reversed())
                        .thenComparing(Comparator.comparingInt((Fila f) -> f.gf).reversed())
                        .thenComparing(f -> f.equipo))
                .toList();
        List<PosicionDto> resultado = new ArrayList<>();
        for (int i = 0; i < ordenadas.size(); i++) {
            Fila f = ordenadas.get(i);
            resultado.add(new PosicionDto(i + 1, f.equipo, f.pj, f.pg, f.pe, f.pp, f.gf, f.gc, f.dg(), f.pts()));
        }
        return resultado;
    }

    /** Solo cuentan los GOL (los goles en contra no son del goleador) de partidos jugados (EN_JUEGO o FINALIZADO). */
    @Transactional(readOnly = true)
    public List<GoleadorDto> goleadores(Long torneoId) {
        verificarTorneo(torneoId);
        Map<Long, List<EventoPartido>> porJugador = eventos.findDelTorneoPorTipos(torneoId, List.of(TipoEvento.GOL)).stream()
                .filter(e -> e.getPartido().getEstado() == EstadoPartido.FINALIZADO
                        || e.getPartido().getEstado() == EstadoPartido.EN_JUEGO)
                .collect(Collectors.groupingBy(e -> e.getJugador().getId()));
        List<List<EventoPartido>> orden = porJugador.values().stream()
                .sorted(Comparator.<List<EventoPartido>>comparingInt(List::size).reversed()
                        .thenComparing(l -> l.get(0).getJugador().getApellido())
                        .thenComparing(l -> l.get(0).getJugador().getNombre()))
                .toList();
        List<GoleadorDto> resultado = new ArrayList<>();
        int posicion = 0, anterior = -1, indice = 0;
        for (List<EventoPartido> goles : orden) {
            indice++;
            if (goles.size() != anterior) { posicion = indice; anterior = goles.size(); } // empate de goles = misma posición
            Jugador j = goles.get(0).getJugador();
            resultado.add(new GoleadorDto(posicion, j.getId(), j.getNombre(), j.getApellido(),
                    j.getEquipo().getNombre(), goles.size()));
        }
        return resultado;
    }

    private void verificarTorneo(Long torneoId) {
        if (!torneos.existsById(torneoId)) throw new RecursoNoEncontradoException("Torneo no encontrado");
    }
}
