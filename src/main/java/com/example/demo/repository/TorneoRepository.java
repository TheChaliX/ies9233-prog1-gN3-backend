package com.example.demo.repository;

import com.example.demo.entity.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    boolean existsByNombreAndTemporada(String nombre, String temporada);
}
