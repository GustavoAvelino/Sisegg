package com.example.Sisegg.DTO;

public record SeguradoraRequestDTO(
    String nome,
    String nomefan,
    String cnpj,
    String email,
    String telefone,
    String susep,
    Double impSeguradora,
    Long corretoraId // <--- Campo adicionado para vincular a corretora
) {
    // Pode deixar assim, sem nenhum método adicional
}
