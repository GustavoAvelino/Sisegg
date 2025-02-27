package com.example.Sisegg.DTO;

import java.util.List;

public record PropostaApoliceRequestDTO(
    String nrProposta,
    String nrApolice,
    String nrEndosso,
    Long seguradoraId,
    Long clienteId,
    Long produtorId,
    Long gradeComissaoId,
    String dataInicioVigencia,
    String dataFimVigencia,
    String tipo,
    String dataEmissaoApolice,
    String apoliceEfetivadaEm,
    Double comissaoPercentual,
    Double descontoPercentual,
    int quantidadeParcelas,
    String dataPrimeiroPagamento,
    String dataBaseParcelas,
    Double valorPrimeiroPagamento,
    Double premioLiquido,
    Double premioTotal,
    String motivoCancelamentoRecusa,
    String dataCancelamentoRecusa,
    String observacoes,
    String status,
    String motivoEndosso,
    boolean cancelado,
    boolean recusado,
    boolean endossado,
    List<Long> veiculos, // Enviar apenas os IDs dos veículos
    List<ParcelaPagamentoDTO> parcelas, // Caso queira enviar parcelas manualmente
    List<String> anexos // Caso queira incluir anexos (URLs ou nomes dos arquivos)
) {}
