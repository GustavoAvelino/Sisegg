package com.example.Sisegg.DTO;

public record ParcelaComissaoRequestDTO(
    Long gradeComissaoId, // ID da GradeComissao a que pertence
    int numeroParcela,
    double comissaoPercentual,
    double plParcelaPercentual
) { }
