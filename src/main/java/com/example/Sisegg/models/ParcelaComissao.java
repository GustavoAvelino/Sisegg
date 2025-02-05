package com.example.Sisegg.models;

import com.example.Sisegg.DTO.ParcelaComissaoResponseDTO;
import com.example.Sisegg.DTO.ParcelaComissaoRequestDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parcela_comissao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ParcelaComissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grade_comissao_id", nullable = false)
    private GradeComissao gradeComissao;

    private int numeroParcela;

    @Column(nullable = true) // Agora o campo pode ser nulo sem problemas
    private Double comissaoPercentual;

    @Column(nullable = true) // Agora o campo pode ser nulo sem problemas
    private Double plParcelaPercentual;

    public ParcelaComissao(ParcelaComissaoRequestDTO dto, GradeComissao gradeComissao) {
        this.gradeComissao = gradeComissao;
        this.numeroParcela = dto.numeroParcela();
        this.comissaoPercentual = dto.comissaoPercentual();
        this.plParcelaPercentual = dto.plParcelaPercentual();
    }
}
