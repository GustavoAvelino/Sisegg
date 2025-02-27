package com.example.Sisegg.DTO;

import com.example.Sisegg.models.ParcelaPagamento;

public record ParcelaPagamentoDTO(
    Long id,
    int numeroParcela,
    Double valor,
    String dataVencimento,
    boolean pago
) {
    public ParcelaPagamentoDTO(ParcelaPagamento parcela) {
        this(
            parcela.getId(),
            parcela.getNumeroParcela(),
            parcela.getValorParcela(), // Certifique-se que este método retorna o valor desejado
            parcela.getDataVencimento(),
            parcela.getPago()
        );
    }
}
