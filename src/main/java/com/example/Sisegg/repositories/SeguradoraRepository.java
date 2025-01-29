package com.example.Sisegg.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Sisegg.models.Seguradora;

public interface SeguradoraRepository extends JpaRepository<Seguradora, Long> {
    List<Seguradora> findByNomeContainingIgnoreCaseOrNomefanContainingIgnoreCase(String nome, String nomefan);

    List<Seguradora> findByCnpjContainingIgnoreCase(String cnpj); // Para busca por CNPJ
}