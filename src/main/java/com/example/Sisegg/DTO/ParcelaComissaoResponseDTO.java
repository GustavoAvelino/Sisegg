package com.example.Sisegg.DTO;

import com.example.Sisegg.models.ParcelaComissao;

public record ParcelaComissaoResponseDTO(
    Long id,
    Long gradeComissaoId, // ID da GradeComissao a que pertence
    int numeroParcela,
    double comissaoPercentual,
    double plParcelaPercentual
) {
    public ParcelaComissaoResponseDTO(ParcelaComissao parcela) {
        this(
            parcela.getId(),
            parcela.getGradeComissao().getId(),
            parcela.getNumeroParcela(),
            parcela.getComissaoPercentual(),
            parcela.getPlParcelaPercentual()
        );
    }
}
