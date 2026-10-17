package com.example.demo.config;

import com.example.demo.dto.Dtos.TorneoDto;
import com.example.demo.dto.Requests.DatosPartido;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.PartidoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Carga la misma Premier League 2026-27 que usaba el mock del frontend (fechas 6, 7 y 8),
 * para que la app muestre lo mismo ahora que habla con el backend real.
 * Los partidos se programan con PartidoService, o sea que pasan por las reglas de cancha y árbitro.
 */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true")
public class DatosDeEjemplo implements CommandLineRunner {

    private static final String[] NOMBRES = {"Lucas", "Mateo", "Tomás", "Nicolás", "Franco", "Agustín", "Bruno", "Joaquín", "Facundo", "Santiago", "Ezequiel"};
    private static final String[] APELLIDOS = {"Gómez", "Fernández", "Rodríguez", "López", "Díaz", "Martínez", "Pérez", "Romero", "Sosa", "Álvarez", "Torres"};

    // equipo, estadio (la "cancha" es el estadio del local)
    private static final String[][] EQUIPOS = {
            {"Arsenal", "Emirates Stadium"}, {"Aston Villa", "Villa Park"}, {"Bournemouth", "Vitality Stadium"},
            {"Brentford", "Gtech Community Stadium"}, {"Brighton", "Amex Stadium"}, {"Chelsea", "Stamford Bridge"},
            {"Coventry City", "Coventry Building Society Arena"}, {"Crystal Palace", "Selhurst Park"},
            {"Everton", "Hill Dickinson Stadium"}, {"Fulham", "Craven Cottage"}, {"Hull City", "MKM Stadium"},
            {"Ipswich Town", "Portman Road"}, {"Leeds United", "Elland Road"}, {"Liverpool", "Anfield"},
            {"Manchester City", "Etihad Stadium"}, {"Manchester United", "Old Trafford"}, {"Newcastle", "St James' Park"},
            {"Nottingham Forest", "City Ground"}, {"Sunderland", "Stadium of Light"}, {"Tottenham", "Tottenham Hotspur Stadium"},
    };

    // fecha, horario, local, visitante, golesLocal, golesVisitante (-1 = sin jugar)
    private static final Object[][] PARTIDOS = {
            {6, "2026-10-10T11:00:00", "Arsenal", "Leeds United", 2, 1},
            {6, "2026-10-10T11:00:00", "Aston Villa", "Brentford", 2, 2},
            {6, "2026-10-10T11:00:00", "Chelsea", "Bournemouth", 5, 1},
            {6, "2026-10-10T11:00:00", "Ipswich Town", "Fulham", 2, 1},
            {6, "2026-10-10T11:00:00", "Manchester United", "Tottenham", 1, 1},
            {6, "2026-10-10T11:00:00", "Sunderland", "Brighton", 0, 2},
            {6, "2026-10-11T11:00:00", "Crystal Palace", "Nottingham Forest", 3, 1},
            {6, "2026-10-11T11:00:00", "Hull City", "Everton", 2, 2},
            {6, "2026-10-11T11:00:00", "Liverpool", "Manchester City", 0, 3},
            {6, "2026-10-12T16:00:00", "Coventry City", "Newcastle", 4, 1},

            {7, "2026-10-17T08:30:00", "Everton", "Chelsea", -1, -1},
            {7, "2026-10-17T11:00:00", "Brentford", "Liverpool", -1, -1},
            {7, "2026-10-17T11:00:00", "Fulham", "Hull City", -1, -1},
            {7, "2026-10-17T11:00:00", "Manchester City", "Ipswich Town", -1, -1},
            {7, "2026-10-17T13:30:00", "Newcastle", "Aston Villa", -1, -1},
            {7, "2026-10-18T10:00:00", "Bournemouth", "Sunderland", -1, -1},
            {7, "2026-10-18T10:00:00", "Brighton", "Crystal Palace", -1, -1},
            {7, "2026-10-18T10:00:00", "Leeds United", "Manchester United", -1, -1},
            {7, "2026-10-18T12:30:00", "Nottingham Forest", "Arsenal", -1, -1},
            {7, "2026-10-19T16:00:00", "Tottenham", "Coventry City", -1, -1},

            {8, "2026-10-23T16:00:00", "Ipswich Town", "Nottingham Forest", -1, -1},
            {8, "2026-10-24T08:30:00", "Aston Villa", "Manchester City", -1, -1},
            {8, "2026-10-24T11:00:00", "Arsenal", "Everton", -1, -1},
            {8, "2026-10-24T11:00:00", "Coventry City", "Fulham", -1, -1},
            {8, "2026-10-24T13:30:00", "Chelsea", "Tottenham", -1, -1},
            {8, "2026-10-25T11:00:00", "Crystal Palace", "Newcastle", -1, -1},
            {8, "2026-10-25T11:00:00", "Hull City", "Brentford", -1, -1},
            {8, "2026-10-25T11:00:00", "Liverpool", "Brighton", -1, -1},
            {8, "2026-10-25T11:00:00", "Manchester United", "Bournemouth", -1, -1},
            {8, "2026-10-25T13:30:00", "Sunderland", "Leeds United", -1, -1},
    };

    private final TorneoRepository torneos;
    private final EquipoRepository equipos;
    private final JugadorRepository jugadores;
    private final CanchaRepository canchas;
    private final ArbitroRepository arbitros;
    private final PartidoRepository partidos;
    private final EventoPartidoRepository eventos;
    private final PartidoService partidoService;
    private final TransactionTemplate tx;

