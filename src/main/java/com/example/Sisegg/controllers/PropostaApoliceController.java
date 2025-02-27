package com.example.Sisegg.controllers;

import com.example.Sisegg.DTO.CancelRecusaDTO;
import com.example.Sisegg.DTO.ParcComissaoDocRequestDTO;
import com.example.Sisegg.DTO.ParcComissaoDocResponseDTO;
import com.example.Sisegg.DTO.ParcelaPagamentoDTO;
import com.example.Sisegg.DTO.PropostaApoliceRequestDTO;
import com.example.Sisegg.DTO.PropostaApoliceResponseDTO;
import com.example.Sisegg.models.PropostaApolice;
import com.example.Sisegg.models.AnexoProposta;
import com.example.Sisegg.models.Corretora;
import com.example.Sisegg.models.GradeComissao;
import com.example.Sisegg.models.ParcComissaoDoc;
import com.example.Sisegg.models.ParcelaComissao;
import com.example.Sisegg.models.ParcelaPagamento;
import com.example.Sisegg.models.Produtor;
import com.example.Sisegg.models.Cliente;
import com.example.Sisegg.models.Seguradora;
import com.example.Sisegg.models.Veiculo;
import com.example.Sisegg.repositories.PropostaApoliceRepository;
import com.example.Sisegg.repositories.CorretoraRepository;
import com.example.Sisegg.repositories.SeguradoraRepository;
import com.example.Sisegg.repositories.ClienteRepository;
import com.example.Sisegg.repositories.GradeComissaoRepository;
import com.example.Sisegg.repositories.ProdutorRepository;
import com.example.Sisegg.repositories.VeiculoRepository;

import jakarta.transaction.Transactional;

import com.example.Sisegg.repositories.ParcelaPagamentoRepository;
import com.example.Sisegg.repositories.ParcComissaoDocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/proposta-apolice")
@CrossOrigin("*")
public class PropostaApoliceController {

    @Autowired
    private PropostaApoliceRepository propostaApoliceRepository;

    @Autowired
    private CorretoraRepository corretoraRepository;

    @Autowired
    private SeguradoraRepository seguradoraRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private GradeComissaoRepository gradeComissaoRepository;

    @Autowired
    private ProdutorRepository produtorRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ParcelaPagamentoRepository parcelaPagamentoRepository;

    @Autowired
    private ParcComissaoDocRepository parcComissaoDocRepository;

    // Criar uma nova Proposta/Apolice
    @PostMapping("/salvar")
    public ResponseEntity<?> salvarProposta(@RequestBody PropostaApoliceRequestDTO dto) {
        System.out.println("📌 Recebendo proposta com produtorId: " + dto.produtorId());

        // Buscar o produtor
        Optional<Produtor> produtorOpt = produtorRepository.findById(dto.produtorId());
        if (produtorOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("⚠ Erro: Produtor não encontrado.");
        }
        Produtor produtor = produtorOpt.get();

        // Verificar se o produtor possui corretora
        if (produtor.getCorretora() == null) {
            return ResponseEntity.badRequest().body("⚠ Erro: O produtor não possui corretora vinculada.");
        }
        Corretora corretora = produtor.getCorretora();

        // Buscar seguradora e cliente
        Optional<Seguradora> seguradoraOpt = seguradoraRepository.findById(dto.seguradoraId());
        if (seguradoraOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("⚠ Erro: Seguradora não encontrada.");
        }
        Optional<Cliente> clienteOpt = clienteRepository.findById(dto.clienteId());
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("⚠ Erro: Cliente não encontrada.");
        }
        Cliente cliente = clienteOpt.get();

        Optional<GradeComissao> gradeOpt = gradeComissaoRepository.findById(dto.gradeComissaoId());
        if (gradeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("⚠ Erro: Grade de comissão não encontrada.");
        }

        // Definir tipo e status
        String tipoDefinido = (dto.nrApolice() != null && !dto.nrApolice().isBlank()) ? "Apolice" : "Proposta";
        String statusDefinido = (dto.status() == null || dto.status().isBlank()) ? "Vigente" : dto.status();

