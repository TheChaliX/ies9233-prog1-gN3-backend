package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "jugador",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_jugador_dni", columnNames = "dni"),
                @UniqueConstraint(name = "uq_jugador_equipo_camiseta", columnNames = {"equipo_id", "numero_camiseta"})
        },
        check = @CheckConstraint(name = "ck_jugador_camiseta", constraint = "numero_camiseta BETWEEN 1 AND 99"))
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, length = 10)
    private String dni;

    @Column(name = "numero_camiseta", nullable = false)
    private Integer numeroCamiseta;

    protected Jugador() {}

    public Jugador(Equipo equipo, String nombre, String apellido, String dni, Integer numeroCamiseta) {
        this.equipo = equipo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.numeroCamiseta = numeroCamiseta;
    }

    public Long getId() { return id; }
    public Equipo getEquipo() { return equipo; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public Integer getNumeroCamiseta() { return numeroCamiseta; }
}
