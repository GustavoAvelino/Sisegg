package com.example.Sisegg.repositories;

import com.example.Sisegg.models.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    List<Veiculo> findByPlacaContainingIgnoreCase(String placa);
    List<Veiculo> findByModeloContainingIgnoreCase(String modelo);
    List<Veiculo> findByPlacaContainingIgnoreCaseAndModeloContainingIgnoreCase(String placa, String modelo);

    //Pesquisa por clienteId
    List<Veiculo> findByClienteId(Long clienteId);

    //Pesquisa combinada
    List<Veiculo> findByPlacaContainingIgnoreCaseAndModeloContainingIgnoreCaseAndClienteId(
        String placa, String modelo, Long clienteId);

        
 
 
}
