package com.example.Sisegg.DTO;

public record ClienteRequestDTO(
    String cnpjCpf, 
    String nome, 
    String nomeSocial, 
    String sexo, 
    String dataNascimento, 
    String estadoCivil, 
    String email, 
    String telefone,
    Long corretoraId // ID da corretora vinculada ao cliente
) {}
