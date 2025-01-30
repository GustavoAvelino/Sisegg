package com.example.Sisegg.controllers;

import com.example.Sisegg.DTO.ClienteRequestDTO;
import com.example.Sisegg.DTO.ClienteResponseDTO;
import com.example.Sisegg.models.Cliente;
import com.example.Sisegg.models.Corretora;
import com.example.Sisegg.repositories.ClienteRepository;
import com.example.Sisegg.repositories.CorretoraRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("cliente")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CorretoraRepository corretoraRepository;

    // CREATE - Salva um novo Cliente
    @PostMapping("/save")
    public ResponseEntity<String> saveCliente(@RequestBody ClienteRequestDTO data) {
        Cliente cliente = new Cliente(data);

        // Vincula a corretora, se vier no DTO
        if (data.corretoraId() != null) {
            Optional<Corretora> corretoraOpt = corretoraRepository.findById(data.corretoraId());
            if (corretoraOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Corretora com o ID fornecido não encontrada.");
            }
            cliente.setCorretora(corretoraOpt.get());
        }

        clienteRepository.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cliente criado com sucesso!");
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> getAll() {
        List<Cliente> lista = clienteRepository.findAll();
        List<ClienteResponseDTO> clientes = lista.stream()
                .map(ClienteResponseDTO::new)
                .toList();
        return ResponseEntity.ok(clientes);
    }

    // READ SINGLE BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> getById(@PathVariable Long id) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            return ResponseEntity.ok(new ClienteResponseDTO(clienteOpt.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateCliente(@PathVariable Long id, @RequestBody ClienteRequestDTO data) {
        Optional<Cliente> clienteOptional = clienteRepository.findById(id);

        if (clienteOptional.isPresent()) {
            Cliente cliente = clienteOptional.get();
            // Atualiza campos básicos
            cliente.setCnpjCpf(data.cnpjCpf());
            cliente.setNome(data.nome());
            cliente.setNomeSocial(data.nomeSocial());
            cliente.setSexo(data.sexo());
            cliente.setDataNascimento(data.dataNascimento());
            cliente.setEstadoCivil(data.estadoCivil());
            cliente.setEmail(data.email());
            cliente.setTelefone(data.telefone());

            // Atualiza corretora, se vier no DTO
            if (data.corretoraId() != null) {
                Optional<Corretora> corretoraOpt = corretoraRepository.findById(data.corretoraId());
                if (corretoraOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Corretora com o ID fornecido não encontrada.");
                }
                cliente.setCorretora(corretoraOpt.get());
            }

            clienteRepository.save(cliente);
            return ResponseEntity.status(HttpStatus.OK).body("Cliente atualizado com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado.");
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCliente(@PathVariable Long id) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            clienteRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Cliente deletado com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado.");
    }

    // SEARCH - Filtra por id, cnpjCpf, descricao (nome), e corretoraId
    @GetMapping("/search")
    public ResponseEntity<List<ClienteResponseDTO>> searchCliente(
        @RequestParam(required = false) Long id,
        @RequestParam(required = false) String cnpjCpf,
        @RequestParam(required = false) String descricao,
        @RequestParam(required = false) Long corretoraId
    ) {
        List<Cliente> clientes;

        if (id != null) {
            // Busca por ID
            Optional<Cliente> clienteOpt = clienteRepository.findById(id);
            if (clienteOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            clientes = List.of(clienteOpt.get());
        }
        else if (cnpjCpf != null && !cnpjCpf.isBlank()) {
            // Busca por CNPJ/CPF
            clientes = clienteRepository.findByCnpjCpf(cnpjCpf);
        }
        else if (descricao != null && !descricao.isBlank()) {
            // Busca por nome ou nome social
            clientes = clienteRepository.findByNomeContainingIgnoreCaseOrNomeSocialContainingIgnoreCase(descricao, descricao);
        }
        else {
            // Se não teve filtro, pega todos
            clientes = clienteRepository.findAll();
        }

        // Filtra pela corretora, se veio corretoraId
        if (corretoraId != null) {
            clientes = clientes.stream()
                    .filter(c -> c.getCorretora() != null && c.getCorretora().getId().equals(corretoraId))
                    .toList();
        }

        if (clientes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<ClienteResponseDTO> dtoList = clientes.stream()
                .map(ClienteResponseDTO::new)
                .toList();
        return ResponseEntity.ok(dtoList);
    }
}
