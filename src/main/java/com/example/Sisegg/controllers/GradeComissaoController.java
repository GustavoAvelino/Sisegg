package com.example.Sisegg.controllers;

import com.example.Sisegg.DTO.*;
import com.example.Sisegg.models.Corretora;
import com.example.Sisegg.models.GradeComissao;
import com.example.Sisegg.models.ParcelaComissao;
import com.example.Sisegg.repositories.CorretoraRepository;
import com.example.Sisegg.repositories.GradeComissaoRepository;
import com.example.Sisegg.repositories.ParcelaComissaoRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/grade-comissao")
@CrossOrigin("*")
public class GradeComissaoController {

    @Autowired
    private GradeComissaoRepository gradeComissaoRepository;

    @Autowired
    private CorretoraRepository corretoraRepository;

    @Autowired
    private ParcelaComissaoRepository parcelaComissaoRepository;

    @PostMapping("/salvar")
public ResponseEntity<String> salvarGradeComissao(@RequestBody GradeComissaoRequestDTO dto) {
    Optional<Corretora> corretoraOpt = corretoraRepository.findById(dto.corretoraId());
    if (corretoraOpt.isEmpty()) {
        return ResponseEntity.badRequest().body("Corretora não encontrada.");
    }

    // 🔹 1. Salvar a Grade de Comissão primeiro
    GradeComissao grade = new GradeComissao(dto, corretoraOpt.get());
    GradeComissao savedGrade = gradeComissaoRepository.save(grade);

    // 🔹 2. Agora, salvar as Parcelas associadas à Grade
    if (dto.parcelas() != null && !dto.parcelas().isEmpty()) {
        List<ParcelaComissao> parcelas = dto.parcelas().stream()
            .map(p -> new ParcelaComissao(
                null, 
                savedGrade, // Associando corretamente à Grade salva
                p.numeroParcela(),
                p.comissaoPercentual(),
                p.plParcelaPercentual()
            ))
            .collect(Collectors.toList());

        parcelaComissaoRepository.saveAll(parcelas);
    }

    return ResponseEntity.ok("Grade de comissão cadastrada com sucesso!");
}


    @Transactional    // 🔹 Atualizar Grade de Comissão
    @PutMapping("/atualizar/{id}")
    public ResponseEntity<String> atualizarGradeComissao(@PathVariable Long id, @RequestBody GradeComissaoRequestDTO dto) {
        Optional<GradeComissao> gradeOpt = gradeComissaoRepository.findById(id);
        if (gradeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        GradeComissao grade = gradeOpt.get();
        grade.setNome(dto.nome());
        grade.setTipoPagamento(dto.tipoPagamento());
        grade.setQuantidadeParcelas(dto.quantidadeParcelas());

        if (dto.corretoraId() != null) {
            Optional<Corretora> corretoraOpt = corretoraRepository.findById(dto.corretoraId());
            corretoraOpt.ifPresent(grade::setCorretora);
        }

        // Remover parcelas antigas antes de salvar as novas
        parcelaComissaoRepository.deleteByGradeComissao_Id(grade.getId());

        List<ParcelaComissao> novasParcelas = dto.parcelas().stream()
            .map(p -> new ParcelaComissao(
                null, // ID será gerado automaticamente
                grade, 
                p.numeroParcela(),
                p.comissaoPercentual(),
                p.plParcelaPercentual()
            ))
            .collect(Collectors.toList());

        gradeComissaoRepository.save(grade);
        parcelaComissaoRepository.saveAll(novasParcelas);

        return ResponseEntity.ok("Grade de comissão atualizada com sucesso!");
    }

    // 🔹 Buscar Parcelas por ID da Grade de Comissão
    @GetMapping("/parcelas/{gradeComissaoId}")
    public ResponseEntity<List<ParcelaComissaoResponseDTO>> getParcelasByGradeComissao(@PathVariable Long gradeComissaoId) {
        List<ParcelaComissao> parcelas = parcelaComissaoRepository.findByGradeComissaoId(gradeComissaoId);
        return ResponseEntity.ok(parcelas.stream().map(ParcelaComissaoResponseDTO::new).toList());
    }

    @Transactional  // 🔹 Deletar Grade de Comissão
    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<String> deletarGradeComissao(@PathVariable Long id) {
        Optional<GradeComissao> gradeOpt = gradeComissaoRepository.findById(id);
        if (gradeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        parcelaComissaoRepository.deleteByGradeComissao_Id(id);
        gradeComissaoRepository.deleteById(id);

        return ResponseEntity.ok("Grade de comissão deletada com sucesso!");
    }


    @GetMapping("/{corretoraId}")
    public ResponseEntity<List<GradeComissaoResponseDTO>> getGradesByCorretora(@PathVariable Long corretoraId) {
        List<GradeComissao> grades = gradeComissaoRepository.findByCorretoraId(corretoraId);
        
        if (grades.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<GradeComissaoResponseDTO> response = grades.stream()
            .map(GradeComissaoResponseDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
