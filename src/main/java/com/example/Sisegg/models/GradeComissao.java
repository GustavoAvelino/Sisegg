package com.example.Sisegg.models;

import com.example.Sisegg.DTO.GradeComissaoRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "grade_comissao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class GradeComissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private Long tipoPagamento;

    @Column(nullable = true) // Agora permite valores nulos
    private Integer quantidadeParcelas;

    @ManyToOne
    @JoinColumn(name = "corretora_id", nullable = false)
    private Corretora corretora;

    @OneToMany(mappedBy = "gradeComissao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParcelaComissao> parcelas;

    public GradeComissao(GradeComissaoRequestDTO dto, Corretora corretora) {
        this.nome = dto.nome();
        this.tipoPagamento = dto.tipoPagamento();
        this.quantidadeParcelas = dto.quantidadeParcelas();
        this.corretora = corretora;
    }
}
