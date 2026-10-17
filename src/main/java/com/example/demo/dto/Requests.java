package com.example.demo.dto;

import com.example.demo.entity.EstadoPartido;
import com.example.demo.entity.TipoEvento;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/** Cuerpos de las peticiones, con validaciones de formato (Bean Validation). */
public final class Requests {
    private Requests() {}

    public record NuevoEvento(
            @NotNull(message = "jugadorId es obligatorio") Long jugadorId,
            @NotNull(message = "tipo es obligatorio") TipoEvento tipo,
            @NotNull(message = "minuto es obligatorio")
            @Min(value = 0, message = "El minuto debe estar entre 0 y 120")
            @Max(value = 120, message = "El minuto debe estar entre 0 y 120") Integer minuto) {}

    public record CambioEstado(@NotNull(message = "estado es obligatorio") EstadoPartido estado) {}

    public record NuevoTorneo(
            @NotBlank @Size(max = 50) String nombre,
            @NotBlank @Size(max = 20) String temporada,
            @NotNull LocalDateTime fechaHoraInicio,
            @NotNull LocalDateTime fechaHoraFin) {}

    public record NuevoEquipo(
            @NotNull Long torneoId,
            @NotBlank @Size(max = 50) String nombre,
            @Size(max = 50) String barrio,
            @Size(max = 20) String colorCamiseta,
            @Size(max = 50) String delegado) {}

    public record NuevoJugador(
            @NotNull Long equipoId,
            @NotBlank @Size(max = 50) String nombre,
            @NotBlank @Size(max = 50) String apellido,
            @NotBlank @Size(max = 10) String dni,
            @NotNull @Min(value = 1, message = "La camiseta debe estar entre 1 y 99")
            @Max(value = 99, message = "La camiseta debe estar entre 1 y 99") Integer numeroCamiseta) {}

    public record NuevaCancha(
            @NotBlank @Size(max = 50) String nombre,
            @NotBlank @Size(max = 100) String direccion,
            @Size(max = 50) String barrio,
            @Size(max = 20) String superficie) {}

    public record NuevoArbitro(
            @NotBlank @Size(max = 50) String nombre,
            @NotBlank @Size(max = 50) String apellido,
            @NotBlank @Size(max = 10) String dni,
            @Size(max = 20) String telefono) {}

    /** Para programar un partido (POST) o reprogramarlo (PUT: equipos y torneo se ignoran). */
    public record DatosPartido(
            @NotNull Long torneoId,
            @NotNull @Min(value = 1, message = "La fecha debe estar entre 1 y 38")
            @Max(value = 38, message = "La fecha debe estar entre 1 y 38") Integer nroFecha,
            @NotNull LocalDateTime inicio,
            @NotNull LocalDateTime fin,
            @NotNull Long canchaId,
            @NotNull Long arbitroId,
            @NotNull Long equipoLocalId,
            @NotNull Long equipoVisitanteId) {}
}
