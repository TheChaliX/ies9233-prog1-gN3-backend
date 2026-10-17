package com.example.demo.repository;

import com.example.demo.entity.Cancha;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanchaRepository extends JpaRepository<Cancha, Long> {
    boolean existsByNombre(String nombre);
}
