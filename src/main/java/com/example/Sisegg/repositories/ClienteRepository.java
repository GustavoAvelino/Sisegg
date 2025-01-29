package com.example.Sisegg.repositories;

import com.example.Sisegg.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByNomeContainingIgnoreCaseOrNomeSocialContainingIgnoreCase(String nome, String nomeSocial);

    List<Cliente> findByCnpjCpf(String cnpjCpf);
}
