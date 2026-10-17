package com.example.demo.controller;

import com.example.demo.dto.Dtos.*;
import com.example.demo.dto.Requests.NuevoTorneo;
import com.example.demo.service.CatalogoService;
import com.example.demo.service.PartidoService;
import com.example.demo.service.ReporteService;
import com.example.demo.service.SuspensionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/torneos")
public class TorneoController {

    private final CatalogoService catalogo;
    private final PartidoService partidos;
    private final ReporteService reportes;
    private final SuspensionService suspensiones;

    public TorneoController(CatalogoService catalogo, PartidoService partidos,
                            ReporteService reportes, SuspensionService suspensiones) {
        this.catalogo = catalogo;
        this.partidos = partidos;
        this.reportes = reportes;
        this.suspensiones = suspensiones;
    }

    @GetMapping
    public List<TorneoDto> listar() { return catalogo.torneos(); }

    @GetMapping("/{id}")
    public TorneoDto obtener(@PathVariable Long id) { return catalogo.torneo(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TorneoDto crear(@Valid @RequestBody NuevoTorneo n) { return catalogo.crearTorneo(n); }

    @GetMapping("/{id}/equipos")
    public List<EquipoDto> equipos(@PathVariable Long id) { return catalogo.equiposDelTorneo(id); }

    @GetMapping("/{id}/partidos")
    public List<PartidoDto> partidos(@PathVariable Long id) { return partidos.listarPorTorneo(id); }

    // ---- reportes
    @GetMapping("/{id}/posiciones")
    public List<PosicionDto> posiciones(@PathVariable Long id) { return reportes.posiciones(id); }

    @GetMapping("/{id}/goleadores")
    public List<GoleadorDto> goleadores(@PathVariable Long id) { return reportes.goleadores(id); }

    /** ?fecha=N para una fecha puntual; sin parámetro, la fecha en curso del torneo. */
    @GetMapping("/{id}/suspendidos")
    public List<SuspendidoDto> suspendidos(@PathVariable Long id, @RequestParam(required = false) Integer fecha) {
        return suspensiones.suspendidos(id, fecha);
    }
}
