package com.example.Sisegg.controllers;

import com.example.Sisegg.DTO.ClienteRequestDTO;
import com.example.Sisegg.DTO.ClienteResponseDTO;
import com.example.Sisegg.models.Cliente;
import com.example.Sisegg.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("cliente")
public class ClienteController {

    @Autowired
    private ClienteRepository repository;

    // Create - Salva um novo Cliente
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/save")
    public ResponseEntity<Void> saveCliente(@RequestBody ClienteRequestDTO data) {
        Cliente clienteData = new Cliente(data);
        repository.save(clienteData);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Read all - Retorna todos os clientes
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping
    public List<ClienteResponseDTO> getAll() {
        List<ClienteResponseDTO> clientes = repository.findAll().stream()
                                                     .map(ClienteResponseDTO::new).toList();
        return clientes;
    }

    // Read single - Retorna um cliente pelo id
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> getById(@PathVariable Long id) {
        Optional<Cliente> cliente = repository.findById(id);
        if (cliente.isPresent()) {
            return ResponseEntity.ok(new ClienteResponseDTO(cliente.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Update - Atualiza um cliente existente pelo id
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PutMapping("/update/{id}")
    public ResponseEntity<Void> updateCliente(@PathVariable Long id, @RequestBody ClienteRequestDTO data) {
        Optional<Cliente> clienteOptional = repository.findById(id);

        if (clienteOptional.isPresent()) {
            Cliente cliente = clienteOptional.get();
            // Atualizando os campos
            cliente.setCnpjCpf(data.cnpjCpf());
            cliente.setNome(data.nome());
            cliente.setNomeSocial(data.nomeSocial());
            cliente.setSexo(data.sexo());
            cliente.setDataNascimento(data.dataNascimento());
            cliente.setEstadoCivil(data.estadoCivil());
            cliente.setEmail(data.email());
            cliente.setTelefone(data.telefone());

            repository.save(cliente); // Salva as alterações no banco de dados
            return ResponseEntity.status(HttpStatus.OK).build();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Delete - Deleta um cliente pelo id
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        Optional<Cliente> cliente = repository.findById(id);

        if (cliente.isPresent()) {
            repository.deleteById(id); // Exclui o cliente
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // Retorna 204 (No Content)
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Retorna 404 (Not Found) se não encontrar
    }

     // Pesquisa por cliente com parâmetros
     @CrossOrigin(origins = "*", allowedHeaders = "*")
     @GetMapping("/search")
     public ResponseEntity<List<ClienteResponseDTO>> searchCliente(
         @RequestParam(required = false) Long id,
         @RequestParam(required = false) String cnpjCpf,
         @RequestParam(required = false) String descricao
     ) {
         List<Cliente> clientes;
 
         if (id != null) {
             Optional<Cliente> cliente = repository.findById(id);
             return cliente.map(value -> ResponseEntity.ok(List.of(new ClienteResponseDTO(value))))
                           .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
         } else if (cnpjCpf != null && !cnpjCpf.isEmpty()) {
             // Pesquisa por CNPJ/CPF
             clientes = repository.findByCnpjCpf(cnpjCpf);
         } else if (descricao != null && !descricao.isEmpty()) {
             // Pesquisa por Nome ou Nome Social utilizando LIKE
             clientes = repository.findByNomeContainingIgnoreCaseOrNomeSocialContainingIgnoreCase(descricao, descricao);
         } else {
             // Se não passar nenhum parâmetro de pesquisa
             clientes = repository.findAll();
         }
 
         if (clientes.isEmpty()) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
         }
 
         List<ClienteResponseDTO> clientesDTO = clientes.stream()
                                                   .map(ClienteResponseDTO::new)
                                                   .toList();
         return ResponseEntity.ok(clientesDTO);
     }
}
