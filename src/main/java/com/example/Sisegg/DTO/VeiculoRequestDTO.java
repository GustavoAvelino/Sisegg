package com.example.Sisegg.DTO;

public record VeiculoRequestDTO(
    Long id,
    String placa,
    String codigoFipe,
    String marca,
    String modelo,
    Integer anoModelo,
    Integer anoFabricacao,
    Double valorFipe,
    String combustivel,
    String chassi,
    Integer passageiros,
    Boolean financiado,
    Boolean chassiRemarcado,
    Boolean kitGas,
    Boolean plotadoOuAdesivado,
    Long clienteId // Aqui deve ser apenas o ID do cliente
) {}
