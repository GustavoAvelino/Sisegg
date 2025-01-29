package com.example.Sisegg.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Sisegg.DTO.SeguradoraRequestDTO;
import com.example.Sisegg.DTO.SeguradoraResponseDTO;
import com.example.Sisegg.models.Seguradora;
import com.example.Sisegg.repositories.SeguradoraRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("seguradora")
public class SeguradoraController {

    @Autowired
    private SeguradoraRepository repository;

    // Create
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/save")
    public ResponseEntity<Void> saveSeguradora(@RequestBody SeguradoraRequestDTO data) {
        Seguradora seguradoraData = new Seguradora(data);
        repository.save(seguradoraData);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Read all
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping
    public List<SeguradoraResponseDTO> getAll() {
        List<SeguradoraResponseDTO> seguradoras = repository.findAll().stream().map(SeguradoraResponseDTO::new).toList();
        return seguradoras;
    }

    // Read single
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/{id}")
    public ResponseEntity<SeguradoraResponseDTO> getById(@PathVariable Long id) {
        Optional<Seguradora> seguradora = repository.findById(id);
        if (seguradora.isPresent()) {
            return ResponseEntity.ok(new SeguradoraResponseDTO(seguradora.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Update
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateSeguradora(@PathVariable Long id, @RequestBody SeguradoraRequestDTO data) {
        Optional<Seguradora> seguradoraOptional = repository.findById(id);

        if (seguradoraOptional.isPresent()) {
            Seguradora seguradora = seguradoraOptional.get();
            // Atualizando os campos
            seguradora.setNome(data.nome());
            seguradora.setNomefan(data.nomefan());
            seguradora.setCnpj(data.cnpj());
            seguradora.setEmail(data.email());
            seguradora.setTelefone(data.telefone());
            seguradora.setSusep(data.susep());
            seguradora.setImpSeguradora(data.impSeguradora());

            repository.save(seguradora); // Salva as alterações no banco de dados
            return ResponseEntity.status(HttpStatus.OK).build();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Delete
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteSeguradora(@PathVariable Long id) {
        Optional<Seguradora> seguradora = repository.findById(id);

        if (seguradora.isPresent()) {
            repository.deleteById(id); // Exclui a seguradora
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // Retorna 204 (No Content)
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Retorna 404 (Not Found) se não encontrar
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/search")
    public ResponseEntity<List<SeguradoraResponseDTO>> searchSeguradora(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String cnpj
    ) {
        List<Seguradora> seguradoras;

        if (id != null) {
            Optional<Seguradora> seguradora = repository.findById(id);
            return seguradora.map(value -> ResponseEntity.ok(List.of(new SeguradoraResponseDTO(value))))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } else if (descricao != null && !descricao.isEmpty()) {
            seguradoras = repository.findByNomeContainingIgnoreCaseOrNomefanContainingIgnoreCase(descricao, descricao);
        } else if (cnpj != null && !cnpj.isEmpty()) {
            seguradoras = repository.findByCnpjContainingIgnoreCase(cnpj);
        } else {
            seguradoras = repository.findAll();
        }

        if (seguradoras.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<SeguradoraResponseDTO> seguradorasDTO = seguradoras.stream()
                .map(SeguradoraResponseDTO::new)
                .toList();
        return ResponseEntity.ok(seguradorasDTO);
    }

}
