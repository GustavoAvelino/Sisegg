package com.example.Sisegg.DTO;

public record ProdutorRequestDTO(
    String nome,
    String cpf,
    String cnpj,
    String dataNascimento,
    String sexo,
    String email,
    String telefone,
    String endereco,
    Double imposto,
    Double repasse,
    String repasseSobre,
    String formaRepasse,
    Long corretoraId
) {}