        // Criar a proposta (PropostaApolice) com o construtor que converte o DTO
        PropostaApolice proposta = new PropostaApolice(dto, corretora, seguradoraOpt.get(), cliente, produtor, gradeOpt.get());
        proposta.setTipo(tipoDefinido);
        proposta.setStatus(statusDefinido);

        // Adicionar veículos, se houver
        if (dto.veiculos() != null && !dto.veiculos().isEmpty()) {
            List<Veiculo> veiculos = veiculoRepository.findAllById(dto.veiculos());
            if (veiculos.size() != dto.veiculos().size()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("⚠ Erro: Alguns veículos não foram encontrados.");
            }
            proposta.setVeiculos(veiculos);
        }

        // Recalcular o prêmio líquido: prêmioTotal - (prêmioTotal * descontoPercentual / 100)
        double premioTotal = dto.premioTotal();
        double descontoPercentual = dto.descontoPercentual();
        double premioLiquidoCalculado = premioTotal - (premioTotal * descontoPercentual / 100.0);
        proposta.setPremioLiquido(premioLiquidoCalculado);

        // Salvar a proposta
        PropostaApolice propostaSalva = propostaApoliceRepository.save(proposta);

        // Gerar automaticamente as parcelas de pagamento e os documentos de comissão
        gerarParcelasAutomaticamente(propostaSalva);
        gerarParcComissaoDocs(propostaSalva);

