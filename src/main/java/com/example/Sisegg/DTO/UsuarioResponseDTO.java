package com.example.Sisegg.DTO;

import com.example.Sisegg.models.Usuario;

public record UsuarioResponseDTO(Long id, String nomeCom, String email, Long corretoraId, Long produtorId) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(),
             usuario.getNomeCom(),
             usuario.getEmail(),
             usuario.getCorretora() != null ? usuario.getCorretora().getId() : null,
             usuario.getProdutor() != null ? usuario.getProdutor().getId() : null);
    }
}
