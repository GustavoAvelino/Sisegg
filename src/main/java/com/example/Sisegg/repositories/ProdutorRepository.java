package com.example.Sisegg.repositories;

import com.example.Sisegg.models.Produtor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdutorRepository extends JpaRepository<Produtor, Long> {
    List<Produtor> findByNomeContainingIgnoreCase(String nome);
    List<Produtor> findByCpf(String cpf);
    List<Produtor> findByCnpj(String cnpj);
}
