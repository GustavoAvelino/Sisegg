package com.example.Sisegg.controllers;

import com.example.Sisegg.DTO.PlacaResponseDTO;
import com.example.Sisegg.DTO.VeiculoRequestDTO;
import com.example.Sisegg.DTO.VeiculoResponseDTO;
import com.example.Sisegg.models.Veiculo;
import com.example.Sisegg.models.Cliente;
import com.example.Sisegg.repositories.VeiculoRepository;
import com.example.Sisegg.services.PlacaService;
import com.example.Sisegg.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("veiculo")
public class VeiculoController {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PlacaService placaService;

    // Create (POST)
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/save")
    public ResponseEntity<String> saveVeiculo(@RequestBody VeiculoRequestDTO data) {
        // Verifica se o cliente existe
        Optional<Cliente> clienteOptional = clienteRepository.findById(data.clienteId());
        if (clienteOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Cliente com o ID fornecido não encontrado.");
        }

        Cliente cliente = clienteOptional.get();
        Veiculo veiculo = new Veiculo(
            data.placa(),
            data.codigoFipe(),
            data.marca(),
            data.modelo(),
            data.anoModelo(),
            data.anoFabricacao(),
            data.valorFipe(),
            data.combustivel(),
            data.chassi(),
            data.passageiros(),
            data.financiado(),
            data.chassiRemarcado(),
            data.kitGas(),
            data.plotadoOuAdesivado(),
            cliente
        );

        veiculoRepository.save(veiculo);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 sem body
    }

    // Read all
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping
    public List<VeiculoResponseDTO> getAll() {
        return veiculoRepository.findAll().stream()
            .map(VeiculoResponseDTO::new)
            .toList();
    }

    // Read single por ID
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> getById(@PathVariable Long id) {
        Optional<Veiculo> veiculoOptional = veiculoRepository.findById(id);
        if (veiculoOptional.isPresent()) {
            return ResponseEntity.ok(new VeiculoResponseDTO(veiculoOptional.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
@PutMapping("/update/{id}")
public ResponseEntity<VeiculoResponseDTO> updateVeiculo(
        @PathVariable Long id,
        @RequestBody VeiculoRequestDTO data
) {
    // 1) Verifica se o veículo existe
    Optional<Veiculo> veiculoOptional = veiculoRepository.findById(id);
    if (veiculoOptional.isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    Veiculo veiculo = veiculoOptional.get();

    // 2) Verifica se o cliente existe
    Optional<Cliente> clienteOptional = clienteRepository.findById(data.clienteId());
    if (clienteOptional.isEmpty()) {
        return ResponseEntity.badRequest().build();
    }
    Cliente cliente = clienteOptional.get();

    // 3) Atualiza os campos do veículo
    veiculo.setPlaca(data.placa());
    veiculo.setCodigoFipe(data.codigoFipe());
    veiculo.setMarca(data.marca());
    veiculo.setModelo(data.modelo());
    veiculo.setAnoModelo(data.anoModelo());
    veiculo.setAnoFabricacao(data.anoFabricacao());
    veiculo.setValorFipe(data.valorFipe());
    veiculo.setCombustivel(data.combustivel());
    veiculo.setChassi(data.chassi());
    veiculo.setPassageiros(data.passageiros());
    veiculo.setFinanciado(data.financiado());
    veiculo.setChassiRemarcado(data.chassiRemarcado());
    veiculo.setKitGas(data.kitGas());
    veiculo.setPlotadoOuAdesivado(data.plotadoOuAdesivado());

    // Vincula o cliente atualizado
    veiculo.setCliente(cliente);

    veiculoRepository.save(veiculo);

    return ResponseEntity.ok(new VeiculoResponseDTO(veiculo));
}
 



    // Delete
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteVeiculo(@PathVariable Long id) {
        Optional<Veiculo> veiculoOptional = veiculoRepository.findById(id);
        if (veiculoOptional.isPresent()) {
            veiculoRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Search (GET) para placa, modelo e clienteId
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/search")
    public ResponseEntity<List<VeiculoResponseDTO>> searchVeiculo(
        @RequestParam(required = false) String placa,
        @RequestParam(required = false) String modelo,
        @RequestParam(required = false) Long clienteId
    ) {
        List<Veiculo> veiculos;

        if (placa != null && modelo != null && clienteId != null) {
            veiculos = veiculoRepository.findByPlacaContainingIgnoreCaseAndModeloContainingIgnoreCaseAndClienteId(
                placa, modelo, clienteId
            );
        } else if (placa != null && modelo != null) {
            veiculos = veiculoRepository.findByPlacaContainingIgnoreCaseAndModeloContainingIgnoreCase(
                placa, modelo
            );
        } else if (placa != null) {
            veiculos = veiculoRepository.findByPlacaContainingIgnoreCase(placa);
        } else if (modelo != null) {
            veiculos = veiculoRepository.findByModeloContainingIgnoreCase(modelo);
        } else if (clienteId != null) {
            veiculos = veiculoRepository.findByClienteId(clienteId);
        } else {
            veiculos = veiculoRepository.findAll();
        }

        if (veiculos.isEmpty()) {
            // Retorna vazio? Se quiser retornar 200 com lista vazia, basta devolver ResponseEntity.ok(listaVazia)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<VeiculoResponseDTO> veiculosDTO = veiculos.stream()
            .map(VeiculoResponseDTO::new)
            .toList();
        return ResponseEntity.ok(veiculosDTO);
    }




    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/consulta-placa/{placa}")
    public ResponseEntity<?> consultarPlaca(@PathVariable String placa) {
        try {
            Map<String, Object> dadosPlaca = placaService.consultarPlaca(placa);
            return ResponseEntity.ok(dadosPlaca);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao consultar API de placas: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
@GetMapping("/consulta-placa-detalhada/{placa}")
public ResponseEntity<?> consultarPlacaDetalhada(@PathVariable String placa) {
    try {
        // Agora consultarPlaca SEMPRE retorna Map<String, Object> ou lança exceção
        Map<String, Object> dadosPlaca = placaService.consultarPlaca(placa);

        // Monta o DTO
        PlacaResponseDTO responseDTO = new PlacaResponseDTO(dadosPlaca);

        // Retorna 200 OK com o DTO
        return ResponseEntity.ok(responseDTO);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao consultar API de placas: " + e.getMessage());
    }
}
}
