package com.example.Sisegg.repositories;

import com.example.Sisegg.models.ParcelaComissao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParcelaComissaoRepository extends JpaRepository<ParcelaComissao, Long> {
    List<ParcelaComissao> findByGradeComissaoId(Long gradeComissaoId);
    
    // Método correto para deletar todas as parcelas de uma grade específica
    void deleteByGradeComissao_Id(Long gradeComissaoId);
}
