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
    Corretora corretora
) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNomeCom(), usuario.getEmail(), usuario.getSenha(), 
             usuario.getConfSenha(), usuario.getRole(), usuario.getCorretora());
    }
}
