package com.example.Sisegg.repositories;

import com.example.Sisegg.models.AnexoProposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnexoPropostaRepository extends JpaRepository<AnexoProposta, Long> {
    List<AnexoProposta> findByPropostaId(Long propostaId);
}
