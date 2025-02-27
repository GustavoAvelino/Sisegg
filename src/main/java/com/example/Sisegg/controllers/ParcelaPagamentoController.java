package com.example.Sisegg.controllers;

import com.example.Sisegg.DTO.ParcelaPagamentoRequestDTO;
import com.example.Sisegg.DTO.ParcelaPagamentoResponseDTO;
import com.example.Sisegg.models.ParcelaPagamento;
import com.example.Sisegg.models.PropostaApolice;
import com.example.Sisegg.repositories.ParcelaPagamentoRepository;
import com.example.Sisegg.repositories.PropostaApoliceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/parcela-pagamento")
@CrossOrigin("*")
public class ParcelaPagamentoController {

    @Autowired
    private ParcelaPagamentoRepository parcelaPagamentoRepository;

    @Autowired
    private PropostaApoliceRepository propostaApoliceRepository;

    // CREATE
    @PostMapping("/save")
    public ResponseEntity<String> saveParcela(@RequestBody ParcelaPagamentoRequestDTO dto) {
        Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findById(dto.propostaId());
        if (propostaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Proposta não encontrada.");
        }
        PropostaApolice proposta = propostaOpt.get();

        ParcelaPagamento nova = new ParcelaPagamento();
        nova.setProposta(proposta);
        nova.setNumeroParcela(dto.numeroParcela());
        nova.setValorParcela(dto.valorParcela());
        nova.setDataVencimento(dto.dataVencimento());
        nova.setPago(dto.pago() != null ? dto.pago() : false);

        parcelaPagamentoRepository.save(nova);
        return ResponseEntity.ok("Conta a Pagar salva com sucesso!");
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateParcela(@PathVariable Long id, @RequestBody ParcelaPagamentoRequestDTO dto) {
        Optional<ParcelaPagamento> parcelaOpt = parcelaPagamentoRepository.findById(id);
        if (parcelaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parcela não encontrada para atualizar.");
        }
        ParcelaPagamento parcela = parcelaOpt.get();

        parcela.setNumeroParcela(dto.numeroParcela());
        parcela.setValorParcela(dto.valorParcela());
        parcela.setDataVencimento(dto.dataVencimento());
        parcela.setPago(dto.pago() != null ? dto.pago() : false);

        parcelaPagamentoRepository.save(parcela);
        return ResponseEntity.ok("Conta a Pagar atualizada com sucesso!");
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteParcela(@PathVariable Long id) {
        Optional<ParcelaPagamento> parcelaOpt = parcelaPagamentoRepository.findById(id);
        if (parcelaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parcela não encontrada para exclusão.");
        }
        parcelaPagamentoRepository.deleteById(id);
        return ResponseEntity.ok("Conta a Pagar excluída com sucesso!");
    }

    // SEARCH
    @GetMapping("/search")
    public ResponseEntity<List<ParcelaPagamentoResponseDTO>> searchContas(
            @RequestParam(required = false) String segurado,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String data,
            @RequestParam(required = false) String status,
            @RequestParam Long corretoraId
    ) {
        List<ParcelaPagamento> todas = parcelaPagamentoRepository.findAll();
        // Definindo os formatadores para ISO e para o formato brasileiro
        DateTimeFormatter formatterISO = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter formatterBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<ParcelaPagamento> filtradas = todas.stream().filter(parc -> {
            // 1) Filtra por corretora
            if (parc.getProposta() == null ||
                parc.getProposta().getCorretora() == null ||
                !parc.getProposta().getCorretora().getId().equals(corretoraId)) {
                return false;
            }
            // 2) Filtra apenas se o tipo for "apólice" ou "endosso"
            if (parc.getProposta().getTipo() != null) {
                String tipoProp = parc.getProposta().getTipo().toLowerCase();
                if (!(tipoProp.contains("apolice") || tipoProp.contains("endosso"))) {
                    return false;
                }
            } else {
                return false;
            }
            // 3) Filtro por segurado
            if (segurado != null && !segurado.isEmpty()) {
                if (parc.getProposta().getCliente() == null ||
                    parc.getProposta().getCliente().getNome() == null ||
                    !parc.getProposta().getCliente().getNome().toLowerCase().contains(segurado.toLowerCase())) {
                    return false;
                }
            }
            // 4) Filtro por documento (nrApolice, nrEndosso ou nrProposta)
            if (documento != null && !documento.isEmpty()) {
                String docQuery = documento.toLowerCase();
                boolean docMatch = false;
                if (parc.getProposta().getNrApolice() != null &&
                    parc.getProposta().getNrApolice().toLowerCase().contains(docQuery)) {
                    docMatch = true;
                }
                if (parc.getProposta().getNrEndosso() != null &&
                    parc.getProposta().getNrEndosso().toLowerCase().contains(docQuery)) {
                    docMatch = true;
                }
                if (parc.getProposta().getNrProposta() != null &&
                    parc.getProposta().getNrProposta().toLowerCase().contains(docQuery)) {
                    docMatch = true;
                }
                if (!docMatch) {
                    return false;
                }
            }
            // 5) Filtro por data de vencimento
            if (data != null && !data.isEmpty()) {
                LocalDate queryDate;
                try {
                    queryDate = LocalDate.parse(data, formatterISO);
                } catch (Exception e) {
                    queryDate = LocalDate.parse(data, formatterBR);
                }
                String parcDateStr = parc.getDataVencimento();
                if (parcDateStr == null || parcDateStr.isEmpty()) {
                    return false;
                }
                LocalDate parcDate;
                try {
                    if (parcDateStr.contains("-")) {
                        parcDate = LocalDate.parse(parcDateStr, formatterISO);
                    } else {
                        parcDate = LocalDate.parse(parcDateStr, formatterBR);
                    }
                } catch (Exception e) {
                    return false;
                }
                if (!parcDate.equals(queryDate)) {
                    return false;
                }
            }
            // 6) Filtro por status
            if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("Todos")) {
                if (status.equalsIgnoreCase("Pago") && !parc.getPago()) {
                    return false;
                } else if (status.equalsIgnoreCase("Pendente") && parc.getPago()) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        // Ordena por data de vencimento decrescente
        filtradas.sort((a, b) -> {
            LocalDate dateA;
            LocalDate dateB;
            try {
                if (a.getDataVencimento().contains("-")) {
                    dateA = LocalDate.parse(a.getDataVencimento(), formatterISO);
                } else {
                    dateA = LocalDate.parse(a.getDataVencimento(), formatterBR);
                }
                if (b.getDataVencimento().contains("-")) {
                    dateB = LocalDate.parse(b.getDataVencimento(), formatterISO);
                } else {
                    dateB = LocalDate.parse(b.getDataVencimento(), formatterBR);
                }
            } catch (Exception e) {
                return 0;
            }
            return dateB.compareTo(dateA);
        });

        // Mapeia os resultados para o DTO, convertendo sempre para o formato brasileiro
        List<ParcelaPagamentoResponseDTO> dtos = filtradas.stream()
                .map(p -> {
                    String formattedDate = p.getDataVencimento();
                    if (formattedDate != null && formattedDate.contains("-")) {
                        LocalDate ld = LocalDate.parse(formattedDate, formatterISO);
                        formattedDate = ld.format(formatterBR);
                    }
                    return new ParcelaPagamentoResponseDTO(p, formattedDate);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ========= MÉTODOS QUE JÁ EXISTIAM ==========

    // Exemplo de gerar parcelas automaticamente ao criar uma apólice
    @PostMapping("/gerar/{propostaId}")
    public ResponseEntity<String> gerarParcelas(@PathVariable Long propostaId) {
        Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findById(propostaId);
        if (propostaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Proposta não encontrada.");
        }

        PropostaApolice proposta = propostaOpt.get();
        List<ParcelaPagamento> parcelas = new ArrayList<>();

        LocalDate dataBase = LocalDate.parse(proposta.getDataBaseParcelas());

        // Primeira parcela com valor personalizado
        parcelas.add(new ParcelaPagamento(
            proposta,
            1,
            proposta.getValorPrimeiroPagamento(),
            proposta.getDataPrimeiroPagamento(),
            false
        ));

        double valorParcelasRestantes = (proposta.getPremioTotal() - proposta.getValorPrimeiroPagamento())
                                        / (proposta.getQuantidadeParcelas() - 1);

        for (int i = 2; i <= proposta.getQuantidadeParcelas(); i++) {
            String dataVencimento = dataBase.plusMonths(i - 1).toString();
            parcelas.add(new ParcelaPagamento(
                proposta,
                i,
                valorParcelasRestantes,
                dataVencimento,
                false
            ));
        }

        parcelaPagamentoRepository.saveAll(parcelas);
        return ResponseEntity.ok("Parcelas geradas com sucesso!");
    }

    // Buscar parcelas de uma apólice específica
    @GetMapping("/{propostaId}")
    public ResponseEntity<List<ParcelaPagamento>> getParcelasByProposta(@PathVariable Long propostaId) {
        List<ParcelaPagamento> parcelas = parcelaPagamentoRepository.findByPropostaId(propostaId);
        if (parcelas.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(parcelas);
    }
}
