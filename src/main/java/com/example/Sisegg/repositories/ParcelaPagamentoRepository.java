package com.example.Sisegg.repositories;

import com.example.Sisegg.models.ParcelaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelaPagamentoRepository extends JpaRepository<ParcelaPagamento, Long> {
    List<ParcelaPagamento> findByPropostaId(Long propostaId);
}
