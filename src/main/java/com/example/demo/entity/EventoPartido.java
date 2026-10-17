package com.example.demo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** Detalle del partido: goles y tarjetas. Si se borra el partido, se borran sus eventos (ON DELETE CASCADE). */
@Entity
@Table(name = "evento_partido",
        check = {
                @CheckConstraint(name = "ck_evento_tipo", constraint = "tipo IN ('GOL','GOL_EN_CONTRA','AMARILLA','ROJA')"),
                @CheckConstraint(name = "ck_evento_minuto", constraint = "minuto BETWEEN 0 AND 120")
        })
public class EventoPartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partido_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Partido partido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEvento tipo;

    @Column(nullable = false)
    private Integer minuto;

    protected EventoPartido() {}

    public EventoPartido(Partido partido, Jugador jugador, TipoEvento tipo, Integer minuto) {
        this.partido = partido;
        this.jugador = jugador;
        this.tipo = tipo;
        this.minuto = minuto;
    }

    public Long getId() { return id; }
    public Partido getPartido() { return partido; }
    public Jugador getJugador() { return jugador; }
    public TipoEvento getTipo() { return tipo; }
    public Integer getMinuto() { return minuto; }
}
