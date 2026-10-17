package com.example.demo.dto;

import com.example.demo.entity.EstadoPartido;
import com.example.demo.entity.Lado;
import com.example.demo.entity.TipoEvento;
import java.time.LocalDateTime;
import java.util.List;

/** Respuestas de la API. Los nombres de campo coinciden con los tipos de TypeScript del frontend. */
public final class Dtos {
    private Dtos() {}

    public record PartidoDto(Long id, Integer fecha, LocalDateTime horario, String local, String visitante,
                             int golesLocal, int golesVisitante, EstadoPartido estado, String cancha) {}

    public record JugadorDto(Long id, String nombre, String apellido, Integer numeroCamiseta) {}

    public record EventoDto(Long id, Long partidoId, TipoEvento tipo, Integer minuto, Lado lado, JugadorDto jugador) {}

    public record PlantelDto(List<JugadorDto> local, List<JugadorDto> visitante) {}

    public record PosicionDto(int posicion, String equipo, int pj, int pg, int pe, int pp,
                              int gf, int gc, int dg, int pts) {}

    public record GoleadorDto(int posicion, Long jugadorId, String nombre, String apellido, String equipo, int goles) {}

    public record SuspendidoDto(Long jugadorId, String nombre, String apellido, String equipo,
                                String motivo, int fechaSuspension) {}

    public record TorneoDto(Long id, String nombre, String temporada,
                            LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {}

    public record EquipoDto(Long id, Long torneoId, String nombre, String barrio, String colorCamiseta, String delegado) {}

    public record CanchaDto(Long id, String nombre, String direccion, String barrio, String superficie) {}

    public record ArbitroDto(Long id, String nombre, String apellido, String dni, String telefono) {}

    public record ErrorDto(int estado, String mensaje) {}
}
