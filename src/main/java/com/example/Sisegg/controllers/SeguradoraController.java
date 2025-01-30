package com.example.Sisegg.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Sisegg.DTO.SeguradoraRequestDTO;
import com.example.Sisegg.DTO.SeguradoraResponseDTO;
import com.example.Sisegg.models.Seguradora;
import com.example.Sisegg.models.Corretora;
import com.example.Sisegg.repositories.SeguradoraRepository;
import com.example.Sisegg.repositories.CorretoraRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("seguradora")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SeguradoraController {

    @Autowired
    private SeguradoraRepository repository;

    @Autowired
    private CorretoraRepository corretoraRepository;

    // CREATE
    @PostMapping("/save")
    public ResponseEntity<String> saveSeguradora(@RequestBody SeguradoraRequestDTO data) {
        Seguradora seguradoraData = new Seguradora(data);

        // Se houver corretoraId, buscar e setar
        if (data.corretoraId() != null) {
            Optional<Corretora> corretoraOpt = corretoraRepository.findById(data.corretoraId());
            if (corretoraOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Corretora com o ID fornecido não encontrada.");
            }
            seguradoraData.setCorretora(corretoraOpt.get());
        }

        repository.save(seguradoraData);
        return ResponseEntity.status(HttpStatus.CREATED).body("Seguradora criada com sucesso!");
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<SeguradoraResponseDTO>> getAll() {
        List<Seguradora> lista = repository.findAll();
        List<SeguradoraResponseDTO> seguradoras = lista.stream()
                .map(SeguradoraResponseDTO::new)
                .toList();
        return ResponseEntity.ok(seguradoras);
    }

    // READ SINGLE
    @GetMapping("/{id}")
    public ResponseEntity<SeguradoraResponseDTO> getById(@PathVariable Long id) {
        Optional<Seguradora> seguradora = repository.findById(id);
        if (seguradora.isPresent()) {
            return ResponseEntity.ok(new SeguradoraResponseDTO(seguradora.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateSeguradora(@PathVariable Long id, @RequestBody SeguradoraRequestDTO data) {
        Optional<Seguradora> seguradoraOptional = repository.findById(id);

        if (seguradoraOptional.isPresent()) {
            Seguradora seguradora = seguradoraOptional.get();

            // Atualiza campos básicos
            seguradora.setNome(data.nome());
            seguradora.setNomefan(data.nomefan());
            seguradora.setCnpj(data.cnpj());
            seguradora.setEmail(data.email());
            seguradora.setTelefone(data.telefone());
            seguradora.setSusep(data.susep());
            seguradora.setImpSeguradora(data.impSeguradora());

            // Se vier corretoraId, vincula a corretora
            if (data.corretoraId() != null) {
                Optional<Corretora> corretoraOpt = corretoraRepository.findById(data.corretoraId());
                if (corretoraOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Corretora com o ID fornecido não encontrada.");
                }
                seguradora.setCorretora(corretoraOpt.get());
            }

            repository.save(seguradora);
            return ResponseEntity.status(HttpStatus.OK).body("Seguradora atualizada com sucesso!");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Seguradora não encontrada.");
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteSeguradora(@PathVariable Long id) {
        Optional<Seguradora> seguradora = repository.findById(id);
        if (seguradora.isPresent()) {
            repository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // SEARCH
    @GetMapping("/search")
    public ResponseEntity<List<SeguradoraResponseDTO>> searchSeguradora(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) Long corretoraId // Novo campo
    ) {
        List<Seguradora> seguradoras;

        // Filtro por ID
        if (id != null) {
            Optional<Seguradora> seguradora = repository.findById(id);
            if (seguradora.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            seguradoras = List.of(seguradora.get());
        }
        // Filtro por 'descricao' (nome ou nomefan)
        else if (descricao != null && !descricao.isEmpty()) {
            seguradoras = repository.findByNomeContainingIgnoreCaseOrNomefanContainingIgnoreCase(descricao, descricao);
            if (seguradoras.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        // Filtro por cnpj
        else if (cnpj != null && !cnpj.isEmpty()) {
            seguradoras = repository.findByCnpjContainingIgnoreCase(cnpj);
            if (seguradoras.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        // Sem filtro, retorna todas
        else {
            seguradoras = repository.findAll();
        }

        // Filtro por corretora, se veio
        if (corretoraId != null) {
            seguradoras = seguradoras.stream()
                    .filter(s -> s.getCorretora() != null && s.getCorretora().getId().equals(corretoraId))
                    .toList();
            if (seguradoras.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        List<SeguradoraResponseDTO> seguradorasDTO = seguradoras.stream()
                .map(SeguradoraResponseDTO::new)
                .toList();
        return ResponseEntity.ok(seguradorasDTO);
    }
}
