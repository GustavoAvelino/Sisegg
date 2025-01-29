package com.example.Sisegg.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Sisegg.models.Corretora;

public interface CorretoraRepository extends JpaRepository<Corretora, Long> {
    List<Corretora> findByNomeContainingIgnoreCaseOrNomefanContainingIgnoreCase(String nome, String nomefan);

    List<Corretora> findByCnpjContainingIgnoreCase(String cnpj); // Adicionado para busca por CNPJ
}
