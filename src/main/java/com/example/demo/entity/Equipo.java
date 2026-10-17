package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipo",
        uniqueConstraints = @UniqueConstraint(name = "uq_equipo_torneo_nombre", columnNames = {"torneo_id", "nombre"}))
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 50)
    private String barrio;

    @Column(name = "color_camiseta", length = 20)
    private String colorCamiseta;

    @Column(length = 50)
    private String delegado;

    protected Equipo() {}

    public Equipo(Torneo torneo, String nombre, String barrio, String colorCamiseta, String delegado) {
        this.torneo = torneo;
        this.nombre = nombre;
        this.barrio = barrio;
        this.colorCamiseta = colorCamiseta;
        this.delegado = delegado;
    }

    public Long getId() { return id; }
    public Torneo getTorneo() { return torneo; }
    public String getNombre() { return nombre; }
    public String getBarrio() { return barrio; }
    public String getColorCamiseta() { return colorCamiseta; }
    public String getDelegado() { return delegado; }
}
