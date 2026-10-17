package com.example.demo.service;

import com.example.demo.dto.Dtos.EventoDto;
import com.example.demo.dto.Dtos.JugadorDto;
import com.example.demo.entity.EventoPartido;
import com.example.demo.entity.Jugador;
import com.example.demo.entity.Lado;
import com.example.demo.entity.Partido;
import com.example.demo.entity.TipoEvento;

/** Conversiones entidad -> DTO y cálculos derivados de un evento. */
final class Mapeo {
    private Mapeo() {}

    static JugadorDto aDto(Jugador j) {
        return new JugadorDto(j.getId(), j.getNombre(), j.getApellido(), j.getNumeroCamiseta());
    }

    /** LOCAL/VISITANTE según el equipo al que pertenece el jugador del evento. */
    static Lado ladoDe(Partido partido, Jugador jugador) {
        return jugador.getEquipo().getId().equals(partido.getEquipoLocal().getId()) ? Lado.LOCAL : Lado.VISITANTE;
    }

    static EventoDto aDto(EventoPartido e) {
        Partido p = e.getPartido();
        return new EventoDto(e.getId(), p.getId(), e.getTipo(), e.getMinuto(), ladoDe(p, e.getJugador()), aDto(e.getJugador()));
    }

    /** Un GOL suma al equipo del jugador; un GOL_EN_CONTRA suma al rival. Las tarjetas no suman. */
    static Lado ladoQueSumaGol(Partido partido, EventoPartido e) {
        if (e.getTipo() != TipoEvento.GOL && e.getTipo() != TipoEvento.GOL_EN_CONTRA) return null;
        Lado lado = ladoDe(partido, e.getJugador());
        if (e.getTipo() == TipoEvento.GOL) return lado;
        return lado == Lado.LOCAL ? Lado.VISITANTE : Lado.LOCAL;
    }
}
