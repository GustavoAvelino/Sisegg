package com.example.Sisegg.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Sisegg.DTO.CorretoraRequestDTO;
import com.example.Sisegg.DTO.CorretoraResponseDTO;
import com.example.Sisegg.models.Corretora;
import com.example.Sisegg.repositories.CorretoraRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("corretora")
public class CorretoraController {

    @Autowired
    private CorretoraRepository repository;

    // Create
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/save")
    public ResponseEntity<CorretoraResponseDTO> saveCorretora(@RequestBody CorretoraRequestDTO data) {
        Corretora corretoraData = new Corretora(data);
        corretoraData = repository.save(corretoraData);
        // Retorna o objeto criado como resposta JSON para evitar o erro de JSON vazio
        return ResponseEntity.status(HttpStatus.CREATED).body(new CorretoraResponseDTO(corretoraData));
    }

    // Read all
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping
    public List<CorretoraResponseDTO> getAll(){
        List<CorretoraResponseDTO> corretoras = repository.findAll().stream().map(CorretoraResponseDTO::new).toList();
        return corretoras;
    }

    // Read single
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/{id}")
    public ResponseEntity<CorretoraResponseDTO> getById(@PathVariable Long id){
        Optional<Corretora> corretora = repository.findById(id);
        if (corretora.isPresent()) {
            return ResponseEntity.ok(new CorretoraResponseDTO(corretora.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Update
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateCorretora(@PathVariable Long id, @RequestBody CorretoraRequestDTO data){
        Optional<Corretora> corretoraOptional = repository.findById(id);
        
        if (corretoraOptional.isPresent()) {
            Corretora corretora = corretoraOptional.get();
            // Atualizando os campos
            corretora.setNome(data.nome());
            corretora.setNomefan(data.nomefan());
            corretora.setEstado(data.estado());
            corretora.setCidade(data.cidade());
            corretora.setEndereco(data.endereco());
            corretora.setCnpj(data.cnpj());
            corretora.setEmail(data.email());
            corretora.setTelefone(data.telefone());
            corretora.setSusep(data.susep());
            
            repository.save(corretora); // Salva as alterações no banco de dados
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Delete
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCorretora(@PathVariable Long id){
        Optional<Corretora> corretora = repository.findById(id);
        
        if (corretora.isPresent()) {
            repository.deleteById(id); // Exclui a corretora
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // Retorna 204 (No Content)
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Retorna 404 (Not Found) se não encontrar
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/search")
    public ResponseEntity<List<CorretoraResponseDTO>> searchCorretora(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String cnpj
    ) {
        List<Corretora> corretoras;

        if (id != null) {
            Optional<Corretora> corretora = repository.findById(id);
            return corretora.map(value -> ResponseEntity.ok(List.of(new CorretoraResponseDTO(value))))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } else if (descricao != null && !descricao.isEmpty()) {
            corretoras = repository.findByNomeContainingIgnoreCaseOrNomefanContainingIgnoreCase(descricao, descricao);
        } else if (cnpj != null && !cnpj.isEmpty()) {
            corretoras = repository.findByCnpjContainingIgnoreCase(cnpj);
        } else {
            corretoras = repository.findAll();
        }

        if (corretoras.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<CorretoraResponseDTO> corretorasDTO = corretoras.stream()
                .map(CorretoraResponseDTO::new)
                .toList();
        return ResponseEntity.ok(corretorasDTO);
    }

}
