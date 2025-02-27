package com.example.Sisegg.DTO;

import com.example.Sisegg.enums.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class MulticalculoRequestDTO {
    private Long corretoraId; // 🔹 Adicionado
    private Long clienteId;
    private Set<Long> veiculosIds;

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
}

