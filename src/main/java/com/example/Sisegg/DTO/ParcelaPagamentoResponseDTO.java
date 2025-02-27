package com.example.Sisegg.DTO;

import com.example.Sisegg.models.ParcelaPagamento;

public record ParcelaPagamentoResponseDTO(
    Long id,
    int numeroParcela,
    Double valorParcela,
    String dataVencimento,
    Boolean pago,
    String numeroDocumento,
    String tipoDocumento,
    String segurado
) {
    public ParcelaPagamentoResponseDTO(ParcelaPagamento p) {
        this(
            p.getId(),
            p.getNumeroParcela(),
            p.getValorParcela(),
            p.getDataVencimento(),
            p.getPago(),
            determineNumeroDocumento(p),
            determineTipoDocumento(p),
            (p.getProposta() != null 
                && p.getProposta().getCliente() != null 
                && p.getProposta().getCliente().getNome() != null)
                ? p.getProposta().getCliente().getNome() : "-"
        );
    }

    // Novo construtor que recebe a data de vencimento já formatada
    public ParcelaPagamentoResponseDTO(ParcelaPagamento p, String dataVencimento) {
        this(
            p.getId(),
            p.getNumeroParcela(),
            p.getValorParcela(),
            dataVencimento,
            p.getPago(),
            determineNumeroDocumento(p),
            determineTipoDocumento(p),
            (p.getProposta() != null 
                && p.getProposta().getCliente() != null 
                && p.getProposta().getCliente().getNome() != null)
                ? p.getProposta().getCliente().getNome() : "-"
        );
    }

    private static String determineNumeroDocumento(ParcelaPagamento p) {
        if (p.getProposta() != null) {
            if (p.getProposta().getNrEndosso() != null && !p.getProposta().getNrEndosso().isEmpty()) {
                return p.getProposta().getNrEndosso();
            }
            if (p.getProposta().getNrApolice() != null && !p.getProposta().getNrApolice().isEmpty()) {
                return p.getProposta().getNrApolice();
            }
            if (p.getProposta().getNrProposta() != null && !p.getProposta().getNrProposta().isEmpty()) {
                return p.getProposta().getNrProposta();
            }
        }
        return "-";
    }

    private static String determineTipoDocumento(ParcelaPagamento p) {
        if (p.getProposta() != null) {
            if (p.getProposta().getNrEndosso() != null && !p.getProposta().getNrEndosso().isEmpty()) {
                return "Endosso";
            }
            if (p.getProposta().getNrApolice() != null && !p.getProposta().getNrApolice().isEmpty()) {
                return "Apólice";
            }
            if (p.getProposta().getNrProposta() != null && !p.getProposta().getNrProposta().isEmpty()) {
                return "Proposta";
            }
        }
        return "-";
    }
}
