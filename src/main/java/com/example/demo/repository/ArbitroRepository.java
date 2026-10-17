package com.example.demo.repository;

import com.example.demo.entity.Arbitro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArbitroRepository extends JpaRepository<Arbitro, Long> {
    boolean existsByDni(String dni);
}
