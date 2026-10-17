package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de las reglas de negocio contra los datos de ejemplo (DatosDeEjemplo).
 * Cada test corre en una transacción que se revierte, así no se pisan entre sí.
 *
 * Ids de ejemplo: equipos 1..20 en orden alfabético (1 Arsenal, 2 Aston Villa, ... 6 Chelsea, 9 Everton),
 * jugadores (equipo-1)*11 + camiseta, partido 11 = Everton vs Chelsea EN_JUEGO,
 * jugador 27 = Bournemouth con roja en la fecha 6 (suspendido en la 7).
 */
@SpringBootTest
@Transactional
class TorneoApiTests {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    // Ojo: MockMvc decodifica el cuerpo como ISO-8859-1, por eso los textos buscados no llevan tildes.
    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Test
    void listaLosPartidosDelTorneoConElMarcadorCalculado() throws Exception {
        mvc.perform(get("/torneos/1/partidos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"local\":\"Arsenal\"")))
                .andExpect(content().string(containsString("\"golesLocal\":2,\"golesVisitante\":1")));
    }

    @Test
    void elMarcadorDelPartidoEnVivoSaleDeSusEventos() throws Exception {
        mvc.perform(get("/partidos/11"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"golesLocal\":1,\"golesVisitante\":1")))
                .andExpect(content().string(containsString("\"estado\":\"EN_JUEGO\"")));
    }

    @Test
    void tablaDePosicionesIncluyeATodosLosEquipos() throws Exception {
        mvc.perform(get("/torneos/1/posiciones"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"posicion\":1")))
                .andExpect(content().string(containsString("\"posicion\":20")));
    }

    @Test
    void cargaUnGolYSumaAlMarcador() throws Exception {
        mvc.perform(post("/partidos/11/eventos").contentType(JSON)
                        .content("{\"jugadorId\":93,\"tipo\":\"GOL\",\"minuto\":50}"))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"lado\":\"LOCAL\"")));
        mvc.perform(get("/partidos/11"))
                .andExpect(content().string(containsString("\"golesLocal\":2,\"golesVisitante\":1")));
    }

    @Test
    void golEnContraSumaAlRival() throws Exception {
        // jugador 93 es de Everton (local): su gol en contra suma al visitante.
        mvc.perform(post("/partidos/11/eventos").contentType(JSON)
                        .content("{\"jugadorId\":93,\"tipo\":\"GOL_EN_CONTRA\",\"minuto\":60}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/partidos/11"))
                .andExpect(content().string(containsString("\"golesLocal\":1,\"golesVisitante\":2")));
    }

    @Test
    void regla4_rechazaJugadorDeOtroEquipo() throws Exception {
        mvc.perform(post("/partidos/11/eventos").contentType(JSON)
                        .content("{\"jugadorId\":1,\"tipo\":\"GOL\",\"minuto\":50}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("no pertenece a este partido")));
    }

    @Test
    void rechazaEventosSiElPartidoNoEstaEnJuego() throws Exception {
        mvc.perform(post("/partidos/12/eventos").contentType(JSON)
                        .content("{\"jugadorId\":34,\"tipo\":\"GOL\",\"minuto\":10}"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("en juego")));
    }

    @Test
    void validaElRangoDelMinuto() throws Exception {
        mvc.perform(post("/partidos/11/eventos").contentType(JSON)
                        .content("{\"jugadorId\":93,\"tipo\":\"GOL\",\"minuto\":121}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("entre 0 y 120")));
    }

    @Test
    void maquinaDeEstados_noPermiteSaltearseNiVolverAtras() throws Exception {
        mvc.perform(patch("/partidos/1/estado").contentType(JSON).content("{\"estado\":\"EN_JUEGO\"}"))
                .andExpect(status().isConflict());
        mvc.perform(patch("/partidos/12/estado").contentType(JSON).content("{\"estado\":\"FINALIZADO\"}"))
                .andExpect(status().isConflict());
        mvc.perform(patch("/partidos/12/estado").contentType(JSON).content("{\"estado\":\"EN_JUEGO\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"estado\":\"EN_JUEGO\"")));
    }

    @Test
    void regla1_unaCanchaNoPuedeTenerDosPartidosSuperpuestos() throws Exception {
        // Cancha 4 (Brentford) ya tiene el partido 12 el 17/10 de 11:00 a 13:00.
        mvc.perform(post("/partidos").contentType(JSON).content(partido(4, 10, 1, 2)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("cancha")));
    }

    @Test
    void regla2_unArbitroNoPuedeDirigirDosPartidosALaMismaHora() throws Exception {
        // Árbitro 2 dirige el partido 12 el 17/10 de 11:00 a 13:00; la cancha 1 está libre.
        mvc.perform(post("/partidos").contentType(JSON).content(partido(1, 2, 1, 2)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("rbitro")));
    }

    @Test
    void unEquipoNoPuedeJugarContraSiMismo() throws Exception {
        mvc.perform(post("/partidos").contentType(JSON).content(partido(1, 10, 1, 1)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("mismo")));
    }

    @Test
    void regla3_elSuspendidoNoPuedeFigurarEnLaFechaSiguiente() throws Exception {
        mvc.perform(get("/torneos/1/suspendidos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bournemouth")))
                .andExpect(content().string(containsString("Hull City")));

        // Partido 16 (Bournemouth vs Sunderland, fecha 7): se inicia y se intenta cargar un evento del jugador 27.
        mvc.perform(patch("/partidos/16/estado").contentType(JSON).content("{\"estado\":\"EN_JUEGO\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/partidos/16/eventos").contentType(JSON)
                        .content("{\"jugadorId\":27,\"tipo\":\"GOL\",\"minuto\":10}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("suspendido")));
    }

    @Test
    void goleadoresYEquiposRespondenOk() throws Exception {
        mvc.perform(get("/torneos/1/goleadores")).andExpect(status().isOk());
        mvc.perform(get("/torneos/1/equipos")).andExpect(status().isOk());
        mvc.perform(get("/partidos/11/jugadores")).andExpect(status().isOk());
    }

    @Test
    void recursoInexistenteDa404ConMensaje() throws Exception {
        mvc.perform(get("/partidos/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Partido no encontrado")));
    }

    /** Partido el 17/10/2026 de 11:30 a 13:30 en la cancha/árbitro/equipos indicados. */
    private static String partido(long canchaId, long arbitroId, long localId, long visitanteId) {
        return "{\"torneoId\":1,\"nroFecha\":7,\"inicio\":\"2026-10-17T11:30:00\",\"fin\":\"2026-10-17T13:30:00\","
                + "\"canchaId\":" + canchaId + ",\"arbitroId\":" + arbitroId
                + ",\"equipoLocalId\":" + localId + ",\"equipoVisitanteId\":" + visitanteId + "}";
    }
}
