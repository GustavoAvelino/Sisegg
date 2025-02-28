package com.example.Sisegg.DTO;

import com.example.Sisegg.models.Cliente;

public record ClienteResponseDTO(
    Long id, 
    String cnpjCpf, 
    String nome, 
    String nomeSocial, 
    String sexo, 
    String dataNascimento, 
    String estadoCivil, 
    String email, 
    String telefone,
    Long corretoraId,
    String corretoraNome
) {
    public ClienteResponseDTO(Cliente cliente) {
        this(cliente.getId(), cliente.getCnpjCpf(), cliente.getNome(), cliente.getNomeSocial(),
             cliente.getSexo(), cliente.getDataNascimento(), cliente.getEstadoCivil(), cliente.getEmail(),
             cliente.getTelefone(),
             cliente.getCorretora() != null ? cliente.getCorretora().getId() : null,
             cliente.getCorretora() != null ? cliente.getCorretora().getNome() : null);
    }
}