    public DatosDeEjemplo(TorneoRepository torneos, EquipoRepository equipos, JugadorRepository jugadores,
                          CanchaRepository canchas, ArbitroRepository arbitros, PartidoRepository partidos,
                          EventoPartidoRepository eventos, PartidoService partidoService, TransactionTemplate tx) {
        this.torneos = torneos;
        this.equipos = equipos;
        this.jugadores = jugadores;
        this.canchas = canchas;
        this.arbitros = arbitros;
        this.partidos = partidos;
        this.eventos = eventos;
        this.partidoService = partidoService;
        this.tx = tx;
    }

    @Override
    public void run(String... args) {
        if (torneos.count() > 0) return; // ya hay datos: no se pisan
        tx.executeWithoutResult(status -> cargar());
    }

    private void cargar() {
        Torneo torneo = torneos.save(new Torneo("Premier League", "2026-27",
                LocalDateTime.parse("2026-08-15T00:00:00"), LocalDateTime.parse("2027-05-30T23:59:59")));

        Map<String, Equipo> porNombre = new LinkedHashMap<>();
        Map<String, List<Jugador>> planteles = new HashMap<>();
        Map<String, Cancha> canchaDe = new HashMap<>();
        for (int e = 0; e < EQUIPOS.length; e++) {
            String nombre = EQUIPOS[e][0];
            Equipo equipo = equipos.save(new Equipo(torneo, nombre, null, null, "Delegado " + nombre));
            porNombre.put(nombre, equipo);
            canchaDe.put(nombre, canchas.save(new Cancha(EQUIPOS[e][1], "Estadio de " + nombre, null, "Césped natural")));
            List<Jugador> plantel = new ArrayList<>();
            for (int i = 0; i < 11; i++) {
                plantel.add(jugadores.save(new Jugador(equipo, NOMBRES[(i + e) % 11], APELLIDOS[(i * 3 + e * 2) % 11],
                        String.format("%08d", 30000000 + e * 100 + i), i + 1)));
            }
            planteles.put(nombre, plantel);
        }

        List<Arbitro> arbitrosCreados = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            arbitrosCreados.add(arbitros.save(new Arbitro("Árbitro" + (i + 1), "Referee" + (i + 1),
                    String.format("%08d", 20000000 + i), null)));
        }

        for (int n = 0; n < PARTIDOS.length; n++) {
            Object[] d = PARTIDOS[n];
            String local = (String) d[2], visitante = (String) d[3];
            LocalDateTime inicio = LocalDateTime.parse((String) d[1]);
            // Se programa por el servicio: valida cancha, árbitro, equipos y horario como cualquier alta real.
            var creado = partidoService.programar(new DatosPartido(torneo.getId(), (Integer) d[0], inicio, inicio.plusHours(2),
                    canchaDe.get(local).getId(), arbitrosCreados.get(n % arbitrosCreados.size()).getId(),
                    porNombre.get(local).getId(), porNombre.get(visitante).getId()));
            Partido partido = partidos.findById(creado.id()).orElseThrow();
            int gl = (Integer) d[4], gv = (Integer) d[5];
            if (gl < 0) continue;

            partido.setEstado(EstadoPartido.FINALIZADO);
            int id = n + 1;
            cargarGoles(partido, planteles.get(local), gl, id, 0);
            cargarGoles(partido, planteles.get(visitante), gv, id, 11);
            if (id % 2 == 0) eventos.save(new EventoPartido(partido, planteles.get(local).get(5), TipoEvento.AMARILLA, 55));
        }

        // Disciplina de ejemplo, para que el reporte de suspendidos tenga qué mostrar:
        // roja a un jugador de Bournemouth (partido 3) y doble amarilla a uno de Hull City (partido 8).
        Partido p3 = partidos.findById(3L).orElseThrow();
        eventos.save(new EventoPartido(p3, planteles.get("Bournemouth").get(4), TipoEvento.ROJA, 70));
        Partido p8 = partidos.findById(8L).orElseThrow();
        eventos.save(new EventoPartido(p8, planteles.get("Hull City").get(7), TipoEvento.AMARILLA, 30));
        eventos.save(new EventoPartido(p8, planteles.get("Hull City").get(7), TipoEvento.AMARILLA, 77));

        // Un partido en vivo para probar la carga de eventos: Everton vs Chelsea (partido 11).
        Partido enVivo = partidos.findById(11L).orElseThrow();
        enVivo.setEstado(EstadoPartido.EN_JUEGO);
        eventos.save(new EventoPartido(enVivo, planteles.get("Chelsea").get(8), TipoEvento.GOL, 12));
        eventos.save(new EventoPartido(enVivo, planteles.get("Everton").get(3), TipoEvento.AMARILLA, 34));
        eventos.save(new EventoPartido(enVivo, planteles.get("Everton").get(9), TipoEvento.GOL, 41));
    }

    private void cargarGoles(Partido partido, List<Jugador> plantel, int cantidad, int partidoNro, int desfase) {
        for (int k = 0; k < cantidad; k++) {
            Jugador autor = plantel.get(((partidoNro + k * 4) % 10) + 1);
            int minuto = 8 + ((partidoNro * 7 + k * 23 + desfase) % 80);
            eventos.save(new EventoPartido(partido, autor, TipoEvento.GOL, minuto));
        }
    }
}
