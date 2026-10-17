package com.example.demo.entity;

import java.util.Set;

/**
 * Máquina de estados del partido: PROGRAMADO -> EN_JUEGO -> FINALIZADO / SUSPENDIDO.
 * Se guarda como texto (EnumType.STRING) y la base rechaza valores fuera de la lista con un CHECK.
 */
public enum EstadoPartido {
    PROGRAMADO, EN_JUEGO, FINALIZADO, SUSPENDIDO;

    public Set<EstadoPartido> siguientesPermitidos() {
        return switch (this) {
            case PROGRAMADO -> Set.of(EN_JUEGO);
            case EN_JUEGO -> Set.of(FINALIZADO, SUSPENDIDO);
            case FINALIZADO, SUSPENDIDO -> Set.of();
        };
    }

    public boolean puedePasarA(EstadoPartido destino) {
        return siguientesPermitidos().contains(destino);
    }
}
