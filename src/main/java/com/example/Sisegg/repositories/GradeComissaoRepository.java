package com.example.Sisegg.repositories;

import com.example.Sisegg.models.GradeComissao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GradeComissaoRepository extends JpaRepository<GradeComissao, Long> {
    List<GradeComissao> findByCorretoraId(Long corretoraId);
}
