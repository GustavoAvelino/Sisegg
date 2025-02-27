package com.example.Sisegg.models;

import com.example.Sisegg.DTO.PropostaApoliceRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "proposta_apolice")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class PropostaApolice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nrProposta;

    @Column(nullable = true)
    private String nrApolice;

    @Column(nullable = true)
    private String nrEndosso;

    @Column(nullable = true)
    private String motivoEndosso;

    @Column(nullable = false)
    private boolean cancelado = false;

    @Column(nullable = false)
    private boolean recusado = false;

    @Column(nullable = false)
    private boolean endossado = false;

    @ManyToOne
    @JoinColumn(name = "corretora_id", nullable = false)
    private Corretora corretora;

    @ManyToOne
    @JoinColumn(name = "seguradora_id", nullable = false)
    private Seguradora seguradora;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "produtor_id", referencedColumnName = "id", nullable = false)
    private Produtor produtor;
    
    @ManyToOne
    @JoinColumn(name = "grade_comissao_id", nullable = false)
    private GradeComissao gradeComissao;

    @OneToMany(mappedBy = "proposta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParcelaPagamento> parcelas;

    @ManyToMany
    @JoinTable(
        name = "proposta_veiculos",
        joinColumns = @JoinColumn(name = "proposta_id"),
        inverseJoinColumns = @JoinColumn(name = "veiculo_id")
    )
    private List<Veiculo> veiculos;

    @OneToMany(mappedBy = "proposta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnexoProposta> anexos;

    @Column(nullable = false)
    private String dataInicioVigencia;

    @Column(nullable = false)
    private String dataFimVigencia;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String dataEmissaoApolice;

    @Column(nullable = true)
    private String apoliceEfetivadaEm;

    @Column(nullable = false)
    private Double comissaoPercentual;

    @Column(nullable = false)
    private Double descontoPercentual;

    @Column(nullable = false)
    private int quantidadeParcelas;

    @Column(nullable = false)
    private String dataPrimeiroPagamento;

    @Column(nullable = false)
    private String dataBaseParcelas;

    @Column(nullable = false)
    private Double valorPrimeiroPagamento;

    @Column(nullable = false)
    private Double premioLiquido;

    @Column(nullable = false)
    private Double premioTotal;

    @Column(nullable = true)
    private String motivoCancelamentoRecusa;

    @Column(nullable = true)
    private String dataCancelamentoRecusa;

    @Column(nullable = true, length = 1000)
    private String observacoes;

    @Column(nullable = false)
    private String status = "VIGENTE";
    // Construtor corrigido
    public PropostaApolice(PropostaApoliceRequestDTO dto, Corretora corretora, Seguradora seguradora,
                       Cliente cliente, Produtor produtor, GradeComissao gradeComissao) {
    this.nrProposta = dto.nrProposta();
    this.nrApolice = dto.nrApolice();
    this.nrEndosso = dto.nrEndosso();
    this.corretora = corretora;
    this.seguradora = seguradora;
    this.cliente = cliente;
    this.produtor = produtor;
    this.gradeComissao = gradeComissao;
    this.dataInicioVigencia = dto.dataInicioVigencia();
    this.dataFimVigencia = dto.dataFimVigencia();
    this.tipo = dto.tipo();
    this.dataEmissaoApolice = dto.dataEmissaoApolice();
    this.apoliceEfetivadaEm = dto.apoliceEfetivadaEm();
    this.comissaoPercentual = dto.comissaoPercentual();
    this.descontoPercentual = dto.descontoPercentual();
    this.quantidadeParcelas = dto.quantidadeParcelas();
    this.dataPrimeiroPagamento = dto.dataPrimeiroPagamento();
    this.dataBaseParcelas = dto.dataBaseParcelas();
    this.valorPrimeiroPagamento = dto.valorPrimeiroPagamento();
    this.premioLiquido = dto.premioLiquido();
    this.premioTotal = dto.premioTotal();
    this.motivoCancelamentoRecusa = dto.motivoCancelamentoRecusa();
    this.dataCancelamentoRecusa = dto.dataCancelamentoRecusa();
    this.observacoes = dto.observacoes();
    
    // Definir status padrão se não vier do DTO
    this.status = (dto.status() != null && !dto.status().isBlank()) ? dto.status() : "Vigente";
}

}
