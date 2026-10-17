package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "arbitro", uniqueConstraints = @UniqueConstraint(name = "uq_arbitro_dni", columnNames = "dni"))
public class Arbitro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, length = 10)
    private String dni;

    @Column(length = 20)
    private String telefono;

    protected Arbitro() {}

    public Arbitro(String nombre, String apellido, String dni, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public String getTelefono() { return telefono; }
}
