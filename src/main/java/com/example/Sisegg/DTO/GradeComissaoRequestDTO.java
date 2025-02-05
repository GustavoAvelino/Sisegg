package com.example.Sisegg.DTO;

import java.util.List;

public record GradeComissaoRequestDTO(
    String nome,
    Long tipoPagamento,
    int quantidadeParcelas,
    Long corretoraId,
    List<ParcelaComissaoRequestDTO> parcelas
) { }
