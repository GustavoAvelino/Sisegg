package com.example.Sisegg.DTO;

import com.example.Sisegg.models.Corretora;

public record CorretoraResponseDTO(Long id, String nome, String nomefan, String estado, String cidade, String endereco, String cnpj, String email, String telefone, String susep, Double impCorretora) {

    public CorretoraResponseDTO (Corretora corretora){
        this(corretora.getId(), corretora.getNome(), corretora.getNomefan(), corretora.getEstado(), corretora.getCidade(), corretora.getEndereco(), corretora.getCnpj(), corretora.getEmail(), corretora.getTelefone(),
        corretora.getSusep(), corretora.getImpCorretora());
    }
}
