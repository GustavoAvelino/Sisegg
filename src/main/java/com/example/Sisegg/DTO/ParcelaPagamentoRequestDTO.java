package com.example.Sisegg.DTO;

public record ParcelaPagamentoRequestDTO(
    Long propostaId,
    int numeroParcela,
    Double valorParcela,
    String dataVencimento,
    Boolean pago
) {}
