package com.example.Sisegg.repositories;

import com.example.Sisegg.models.PropostaApolice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PropostaApoliceRepository extends JpaRepository<PropostaApolice, Long> {
    
    List<PropostaApolice> findByCorretoraId(Long corretoraId);

    Optional<PropostaApolice> findByNrProposta(String nrProposta);

    Optional<PropostaApolice> findByNrApolice(String nrApolice);

    Optional<PropostaApolice> findByNrEndosso(String nrEndosso);

    boolean existsByNrProposta(String nrProposta);

    boolean existsByNrApolice(String nrApolice);

    @Query("SELECT p FROM PropostaApolice p WHERE p.status NOT IN ('CANCELADA', 'RECUSADA')")
    List<PropostaApolice> findPropostasAtivas();

    List<PropostaApolice> findByStatusIgnoreCase(String status);


}
