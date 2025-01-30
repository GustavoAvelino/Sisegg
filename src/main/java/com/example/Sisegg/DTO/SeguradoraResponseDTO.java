package com.example.Sisegg.DTO;

import com.example.Sisegg.models.Seguradora;

public record SeguradoraResponseDTO(
    Long id,
    String nome,
    String nomefan,
    String cnpj,
    String email,
    String telefone,
    String susep,
    Double impSeguradora,
    Long corretoraId,      // adicionado
    String corretoraNome   // adicionado (caso queira exibir)
) {
    public SeguradoraResponseDTO(Seguradora seguradora) {
        this(
            seguradora.getId(),
            seguradora.getNome(),
            seguradora.getNomefan(),
            seguradora.getCnpj(),
            seguradora.getEmail(),
            seguradora.getTelefone(),
            seguradora.getSusep(),
            seguradora.getImpSeguradora(),
            seguradora.getCorretora() != null ? seguradora.getCorretora().getId() : null,
            seguradora.getCorretora() != null ? seguradora.getCorretora().getNome() : null
        );
    }
}
