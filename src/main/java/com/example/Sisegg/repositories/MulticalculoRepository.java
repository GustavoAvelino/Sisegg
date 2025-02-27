package com.example.Sisegg.repositories;

import com.example.Sisegg.models.Multicalculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MulticalculoRepository extends JpaRepository<Multicalculo, Long> {
    List<Multicalculo> findByCorretoraId(Long corretoraId);
}
