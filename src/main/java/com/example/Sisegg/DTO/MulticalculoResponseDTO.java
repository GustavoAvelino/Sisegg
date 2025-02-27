package com.example.Sisegg.DTO;

import com.example.Sisegg.enums.TipoSeguro;
import com.example.Sisegg.enums.Sexo;
import com.example.Sisegg.enums.TipoResidencia;
import com.example.Sisegg.enums.UsoVeiculoTrabalho;
import com.example.Sisegg.enums.TipoEstacionamento;
import com.example.Sisegg.enums.UsoVeiculoFaculdade;
import com.example.Sisegg.models.Multicalculo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public class MulticalculoResponseDTO {
    private Long id;
    private ClienteResponseDTO cliente;
    private Set<VeiculoResponseDTO> veiculos;
    private TipoSeguro tipoSeguro;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;
    private Boolean dependente17a26;
    private Integer idadeDependenteMaisNovo;
    private Integer tempoUtilizacaoDependentes;
    private Sexo sexoResidente;
    private Boolean residentePrincipal1825;
    private TipoResidencia principalCondutorResideEm;
    private UsoVeiculoTrabalho veiculoUtilizadoTrabalho;
    private TipoEstacionamento estacionamentoResidencia;
    private UsoVeiculoFaculdade veiculoUtilizadoFaculdade;
    private Integer quantidadeVeiculosResidencia;
    private Integer frequenciaUtilizacaoTrabalho;
    private Integer mediaKmMes;
    private Boolean principalCondutorRoubado2Anos;
    private BigDecimal comissaoPercentual;
    private BigDecimal valorPercentualFipe;
    private BigDecimal danosMateriais;
    private BigDecimal danosCorporais;
    private BigDecimal danosMorais;
    private BigDecimal mortePassageiro;
    private BigDecimal invalidezPermanentePassageiro;
    private BigDecimal despesasHospitalares;

    // Construtor que mapeia Multicalculo para MulticalculoResponseDTO
    public MulticalculoResponseDTO(Multicalculo multicalculo) {
        this.id = multicalculo.getId();
        this.cliente = new ClienteResponseDTO(multicalculo.getCliente());
        this.veiculos = multicalculo.getVeiculos().stream()
                .map(VeiculoResponseDTO::new)
                .collect(Collectors.toSet());
        this.tipoSeguro = multicalculo.getTipoSeguro();
        this.vigenciaInicio = multicalculo.getVigenciaInicio();
        this.vigenciaFim = multicalculo.getVigenciaFim();
        this.dependente17a26 = multicalculo.getDependente17a26();
        this.idadeDependenteMaisNovo = multicalculo.getIdadeDependenteMaisNovo();
        this.tempoUtilizacaoDependentes = multicalculo.getTempoUtilizacaoDependentes();
        this.sexoResidente = multicalculo.getSexoResidente();
        this.residentePrincipal1825 = multicalculo.getResidentePrincipal1825();
        this.principalCondutorResideEm = multicalculo.getPrincipalCondutorResideEm();
        this.veiculoUtilizadoTrabalho = multicalculo.getVeiculoUtilizadoTrabalho();
        this.estacionamentoResidencia = multicalculo.getEstacionamentoResidencia();
        this.veiculoUtilizadoFaculdade = multicalculo.getVeiculoUtilizadoFaculdade();
        this.quantidadeVeiculosResidencia = multicalculo.getQuantidadeVeiculosResidencia();
        this.frequenciaUtilizacaoTrabalho = multicalculo.getFrequenciaUtilizacaoTrabalho();
        this.mediaKmMes = multicalculo.getMediaKmMes();
        this.principalCondutorRoubado2Anos = multicalculo.getPrincipalCondutorRoubado2Anos();
        this.comissaoPercentual = multicalculo.getComissaoPercentual();
        this.valorPercentualFipe = multicalculo.getValorPercentualFipe();
        this.danosMateriais = multicalculo.getDanosMateriais();
        this.danosCorporais = multicalculo.getDanosCorporais();
        this.danosMorais = multicalculo.getDanosMorais();
        this.mortePassageiro = multicalculo.getMortePassageiro();
        this.invalidezPermanentePassageiro = multicalculo.getInvalidezPermanentePassageiro();
        this.despesasHospitalares = multicalculo.getDespesasHospitalares();
    }

    // Getters e Setters (ou utilize Lombok com @Getter/@Setter)
    public Long getId() {
        return id;
    }
    public ClienteResponseDTO getCliente() {
        return cliente;
    }
    public Set<VeiculoResponseDTO> getVeiculos() {
        return veiculos;
    }
    public TipoSeguro getTipoSeguro() {
        return tipoSeguro;
    }
    public LocalDate getVigenciaInicio() {
        return vigenciaInicio;
    }
    public LocalDate getVigenciaFim() {
        return vigenciaFim;
    }
    public Boolean getDependente17a26() {
        return dependente17a26;
    }
    public Integer getIdadeDependenteMaisNovo() {
        return idadeDependenteMaisNovo;
    }
    public Integer getTempoUtilizacaoDependentes() {
        return tempoUtilizacaoDependentes;
    }
    public Sexo getSexoResidente() {
        return sexoResidente;
    }
    public Boolean getResidentePrincipal1825() {
        return residentePrincipal1825;
    }
    public TipoResidencia getPrincipalCondutorResideEm() {
        return principalCondutorResideEm;
    }
    public UsoVeiculoTrabalho getVeiculoUtilizadoTrabalho() {
        return veiculoUtilizadoTrabalho;
    }
    public TipoEstacionamento getEstacionamentoResidencia() {
        return estacionamentoResidencia;
    }
    public UsoVeiculoFaculdade getVeiculoUtilizadoFaculdade() {
        return veiculoUtilizadoFaculdade;
    }
    public Integer getQuantidadeVeiculosResidencia() {
        return quantidadeVeiculosResidencia;
    }
    public Integer getFrequenciaUtilizacaoTrabalho() {
        return frequenciaUtilizacaoTrabalho;
    }
    public Integer getMediaKmMes() {
        return mediaKmMes;
    }
    public Boolean getPrincipalCondutorRoubado2Anos() {
        return principalCondutorRoubado2Anos;
    }
    public BigDecimal getComissaoPercentual() {
        return comissaoPercentual;
    }
    public BigDecimal getValorPercentualFipe() {
        return valorPercentualFipe;
    }
    public BigDecimal getDanosMateriais() {
        return danosMateriais;
    }
    public BigDecimal getDanosCorporais() {
        return danosCorporais;
    }
    public BigDecimal getDanosMorais() {
        return danosMorais;
    }
    public BigDecimal getMortePassageiro() {
        return mortePassageiro;
    }
    public BigDecimal getInvalidezPermanentePassageiro() {
        return invalidezPermanentePassageiro;
    }
    public BigDecimal getDespesasHospitalares() {
        return despesasHospitalares;
    }
}
