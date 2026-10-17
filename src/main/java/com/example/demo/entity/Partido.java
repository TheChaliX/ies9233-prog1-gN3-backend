package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Cabecera del par cabecera-detalle (el detalle es EventoPartido).
 * El resultado NO se guarda: se calcula a partir de los eventos, así nunca puede quedar inconsistente.
 */
@Entity
@Table(name = "partido",
        uniqueConstraints = @UniqueConstraint(name = "uq_partido_torneo_local_visitante",
                columnNames = {"torneo_id", "equipo_local_id", "equipo_visitante_id"}),
        check = {
                @CheckConstraint(name = "ck_partido_horario", constraint = "fin > inicio"),
                @CheckConstraint(name = "ck_partido_equipos_distintos", constraint = "equipo_local_id <> equipo_visitante_id"),
                @CheckConstraint(name = "ck_partido_nro_fecha", constraint = "nro_fecha BETWEEN 1 AND 38"),
                @CheckConstraint(name = "ck_partido_estado",
                        constraint = "estado IN ('PROGRAMADO','EN_JUEGO','FINALIZADO','SUSPENDIDO')")
        })
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @Column(name = "nro_fecha", nullable = false)
    private Integer nroFecha;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cancha_id", nullable = false)
    private Cancha cancha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arbitro_id", nullable = false)
    private Arbitro arbitro;

    // Dos relaciones con Equipo: dos columnas FK distintas.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_local_id", nullable = false)
    private Equipo equipoLocal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_visitante_id", nullable = false)
    private Equipo equipoVisitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPartido estado = EstadoPartido.PROGRAMADO;

    protected Partido() {}

    public Partido(Torneo torneo, Integer nroFecha, LocalDateTime inicio, LocalDateTime fin,
                   Cancha cancha, Arbitro arbitro, Equipo equipoLocal, Equipo equipoVisitante) {
        this.torneo = torneo;
        this.nroFecha = nroFecha;
        this.inicio = inicio;
        this.fin = fin;
        this.cancha = cancha;
        this.arbitro = arbitro;
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
    }

    /** Reprogramación: cambia cuándo y dónde se juega. */
    public void reprogramar(Integer nroFecha, LocalDateTime inicio, LocalDateTime fin, Cancha cancha, Arbitro arbitro) {
        this.nroFecha = nroFecha;
        this.inicio = inicio;
        this.fin = fin;
        this.cancha = cancha;
        this.arbitro = arbitro;
    }

    public Long getId() { return id; }
    public Torneo getTorneo() { return torneo; }
    public Integer getNroFecha() { return nroFecha; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFin() { return fin; }
    public Cancha getCancha() { return cancha; }
    public Arbitro getArbitro() { return arbitro; }
    public Equipo getEquipoLocal() { return equipoLocal; }
    public Equipo getEquipoVisitante() { return equipoVisitante; }
    public EstadoPartido getEstado() { return estado; }
    public void setEstado(EstadoPartido estado) { this.estado = estado; }
}
