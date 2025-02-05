package com.example.Sisegg.controllers;

import com.example.Sisegg.DTO.ProdutorRequestDTO;
import com.example.Sisegg.DTO.ProdutorResponseDTO;
import com.example.Sisegg.models.Produtor;
import com.example.Sisegg.repositories.ProdutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("produtor")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProdutorController {

    @Autowired
    private ProdutorRepository produtorRepository;

    // CREATE - Salvar um novo Produtor
    @PostMapping("/save")
    public ResponseEntity<String> saveProdutor(@RequestBody ProdutorRequestDTO data) {
        Produtor produtor = new Produtor(data);
        produtorRepository.save(produtor);
        return ResponseEntity.status(HttpStatus.CREATED).body("Produtor criado com sucesso!");
    }

    // READ ALL - Buscar todos os produtores
    @GetMapping
    public ResponseEntity<List<ProdutorResponseDTO>> getAll() {
        List<Produtor> lista = produtorRepository.findAll();
        List<ProdutorResponseDTO> produtores = lista.stream().map(ProdutorResponseDTO::new).toList();
        return ResponseEntity.ok(produtores);
    }

    // READ SINGLE - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProdutorResponseDTO> getById(@PathVariable Long id) {
        Optional<Produtor> produtorOpt = produtorRepository.findById(id);
        return produtorOpt.map(produtor -> ResponseEntity.ok(new ProdutorResponseDTO(produtor)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // UPDATE - Atualizar um Produtor
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateProdutor(@PathVariable Long id, @RequestBody ProdutorRequestDTO data) {
        Optional<Produtor> produtorOptional = produtorRepository.findById(id);

        if (produtorOptional.isPresent()) {
            Produtor produtor = produtorOptional.get();
            produtor.setNome(data.nome());
            produtor.setCpf(data.cpf());
            produtor.setCnpj(data.cnpj());
            produtor.setDataNascimento(data.dataNascimento());
            produtor.setSexo(data.sexo());
            produtor.setEmail(data.email());
            produtor.setTelefone(data.telefone());
            produtor.setEndereco(data.endereco());
            produtor.setImposto(data.imposto());
            produtor.setRepasse(data.repasse());
            produtor.setRepasseSobre(data.repasseSobre());
            produtor.setFormaRepasse(data.formaRepasse());

            produtorRepository.save(produtor);
            return ResponseEntity.status(HttpStatus.OK).body("Produtor atualizado com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produtor não encontrado.");
    }

    // DELETE - Remover um Produtor
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProdutor(@PathVariable Long id) {
        Optional<Produtor> produtorOpt = produtorRepository.findById(id);
        if (produtorOpt.isPresent()) {
            produtorRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Produtor deletado com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produtor não encontrado.");
    }

    // SEARCH - Filtrar por ID, CPF, CNPJ ou Nome
    @GetMapping("/search")
    public ResponseEntity<List<ProdutorResponseDTO>> searchProdutor(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String nome
    ) {
        List<Produtor> produtores;

        if (id != null) {
            Optional<Produtor> produtorOpt = produtorRepository.findById(id);
            if (produtorOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            produtores = List.of(produtorOpt.get());
        }
        else if (cpf != null && !cpf.isBlank()) {
            produtores = produtorRepository.findByCpf(cpf);
        }
        else if (cnpj != null && !cnpj.isBlank()) {
            produtores = produtorRepository.findByCnpj(cnpj);
        }
        else if (nome != null && !nome.isBlank()) {
            produtores = produtorRepository.findByNomeContainingIgnoreCase(nome);
        }
        else {
            produtores = produtorRepository.findAll();
        }

        if (produtores.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<ProdutorResponseDTO> dtoList = produtores.stream().map(ProdutorResponseDTO::new).toList();
        return ResponseEntity.ok(dtoList);
    }
}
