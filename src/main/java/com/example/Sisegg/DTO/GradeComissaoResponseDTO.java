package com.example.Sisegg.DTO;

import com.example.Sisegg.models.GradeComissao;
import java.util.List;

public record GradeComissaoResponseDTO(
    Long id,
    String nome,
    Long tipoPagamento,
    int quantidadeParcelas,
    Long corretoraId,
    List<ParcelaComissaoResponseDTO> parcelas
) {
    public GradeComissaoResponseDTO(GradeComissao grade) {
        this(
            grade.getId(),
            grade.getNome(),
            grade.getTipoPagamento(),
            grade.getQuantidadeParcelas(),
            grade.getCorretora().getId(),
            grade.getParcelas().stream().map(ParcelaComissaoResponseDTO::new).toList()
        );
    }
}
