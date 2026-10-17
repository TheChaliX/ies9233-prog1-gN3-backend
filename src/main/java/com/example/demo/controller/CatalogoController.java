package com.example.demo.controller;

import com.example.demo.dto.Dtos.*;
import com.example.demo.dto.Requests.*;
import com.example.demo.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Equipos, jugadores, canchas y árbitros. */
@RestController
public class CatalogoController {

    private final CatalogoService catalogo;

    public CatalogoController(CatalogoService catalogo) { this.catalogo = catalogo; }

    @PostMapping("/equipos")
    @ResponseStatus(HttpStatus.CREATED)
    public EquipoDto crearEquipo(@Valid @RequestBody NuevoEquipo n) { return catalogo.crearEquipo(n); }

    @GetMapping("/equipos/{id}/jugadores")
    public List<JugadorDto> jugadores(@PathVariable Long id) { return catalogo.jugadoresDelEquipo(id); }

    @PostMapping("/jugadores")
    @ResponseStatus(HttpStatus.CREATED)
    public JugadorDto crearJugador(@Valid @RequestBody NuevoJugador n) { return catalogo.crearJugador(n); }

    @GetMapping("/canchas")
    public List<CanchaDto> canchas() { return catalogo.canchas(); }

    @PostMapping("/canchas")
    @ResponseStatus(HttpStatus.CREATED)
    public CanchaDto crearCancha(@Valid @RequestBody NuevaCancha n) { return catalogo.crearCancha(n); }

    @GetMapping("/arbitros")
    public List<ArbitroDto> arbitros() { return catalogo.arbitros(); }

    @PostMapping("/arbitros")
    @ResponseStatus(HttpStatus.CREATED)
    public ArbitroDto crearArbitro(@Valid @RequestBody NuevoArbitro n) { return catalogo.crearArbitro(n); }
}
