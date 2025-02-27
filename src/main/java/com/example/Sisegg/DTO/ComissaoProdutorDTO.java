package com.example.Sisegg.DTO;

public record ComissaoProdutorDTO(
    Long docId,
    String numeroDocumento,
    String dataVencimento,
    int numeroParcela,
    Double premioLiquido,
    Double somaComissaoCorretora,
    Double comissaoProdutorParcela,
    String repasseSobre,
    Double repasse,
    String formaRepasse,
    String tipoDocumento,
    String produtorNome
) {}
