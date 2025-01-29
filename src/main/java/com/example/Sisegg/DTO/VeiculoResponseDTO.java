package com.example.Sisegg.DTO;

import com.example.Sisegg.models.Veiculo;

public record VeiculoResponseDTO(
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
    ClienteResponseDTO cliente
) {
    public VeiculoResponseDTO(Veiculo veiculo) {
        this(
            veiculo.getId(),
            veiculo.getPlaca(),
            veiculo.getCodigoFipe(),
            veiculo.getMarca(),
            veiculo.getModelo(),
            veiculo.getAnoModelo(),
            veiculo.getAnoFabricacao(),
            veiculo.getValorFipe(),
            veiculo.getCombustivel(),
            veiculo.getChassi(),
            veiculo.getPassageiros(),
            veiculo.getFinanciado(),
            veiculo.getChassiRemarcado(),
            veiculo.getKitGas(),
            veiculo.getPlotadoOuAdesivado(),
            new ClienteResponseDTO(veiculo.getCliente())
        );
    }
}
