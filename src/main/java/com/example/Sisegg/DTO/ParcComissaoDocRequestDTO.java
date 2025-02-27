package com.example.Sisegg.DTO;

public record ParcComissaoDocRequestDTO(
    Long propostaId,         // Novo campo para identificar a proposta
    Long gradeComissaoId,
    Long parcelaComissaoId,
    int numeroParcela,
    Double valorParcela,
    Double percentualComissao,
    Double valorComissao,
    String dataVencimento,
    Boolean recebido
) {}
