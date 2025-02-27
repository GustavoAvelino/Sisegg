package com.example.Sisegg.models;

import com.example.Sisegg.DTO.MulticalculoRequestDTO;
import com.example.Sisegg.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "multicalculo")
public class Multicalculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com Corretora
    @ManyToOne
    @JoinColumn(name = "corretora_id", nullable = false)
    private Corretora corretora;

    // Relacionamento com Cliente
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Relacionamento com Veículo (ManyToMany)
    @ManyToMany
    @JoinTable(
        name = "multicalculo_veiculo",
        joinColumns = @JoinColumn(name = "multicalculo_id"),
        inverseJoinColumns = @JoinColumn(name = "veiculo_id")
    )
    private Set<Veiculo> veiculos;

    // Informações do Seguro
    @Column(name = "tipo_seguro", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoSeguro tipoSeguro;

    @Column(name = "vigencia_inicio", nullable = false)
    private LocalDate vigenciaInicio;

    @Column(name = "vigencia_fim", nullable = false)
    private LocalDate vigenciaFim;

    // Perfil de Risco
    @Column(name = "dependente_17_26")
    private Boolean dependente17a26;

    @Column(name = "idade_dependente_mais_novo")
    private Integer idadeDependenteMaisNovo;

    @Column(name = "tempo_utilizacao_dependentes")
    private Integer tempoUtilizacaoDependentes; // Em meses

    @Column(name = "sexo_residente", nullable = false)
    @Enumerated(EnumType.STRING)
    private Sexo sexoResidente;

    @Column(name = "residente_principal_18_25")
    private Boolean residentePrincipal1825;

    @Column(name = "principal_condutor_reside_em", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoResidencia principalCondutorResideEm;

    @Column(name = "veiculo_utilizado_trabalho")
    @Enumerated(EnumType.STRING)
    private UsoVeiculoTrabalho veiculoUtilizadoTrabalho;

    @Column(name = "estacionamento_residencia")
    @Enumerated(EnumType.STRING)
    private TipoEstacionamento estacionamentoResidencia;

    @Column(name = "veiculo_utilizado_faculdade")
    @Enumerated(EnumType.STRING)
    private UsoVeiculoFaculdade veiculoUtilizadoFaculdade;

    @Column(name = "quantidade_veiculos_residencia")
    private Integer quantidadeVeiculosResidencia;

    @Column(name = "frequencia_utilizacao_trabalho")
    private Integer frequenciaUtilizacaoTrabalho; // Número de vezes por semana

    @Column(name = "media_km_mes")
    private Integer mediaKmMes;

    @Column(name = "principal_condutor_roubado_2_anos")
    private Boolean principalCondutorRoubado2Anos;

    // Coberturas
    @Column(name = "comissao_percentual", nullable = false)
    private BigDecimal comissaoPercentual;

    @Column(name = "valor_percentual_fipe", nullable = false)
    private BigDecimal valorPercentualFipe;

    @Column(name = "danos_materiais", nullable = false)
    private BigDecimal danosMateriais;

    @Column(name = "danos_corporais", nullable = false)
    private BigDecimal danosCorporais;

    @Column(name = "danos_morais", nullable = false)
    private BigDecimal danosMorais;

    @Column(name = "morte_passageiro", nullable = false)
    private BigDecimal mortePassageiro;

    @Column(name = "invalidez_permanente_passageiro", nullable = false)
    private BigDecimal invalidezPermanentePassageiro;

    @Column(name = "despesas_hospitalares", nullable = false)
    private BigDecimal despesasHospitalares;

    // 🔹 Construtor com DTO
    public Multicalculo(MulticalculoRequestDTO dto, Cliente cliente, Set<Veiculo> veiculos, Corretora corretora) {
        this.cliente = cliente;
        this.veiculos = veiculos;
        this.corretora = corretora;
        this.tipoSeguro = dto.getTipoSeguro();
        this.vigenciaInicio = dto.getVigenciaInicio();
        this.vigenciaFim = dto.getVigenciaFim();
        this.dependente17a26 = dto.getDependente17a26();
        this.idadeDependenteMaisNovo = dto.getIdadeDependenteMaisNovo();
        this.tempoUtilizacaoDependentes = dto.getTempoUtilizacaoDependentes();
        this.sexoResidente = dto.getSexoResidente();
        this.residentePrincipal1825 = dto.getResidentePrincipal1825();
        this.principalCondutorResideEm = dto.getPrincipalCondutorResideEm();
        this.veiculoUtilizadoTrabalho = dto.getVeiculoUtilizadoTrabalho();
        this.estacionamentoResidencia = dto.getEstacionamentoResidencia();
        this.veiculoUtilizadoFaculdade = dto.getVeiculoUtilizadoFaculdade();
        this.quantidadeVeiculosResidencia = dto.getQuantidadeVeiculosResidencia();
        this.frequenciaUtilizacaoTrabalho = dto.getFrequenciaUtilizacaoTrabalho();
        this.mediaKmMes = dto.getMediaKmMes();
        this.principalCondutorRoubado2Anos = dto.getPrincipalCondutorRoubado2Anos();
        this.comissaoPercentual = dto.getComissaoPercentual();
        this.valorPercentualFipe = dto.getValorPercentualFipe();
        this.danosMateriais = dto.getDanosMateriais();
        this.danosCorporais = dto.getDanosCorporais();
        this.danosMorais = dto.getDanosMorais();
        this.mortePassageiro = dto.getMortePassageiro();
        this.invalidezPermanentePassageiro = dto.getInvalidezPermanentePassageiro();
        this.despesasHospitalares = dto.getDespesasHospitalares();
    }
}
