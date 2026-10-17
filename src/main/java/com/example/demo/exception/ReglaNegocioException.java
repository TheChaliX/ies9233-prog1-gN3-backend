package com.example.demo.exception;

import org.springframework.http.HttpStatus;

/** Se viola una regla de negocio del backend. Por defecto 422; 409 para conflictos con otros registros. */
public class ReglaNegocioException extends RuntimeException {
    private final HttpStatus estado;

    public ReglaNegocioException(String mensaje) { this(mensaje, HttpStatus.UNPROCESSABLE_ENTITY); }

    public ReglaNegocioException(String mensaje, HttpStatus estado) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() { return estado; }

    public static ReglaNegocioException conflicto(String mensaje) {
        return new ReglaNegocioException(mensaje, HttpStatus.CONFLICT);
    }
}
