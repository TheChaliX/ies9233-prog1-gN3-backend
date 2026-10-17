package com.example.demo.service;

import com.example.demo.dto.Dtos.SuspendidoDto;
import com.example.demo.entity.EventoPartido;
import com.example.demo.entity.TipoEvento;
import com.example.demo.repository.EventoPartidoRepository;
import com.example.demo.repository.PartidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * Regla 3: un jugador con 2 amarillas o 1 roja queda suspendido para la fecha siguiente.
 *
 * Las amarillas se acumulan a lo largo del torneo: cada par de amarillas completa una suspensión.
 * Si en la fecha F el jugador completa un par de amarillas (la 2ª, 4ª, 6ª...) o recibe una roja,
 * no puede jugar la fecha F+1. Se calcula siempre a partir de los eventos, no se guarda.
 */
@Service
public class SuspensionService {

    private final EventoPartidoRepository eventos;
    private final PartidoRepository partidos;

    public SuspensionService(EventoPartidoRepository eventos, PartidoRepository partidos) {
        this.eventos = eventos;
        this.partidos = partidos;
    }

    /** Suspensión de un jugador para una fecha concreta del torneo. */
    private record Suspension(EventoPartido ejemplo, int fechaSuspension, String motivo) {}

    @Transactional(readOnly = true)
    public List<SuspendidoDto> suspendidos(Long torneoId, Integer fecha) {
        int fechaConsulta = fecha != null ? fecha : fechaEnCurso(torneoId);
        return calcular(torneoId).stream()
                .filter(s -> s.fechaSuspension() == fechaConsulta)
                .map(s -> new SuspendidoDto(s.ejemplo().getJugador().getId(), s.ejemplo().getJugador().getNombre(),
                        s.ejemplo().getJugador().getApellido(), s.ejemplo().getJugador().getEquipo().getNombre(),
                        s.motivo(), s.fechaSuspension()))
                .sorted(Comparator.comparing(SuspendidoDto::equipo).thenComparing(SuspendidoDto::apellido))
                .toList();
    }

    /** ¿Está el jugador suspendido para jugar en esa fecha? */
    @Transactional(readOnly = true)
    public Optional<String> motivoSuspension(Long torneoId, Long jugadorId, int nroFecha) {
        return calcular(torneoId).stream()
                .filter(s -> s.ejemplo().getJugador().getId().equals(jugadorId) && s.fechaSuspension() == nroFecha)
                .map(Suspension::motivo)
                .findFirst();
    }

    /** Fecha "siguiente" por defecto: la primera del torneo que todavía tiene partidos sin finalizar. */
    private int fechaEnCurso(Long torneoId) {
        var todos = partidos.findDelTorneo(torneoId);
        return todos.stream()
                .filter(p -> p.getEstado() != com.example.demo.entity.EstadoPartido.FINALIZADO)
                .mapToInt(p -> p.getNroFecha()).min()
                .orElseGet(() -> todos.stream().mapToInt(p -> p.getNroFecha()).max().orElse(0) + 1);
    }

    private List<Suspension> calcular(Long torneoId) {
        var disciplinarios = eventos.findDelTorneoPorTipos(torneoId, List.of(TipoEvento.AMARILLA, TipoEvento.ROJA));
        Map<Long, List<EventoPartido>> porJugador = new HashMap<>();
        for (EventoPartido e : disciplinarios) {
            porJugador.computeIfAbsent(e.getJugador().getId(), k -> new ArrayList<>()).add(e);
        }

        List<Suspension> resultado = new ArrayList<>();
        for (List<EventoPartido> lista : porJugador.values()) {
            // Agrupa por fecha del torneo, en orden.
            TreeMap<Integer, List<EventoPartido>> porFecha = new TreeMap<>();
            for (EventoPartido e : lista) {
                porFecha.computeIfAbsent(e.getPartido().getNroFecha(), k -> new ArrayList<>()).add(e);
            }
            int amarillasAcumuladas = 0;
            for (var entrada : porFecha.entrySet()) {
                long amarillas = entrada.getValue().stream().filter(e -> e.getTipo() == TipoEvento.AMARILLA).count();
                boolean roja = entrada.getValue().stream().anyMatch(e -> e.getTipo() == TipoEvento.ROJA);
                int antes = amarillasAcumuladas;
                amarillasAcumuladas += (int) amarillas;
                String motivo = null;
                if (roja) motivo = "Roja";
                else if (amarillasAcumuladas / 2 > antes / 2) motivo = "2 amarillas";
                if (motivo != null) {
                    resultado.add(new Suspension(entrada.getValue().get(0), entrada.getKey() + 1, motivo));
                }
            }
        }
        return resultado;
    }
}
