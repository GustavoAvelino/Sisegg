package com.example.Sisegg.DTO;

import com.example.Sisegg.models.ParcComissaoDoc;

public record ParcComissaoDocResponseDTO(
    Long id,
    int numeroParcela,
    Double valorParcela,
    Double percentualComissao,
    Double valorComissao,
    String dataVencimento,
    Boolean recebido,
    String numeroDocumento,
    String tipoDocumento,
    String produtor
) {
    public ParcComissaoDocResponseDTO(ParcComissaoDoc doc) {
        this(
            doc.getId(),
            doc.getNumeroParcela(),
            doc.getValorParcela(),
            doc.getPercentualComissao(),
            doc.getValorComissao(),
            doc.getDataVencimento(),
            doc.getRecebido(),
            determineNumeroDocumento(doc),
            determineTipoDocumento(doc),
            (doc.getProposta() != null &&
             doc.getProposta().getProdutor() != null &&
             doc.getProposta().getProdutor().getNome() != null)
                ? doc.getProposta().getProdutor().getNome() : "-"
        );
    }
    
    // Novo construtor que recebe a data de vencimento já formatada
    public ParcComissaoDocResponseDTO(ParcComissaoDoc doc, String dataVencimento) {
        this(
            doc.getId(),
            doc.getNumeroParcela(),
            doc.getValorParcela(),
            doc.getPercentualComissao(),
            doc.getValorComissao(),
            dataVencimento,
            doc.getRecebido(),
            determineNumeroDocumento(doc),
            determineTipoDocumento(doc),
            (doc.getProposta() != null &&
             doc.getProposta().getProdutor() != null &&
             doc.getProposta().getProdutor().getNome() != null)
                ? doc.getProposta().getProdutor().getNome() : "-"
        );
    }

    private static String determineNumeroDocumento(ParcComissaoDoc doc) {
        if (doc.getProposta() != null) {
            if (doc.getProposta().getNrEndosso() != null && !doc.getProposta().getNrEndosso().isEmpty()) {
                return doc.getProposta().getNrEndosso();
            }
            if (doc.getProposta().getNrApolice() != null && !doc.getProposta().getNrApolice().isEmpty()) {
                return doc.getProposta().getNrApolice();
            }
            if (doc.getProposta().getNrProposta() != null && !doc.getProposta().getNrProposta().isEmpty()) {
                return doc.getProposta().getNrProposta();
            }
        }
        return "-";
    }

    private static String determineTipoDocumento(ParcComissaoDoc doc) {
        if (doc.getProposta() != null) {
            if (doc.getProposta().getNrEndosso() != null && !doc.getProposta().getNrEndosso().isEmpty()) {
                return "Endosso";
            }
            if (doc.getProposta().getNrApolice() != null && !doc.getProposta().getNrApolice().isEmpty()) {
                return "Apólice";
            }
            if (doc.getProposta().getNrProposta() != null && !doc.getProposta().getNrProposta().isEmpty()) {
                return "Proposta";
            }
        }
        return "-";
    }
}
