package com.example.Sisegg.DTO;

import com.example.Sisegg.models.Seguradora;

public record SeguradoraResponseDTO(Long id, String nome, String nomefan, String cnpj, String email, String telefone, String susep, Double impSeguradora) {

    public SeguradoraResponseDTO(Seguradora seguradora) {
        this(seguradora.getId(), seguradora.getNome(), seguradora.getNomefan(), seguradora.getCnpj(), seguradora.getEmail(), seguradora.getTelefone(), seguradora.getSusep(), seguradora.getImpSeguradora());
    }
}
