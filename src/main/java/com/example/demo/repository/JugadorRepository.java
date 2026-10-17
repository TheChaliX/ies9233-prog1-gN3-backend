package com.example.demo.repository;

import com.example.demo.entity.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    List<Jugador> findByEquipoIdOrderByNumeroCamiseta(Long equipoId);
    boolean existsByDni(String dni);
    boolean existsByEquipoIdAndNumeroCamiseta(Long equipoId, Integer numeroCamiseta);
}
