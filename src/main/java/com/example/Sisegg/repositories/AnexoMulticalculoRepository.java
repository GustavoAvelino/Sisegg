package com.example.Sisegg.repositories;

import com.example.Sisegg.models.AnexoMulticalculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnexoMulticalculoRepository extends JpaRepository<AnexoMulticalculo, Long> {
    List<AnexoMulticalculo> findByMulticalculoId(Long multicalculoId);
}
