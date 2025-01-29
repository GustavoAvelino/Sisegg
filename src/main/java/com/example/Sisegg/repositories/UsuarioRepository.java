package com.example.Sisegg.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Sisegg.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Método personalizado para buscar por nome utilizando LIKE (com nomeCom)
    List<Usuario> findByNomeComContainingIgnoreCase(String nomeCom);

    Optional<Usuario> findByEmail(String email);
   
}
