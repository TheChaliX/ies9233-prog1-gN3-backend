package com.example.demo.controller;

import com.example.demo.dto.Dtos.*;
import com.example.demo.dto.Requests.CambioEstado;
import com.example.demo.dto.Requests.DatosPartido;
import com.example.demo.dto.Requests.NuevoEvento;
import com.example.demo.service.EventoService;
import com.example.demo.service.PartidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/partidos")
public class PartidoController {

    private final PartidoService partidos;
    private final EventoService eventos;

    public PartidoController(PartidoService partidos, EventoService eventos) {
        this.partidos = partidos;
        this.eventos = eventos;
    }

    @GetMapping("/{id}")
    public PartidoDto obtener(@PathVariable Long id) { return partidos.obtener(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartidoDto programar(@Valid @RequestBody DatosPartido d) { return partidos.programar(d); }

    @PutMapping("/{id}")
    public PartidoDto reprogramar(@PathVariable Long id, @Valid @RequestBody DatosPartido d) {
        return partidos.reprogramar(id, d);
    }

    @PatchMapping("/{id}/estado")
    public PartidoDto cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstado c) {
        return partidos.cambiarEstado(id, c);
    }

    @GetMapping("/{id}/jugadores")
    public PlantelDto plantel(@PathVariable Long id) { return eventos.plantel(id); }

    @GetMapping("/{id}/eventos")
    public List<EventoDto> eventos(@PathVariable Long id) { return eventos.listar(id); }

    @PostMapping("/{id}/eventos")
    @ResponseStatus(HttpStatus.CREATED)
    public EventoDto crearEvento(@PathVariable Long id, @Valid @RequestBody NuevoEvento n) {
        return eventos.crear(id, n);
    }

    @DeleteMapping("/{id}/eventos/{eventoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarEvento(@PathVariable Long id, @PathVariable Long eventoId) {
        eventos.eliminar(id, eventoId);
    }
}
