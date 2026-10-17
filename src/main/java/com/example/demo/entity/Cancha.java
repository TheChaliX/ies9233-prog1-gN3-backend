package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cancha", uniqueConstraints = @UniqueConstraint(name = "uq_cancha_nombre", columnNames = "nombre"))
public class Cancha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String direccion;

    @Column(length = 50)
    private String barrio;

    @Column(length = 20)
    private String superficie;

    protected Cancha() {}

    public Cancha(String nombre, String direccion, String barrio, String superficie) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.barrio = barrio;
        this.superficie = superficie;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getBarrio() { return barrio; }
    public String getSuperficie() { return superficie; }
}