        // Preparar e retornar a resposta
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("message", "✅ Proposta/Apolice salva com sucesso!");
        resposta.put("propostaId", propostaSalva.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    // Gerar Parcelas de Pagamento automaticamente
    private void gerarParcelasAutomaticamente(PropostaApolice proposta) {
        List<ParcelaPagamento> parcelas = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dataBase = LocalDate.parse(proposta.getDataBaseParcelas());
        String dataPrimeiroPagamentoFormatada = LocalDate.parse(proposta.getDataPrimeiroPagamento()).format(formatter);
        double adjustedPremioTotal = proposta.getPremioTotal() * (1 - (proposta.getDescontoPercentual() / 100.0));
        parcelas.add(new ParcelaPagamento(proposta, 1, proposta.getValorPrimeiroPagamento(), dataPrimeiroPagamentoFormatada, false));
        int parcelasRestantes = proposta.getQuantidadeParcelas() - 1;
        double valorParcelasRestantes = (adjustedPremioTotal - proposta.getValorPrimeiroPagamento()) / parcelasRestantes;
        for (int i = 2; i <= proposta.getQuantidadeParcelas(); i++) {
            String dataVencimento = dataBase.plusMonths(i - 1).format(formatter);
            parcelas.add(new ParcelaPagamento(proposta, i, valorParcelasRestantes, dataVencimento, false));
        }
        parcelaPagamentoRepository.saveAll(parcelas);
        System.out.println("✅ Parcelas geradas automaticamente para a proposta ID: " + proposta.getId());
    }

    // Gerar Documentos de Comissão automaticamente
    private void gerarParcComissaoDocs(PropostaApolice proposta) {
        List<ParcelaPagamento> parcelasPagamento = parcelaPagamentoRepository.findByPropostaId(proposta.getId());
        List<ParcelaComissao> parcelasComissao = proposta.getGradeComissao().getParcelas();
        List<ParcComissaoDoc> docs = new ArrayList<>();
        int tipoPagamento = proposta.getGradeComissao().getTipoPagamento().intValue();

        if (tipoPagamento == 1) { // Antecipado
            if (!parcelasPagamento.isEmpty()) {
                ParcelaPagamento primeiraParcela = parcelasPagamento.get(0);
                double adjustedPremioTotal = proposta.getPremioTotal() * (1 - (proposta.getDescontoPercentual() / 100.0));
                double totalCommission = 0.0;
                if (proposta.getComissaoPercentual() != null && proposta.getComissaoPercentual() > 0) {
                    totalCommission = adjustedPremioTotal * proposta.getComissaoPercentual() / 100.0;
                } else {
                    Optional<ParcelaComissao> pcOpt = parcelasComissao.stream()
                            .filter(pc -> pc.getNumeroParcela() == 1)
                            .findFirst();
                    if (pcOpt.isPresent() && pcOpt.get().getComissaoPercentual() != null) {
                        totalCommission = adjustedPremioTotal * pcOpt.get().getComissaoPercentual() / 100.0;
                    }
                }
                ParcComissaoDoc doc = new ParcComissaoDoc();
                doc.setProposta(proposta);
                doc.setGradeComissao(proposta.getGradeComissao());
                doc.setNumeroParcela(primeiraParcela.getNumeroParcela());
                doc.setValorParcela(primeiraParcela.getValorParcela());
                doc.setDataVencimento(primeiraParcela.getDataVencimento());
                doc.setRecebido(false);
                double perc = (proposta.getComissaoPercentual() != null && proposta.getComissaoPercentual() > 0)
                              ? proposta.getComissaoPercentual()
                              : parcelasComissao.stream()
                                  .filter(pc -> pc.getNumeroParcela() == 1)
                                  .findFirst()
                                  .map(ParcelaComissao::getComissaoPercentual)
                                  .orElse(0.0);
                doc.setPercentualComissao(perc);
                doc.setValorComissao(totalCommission);
                if (proposta.getComissaoPercentual() == null || proposta.getComissaoPercentual() <= 0) {
                    Optional<ParcelaComissao> pcOpt = parcelasComissao.stream()
                            .filter(pc -> pc.getNumeroParcela() == 1)
                            .findFirst();
                    pcOpt.ifPresent(doc::setParcelaComissao);
                }
                docs.add(doc);
            }
        } else if (tipoPagamento == 2) { // Esgotamento
            if (proposta.getComissaoPercentual() != null && proposta.getComissaoPercentual() > 0) {
                double adjustedPremioTotal = proposta.getPremioTotal() * (1 - (proposta.getDescontoPercentual() / 100.0));
                double totalComm = adjustedPremioTotal * proposta.getComissaoPercentual() / 100.0;
                double remainingComm = totalComm;
                for (ParcelaPagamento pp : parcelasPagamento) {
                    ParcComissaoDoc doc = new ParcComissaoDoc();
                    doc.setProposta(proposta);
                    doc.setGradeComissao(proposta.getGradeComissao());
                    doc.setNumeroParcela(pp.getNumeroParcela());
                    doc.setValorParcela(pp.getValorParcela());
                    doc.setDataVencimento(pp.getDataVencimento());
                    doc.setRecebido(false);
                    double commissionForThis = 0.0;
                    if (remainingComm > 0) {
                        commissionForThis = Math.min(pp.getValorParcela(), remainingComm);
                        remainingComm -= commissionForThis;
                    }
                    doc.setPercentualComissao(proposta.getComissaoPercentual());
                    doc.setValorComissao(commissionForThis);
                    if (proposta.getComissaoPercentual() == null || proposta.getComissaoPercentual() <= 0) {
                        Optional<ParcelaComissao> pcOpt = parcelasComissao.stream()
                                .filter(pc -> pc.getNumeroParcela() == pp.getNumeroParcela())
                                .findFirst();
                        pcOpt.ifPresent(doc::setParcelaComissao);
                    }
                    docs.add(doc);
                }
            } else {
                for (ParcelaPagamento pp : parcelasPagamento) {
                    Optional<ParcelaComissao> pcOpt = parcelasComissao.stream()
                            .filter(pc -> pc.getNumeroParcela() == pp.getNumeroParcela())
                            .findFirst();
                    double perc = (pcOpt.isPresent() && pcOpt.get().getComissaoPercentual() != null)
                                  ? pcOpt.get().getComissaoPercentual() : 0.0;
                    double commissionForThis = pp.getValorParcela() * perc / 100.0;
                    ParcComissaoDoc doc = new ParcComissaoDoc();
                    doc.setProposta(proposta);
                    doc.setGradeComissao(proposta.getGradeComissao());
                    pcOpt.ifPresent(doc::setParcelaComissao);
                    doc.setNumeroParcela(pp.getNumeroParcela());
                    doc.setValorParcela(pp.getValorParcela());
                    doc.setDataVencimento(pp.getDataVencimento());
                    doc.setRecebido(false);
                    doc.setPercentualComissao(perc);
                    doc.setValorComissao(commissionForThis);
                    docs.add(doc);
                }
            }
        } else { // Tipo 3 – Na parcela (padrão)
            for (ParcelaPagamento pp : parcelasPagamento) {
                double perc = 0.0;
                if (proposta.getComissaoPercentual() != null && proposta.getComissaoPercentual() > 0) {
                    perc = proposta.getComissaoPercentual();
                } else {
                    Optional<ParcelaComissao> pcOpt = parcelasComissao.stream()
                        .filter(pc -> pc.getNumeroParcela() == pp.getNumeroParcela())
                        .findFirst();
                    if (pcOpt.isPresent() && pcOpt.get().getComissaoPercentual() != null) {
                        perc = pcOpt.get().getComissaoPercentual();
                    }
                }
                double commissionForThis = pp.getValorParcela() * perc / 100.0;
                ParcComissaoDoc doc = new ParcComissaoDoc();
                doc.setProposta(proposta);
                doc.setGradeComissao(proposta.getGradeComissao());
                Optional<ParcelaComissao> pcOpt = parcelasComissao.stream()
                        .filter(pc -> pc.getNumeroParcela() == pp.getNumeroParcela())
                        .findFirst();
                pcOpt.ifPresent(doc::setParcelaComissao);
                doc.setNumeroParcela(pp.getNumeroParcela());
                doc.setValorParcela(pp.getValorParcela());
                doc.setDataVencimento(pp.getDataVencimento());
                doc.setRecebido(false);
                doc.setPercentualComissao(perc);
                doc.setValorComissao(commissionForThis);
                docs.add(doc);
            }
        }

        parcComissaoDocRepository.saveAll(docs);
        System.out.println("✅ Documentos de comissão gerados para a proposta ID: " + proposta.getId());
    }

    // Atualizar uma Proposta/Apolice
    @PutMapping("/atualizar/{id}")
public ResponseEntity<String> atualizarProposta(@PathVariable Long id,
                                                @RequestBody PropostaApoliceRequestDTO dto) {
    Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findById(id);
    if (propostaOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    PropostaApolice proposta = propostaOpt.get();

    // Atualiza os dados básicos
    proposta.setNrProposta(dto.nrProposta());
    proposta.setNrApolice(dto.nrApolice());
    proposta.setNrEndosso(dto.nrEndosso());
    proposta.setDataInicioVigencia(dto.dataInicioVigencia());
    proposta.setDataFimVigencia(dto.dataFimVigencia());
    
    // Define o tipo com base no número de endosso e apólice
    String tipoAtualizado;
    if (dto.nrEndosso() != null && !dto.nrEndosso().isBlank()) {
        tipoAtualizado = "Endosso";
    } else if (dto.nrApolice() != null && !dto.nrApolice().isBlank()) {
        tipoAtualizado = "Apolice";
    } else {
        tipoAtualizado = "Proposta";
    }
    proposta.setTipo(tipoAtualizado);
    
    proposta.setDataEmissaoApolice(dto.dataEmissaoApolice());
    proposta.setApoliceEfetivadaEm(dto.apoliceEfetivadaEm());
    proposta.setComissaoPercentual(dto.comissaoPercentual());
    proposta.setDescontoPercentual(dto.descontoPercentual());
    proposta.setQuantidadeParcelas(dto.quantidadeParcelas());
    proposta.setDataPrimeiroPagamento(dto.dataPrimeiroPagamento());
    proposta.setDataBaseParcelas(dto.dataBaseParcelas());
    proposta.setValorPrimeiroPagamento(dto.valorPrimeiroPagamento());
    
    // Atualiza o prêmio total e recalcula o prêmio líquido
    double premioTotal = dto.premioTotal();
    proposta.setPremioTotal(premioTotal);
    double descontoPercentual = dto.descontoPercentual();
    double premioLiquidoCalculado = premioTotal - (premioTotal * descontoPercentual / 100.0);
    proposta.setPremioLiquido(premioLiquidoCalculado);
    
    proposta.setMotivoCancelamentoRecusa(dto.motivoCancelamentoRecusa());
    proposta.setDataCancelamentoRecusa(dto.dataCancelamentoRecusa());
    proposta.setObservacoes(dto.observacoes());
    proposta.setStatus((dto.status() == null || dto.status().isBlank()) ? "Vigente" : dto.status());

    propostaApoliceRepository.save(proposta);
    return ResponseEntity.ok("✅ Proposta/Apolice atualizada com sucesso!");
}
    // Alterar o status
    @PatchMapping("/alterar-status/{id}")
    public ResponseEntity<String> alterarStatus(@PathVariable Long id, @RequestParam String status) {
        return alterarCampoTexto(id, "status", status);
    }

    // Alterar o tipo
    @PatchMapping("/alterar-tipo/{id}")
    public ResponseEntity<String> alterarTipo(@PathVariable Long id, @RequestParam String tipo) {
        return alterarCampoTexto(id, "tipo", tipo);
    }

    // Buscar por número da apólice, proposta ou endosso
    @GetMapping("/buscar")
    public ResponseEntity<List<PropostaApoliceResponseDTO>> buscarPropostas(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String nrProposta) {

        List<PropostaApolice> propostas;

        if (nrProposta != null) {
            Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findByNrProposta(nrProposta);
            if (propostaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            propostas = List.of(propostaOpt.get());
        } else if (status != null && !status.isBlank()) {
            propostas = propostaApoliceRepository.findByStatusIgnoreCase(status);
        } else {
            propostas = propostaApoliceRepository.findPropostasAtivas();
        }

        if (propostas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<PropostaApoliceResponseDTO> dtoList = propostas.stream()
                .map(PropostaApoliceResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    // Marcar Proposta/Apolice como "cancelada" (limpando coleções de parcelas e veículos)
    @PatchMapping("/cancelar/{id}")
    @Transactional
    public ResponseEntity<String> cancelarProposta(@PathVariable Long id,
        @RequestBody Map<String, String> payload) {
    
        Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findById(id);
        if (propostaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PropostaApolice proposta = propostaOpt.get();
    
        // Atualiza os campos de cancelamento
        String motivo = payload.get("motivoCancelamentoRecusa");
        String dataCancelamento = payload.get("dataCancelamentoRecusa");
        proposta.setMotivoCancelamentoRecusa(motivo);
        proposta.setDataCancelamentoRecusa(dataCancelamento);
    
        // Limpa as coleções associadas (parcelas e veículos)
        if (proposta.getParcelas() != null) {
            proposta.getParcelas().clear();
        }
        if (proposta.getVeiculos() != null) {
            proposta.getVeiculos().clear();
        }
        // Remove as parcelas de comissão associadas à proposta
        parcComissaoDocRepository.deleteByPropostaId(proposta.getId());
    
        proposta.setStatus("Cancelada");
        proposta.setCancelado(true);
    
        propostaApoliceRepository.save(proposta);
        return ResponseEntity.ok("Proposta/Apolice cancelada com sucesso!");
    }
    
    // Marcar Proposta/Apolice como "recusada = true"
    @PatchMapping("/recusar/{id}")
    @Transactional
    public ResponseEntity<String> recusarProposta(@PathVariable Long id,
        @RequestBody Map<String, String> payload) {
    
        Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findById(id);
        if (propostaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PropostaApolice proposta = propostaOpt.get();
    
        // Atualiza os campos de recusa
        String motivo = payload.get("motivoCancelamentoRecusa");
        String dataCancelamento = payload.get("dataCancelamentoRecusa");
        proposta.setMotivoCancelamentoRecusa(motivo);
        proposta.setDataCancelamentoRecusa(dataCancelamento);
    
        if (proposta.getParcelas() != null) {
            proposta.getParcelas().clear();
        }
        if (proposta.getVeiculos() != null) {
            proposta.getVeiculos().clear();
        }
        parcComissaoDocRepository.deleteByPropostaId(proposta.getId());
    
        proposta.setStatus("Recusada");
        proposta.setRecusado(true);
    
        propostaApoliceRepository.save(proposta);
        return ResponseEntity.ok("Proposta/Apolice recusada com sucesso!");
    }

    // Marcar Proposta/Apolice como "endossada"
    @PatchMapping("/endossar/{id}")
public ResponseEntity<String> endossarProposta(@PathVariable Long id) {
    Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findById(id);
    if (propostaOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    PropostaApolice proposta = propostaOpt.get();
    // Marcar a proposta como endossada e atualizar o status
    proposta.setEndossado(true);
    proposta.setStatus("Endossada"); // Atualiza o status para "Endossada"
    propostaApoliceRepository.save(proposta);
    return ResponseEntity.ok("Proposta/Apolice marcada como endossada com sucesso!");
}

    // Excluir (DELETE) uma Proposta/Apolice
    @DeleteMapping("/excluir/{id}")
    public ResponseEntity<String> excluirProposta(@PathVariable Long id) {
        Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findById(id);
        if (propostaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        propostaApoliceRepository.delete(propostaOpt.get());
        return ResponseEntity.ok("Proposta/Apolice excluída com sucesso!");
    }

    /**
     * Helper para alterar campos do tipo String (status, tipo).
     */
    private ResponseEntity<String> alterarCampoTexto(Long id, String campo, String valor) {
        Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findById(id);
        if (propostaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        PropostaApolice proposta = propostaOpt.get();
        switch (campo) {
            case "status":
                proposta.setStatus(valor);
                break;
            case "tipo":
                proposta.setTipo(valor);
                break;
            default:
                return ResponseEntity.badRequest().body("Campo inválido para atualização de texto.");
        }
        propostaApoliceRepository.save(proposta);
        return ResponseEntity.ok(campo + " atualizado com sucesso!");
    }

    // Buscar propostas ativas, incluindo documentos de comissão vinculados à proposta
    @GetMapping("/ativas")
    public ResponseEntity<?> getPropostasAtivas() {
        List<PropostaApolice> propostas = propostaApoliceRepository.findPropostasAtivas();
        if (propostas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<PropostaApoliceResponseDTO> propostasDTO = propostas.stream()
            .map(proposta -> {
                List<ParcelaPagamentoDTO> parcelasDTO = parcelaPagamentoRepository.findByPropostaId(proposta.getId()).stream()
                        .map(ParcelaPagamentoDTO::new)
                        .collect(Collectors.toList());
                List<ParcComissaoDocResponseDTO> comissoesDTO = parcComissaoDocRepository.findByPropostaId(proposta.getId()).stream()
                        .map(ParcComissaoDocResponseDTO::new)
                        .collect(Collectors.toList());
                return new PropostaApoliceResponseDTO(proposta, parcelasDTO, proposta.getVeiculos(), proposta.getAnexos(), comissoesDTO);
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(propostasDTO);
    }

    // Buscar Proposta/Apolice por ID
    @GetMapping("/{id}")
    public ResponseEntity<PropostaApoliceResponseDTO> buscarPorId(@PathVariable Long id) {
        Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findById(id);
        if (propostaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        PropostaApolice proposta = propostaOpt.get();
        List<ParcelaPagamentoDTO> parcelasDTO = parcelaPagamentoRepository.findByPropostaId(proposta.getId()).stream()
                .map(ParcelaPagamentoDTO::new)
                .collect(Collectors.toList());
        List<Veiculo> veiculos = proposta.getVeiculos();
        List<AnexoProposta> anexos = proposta.getAnexos();
        List<ParcComissaoDocResponseDTO> comissoesDTO = parcComissaoDocRepository.findByPropostaId(proposta.getId()).stream()
                .map(ParcComissaoDocResponseDTO::new)
                .collect(Collectors.toList());
        PropostaApoliceResponseDTO responseDTO = new PropostaApoliceResponseDTO(proposta, parcelasDTO, veiculos, anexos, comissoesDTO);
        return ResponseEntity.ok(responseDTO);
    }
}
