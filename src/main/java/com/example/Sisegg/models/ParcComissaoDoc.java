package com.example.Sisegg.models;

import com.example.Sisegg.DTO.ParcComissaoDocRequestDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parc_comissao_doc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ParcComissaoDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com a PropostaApolice (obrigatório para vincular o documento à proposta)
    @ManyToOne
    @JoinColumn(name = "proposta_id", nullable = false)
    private PropostaApolice proposta;

    @ManyToOne
    @JoinColumn(name = "grade_comissao_id", nullable = false)
    private GradeComissao gradeComissao;

    @ManyToOne
    @JoinColumn(name = "parcela_comissao_id", nullable = true)
    private ParcelaComissao parcelaComissao;

    private int numeroParcela;

    @Column(nullable = false)
    private Double valorParcela;

    @Column(nullable = false)
    private Double percentualComissao;

    @Column(nullable = false)
    private Double valorComissao;

    @Column(nullable = false)
    private String dataVencimento;

    @Column(nullable = false)
    private Boolean recebido;

    // Construtor para conversão de DTO -> Entidade, agora recebendo também a proposta
    public ParcComissaoDoc(ParcComissaoDocRequestDTO dto, GradeComissao gradeComissao, ParcelaComissao parcelaComissao, PropostaApolice proposta) {
        this.proposta = proposta;
        this.gradeComissao = gradeComissao;
        this.parcelaComissao = parcelaComissao;
        this.numeroParcela = dto.numeroParcela();
        this.valorParcela = dto.valorParcela();
        this.percentualComissao = dto.percentualComissao();
        this.valorComissao = dto.valorComissao();
        this.dataVencimento = dto.dataVencimento();
        this.recebido = dto.recebido();
    }
}
