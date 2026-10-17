package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "torneo",
        uniqueConstraints = @UniqueConstraint(name = "uq_torneo_nombre_temporada", columnNames = {"nombre", "temporada"}),
        check = @CheckConstraint(name = "ck_torneo_fechas", constraint = "fecha_hora_fin > fecha_hora_inicio"))
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String temporada;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    protected Torneo() {}

    public Torneo(String nombre, String temporada, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        this.nombre = nombre;
        this.temporada = temporada;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTemporada() { return temporada; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
}
