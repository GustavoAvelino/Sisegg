package com.example.Sisegg.DTO;

import com.example.Sisegg.models.Corretora;
import com.example.Sisegg.models.Usuario;

public record UsuarioResponseDTO(
    Long id,
    String nomeCom,
    String email,
    String senha,
    String confSenha,
    int role,
    Long corretoraId,
    String corretoraNome
) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(
            usuario.getId(),
            usuario.getNomeCom(),
            usuario.getEmail(),
            usuario.getSenha(),
            usuario.getConfSenha(),
            usuario.getRole(),
            usuario.getCorretora() != null ? usuario.getCorretora().getId() : null,
            usuario.getCorretora() != null ? usuario.getCorretora().getNome() : null
        );
    }
}
