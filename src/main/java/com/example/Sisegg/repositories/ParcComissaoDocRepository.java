package com.example.Sisegg.repositories;

import com.example.Sisegg.models.ParcComissaoDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParcComissaoDocRepository extends JpaRepository<ParcComissaoDoc, Long> {

    @Query("SELECT p FROM ParcComissaoDoc p WHERE p.proposta.id = :propostaId")
    List<ParcComissaoDoc> findByPropostaId(@Param("propostaId") Long propostaId);

    void deleteByPropostaId(Long propostaId);
}
