package com.example.Sisegg.DTO;


public record UsuarioRequestDTO(Long id, String nomeCom, String email, String senha, String confSenha, int role, Long corretoraId) { }