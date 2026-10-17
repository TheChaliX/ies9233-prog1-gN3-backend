package com.example.demo.repository;

import com.example.demo.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    List<Equipo> findByTorneoIdOrderByNombre(Long torneoId);
    boolean existsByTorneoIdAndNombre(Long torneoId, String nombre);
}
