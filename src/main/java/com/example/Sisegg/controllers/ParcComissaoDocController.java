package com.example.Sisegg.controllers;

import com.example.Sisegg.DTO.ParcComissaoDocRequestDTO;
import com.example.Sisegg.DTO.ParcComissaoDocResponseDTO;
import com.example.Sisegg.models.ParcComissaoDoc;
import com.example.Sisegg.models.GradeComissao;
import com.example.Sisegg.models.ParcelaComissao;
import com.example.Sisegg.models.PropostaApolice;
import com.example.Sisegg.repositories.ParcComissaoDocRepository;
import com.example.Sisegg.repositories.GradeComissaoRepository;
import com.example.Sisegg.repositories.ParcelaComissaoRepository;
import com.example.Sisegg.repositories.PropostaApoliceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("parc-comissao-doc")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ParcComissaoDocController {

    @Autowired
    private ParcComissaoDocRepository parcComissaoDocRepository;

    @Autowired
    private GradeComissaoRepository gradeComissaoRepository;
   
    @Autowired
    private PropostaApoliceRepository propostaApoliceRepository;
    
    @Autowired
    private ParcelaComissaoRepository parcelaComissaoRepository;

    // ✅ Criar uma nova parcela do documento
    @PostMapping("/save")
    public ResponseEntity<String> saveParcela(@RequestBody ParcComissaoDocRequestDTO dto) {
        // Busque a GradeComissao
        Optional<GradeComissao> gradeOpt = gradeComissaoRepository.findById(dto.gradeComissaoId());
        if (gradeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Grade de comissão não encontrada.");
        }
        
        // Busque a PropostaApolice usando o ID enviado no DTO
        Optional<PropostaApolice> propostaOpt = propostaApoliceRepository.findById(dto.propostaId());
        if (propostaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Proposta não encontrada.");
        }
        PropostaApolice proposta = propostaOpt.get();
        
        // Busque a ParcelaComissao, se houver
        ParcelaComissao parcelaComissao = null;
        if (dto.parcelaComissaoId() != null) {
            Optional<ParcelaComissao> parcelaOpt = parcelaComissaoRepository.findById(dto.parcelaComissaoId());
            parcelaComissao = parcelaOpt.orElse(null);
        }
        
        // Crie a nova parcela de comissão, passando também a proposta
        ParcComissaoDoc novaParcela = new ParcComissaoDoc(dto, gradeOpt.get(), parcelaComissao, proposta);
        parcComissaoDocRepository.save(novaParcela);

        return ResponseEntity.ok("Parcela do documento salva com sucesso!");
    }
    
    // Buscar parcelas de comissão pelo id da proposta
    @GetMapping("/{propostaId}")
    public ResponseEntity<List<ParcComissaoDocResponseDTO>> getParcelasByProposta(@PathVariable Long propostaId) {
        List<ParcComissaoDoc> docs = parcComissaoDocRepository.findByPropostaId(propostaId);
        DateTimeFormatter formatterBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<ParcComissaoDocResponseDTO> response = docs.stream()
                .map(doc -> {
                    String data = doc.getDataVencimento();
                    if (data != null && data.contains("-")) { // se estiver em ISO
                        LocalDate ld = LocalDate.parse(data);
                        data = ld.format(formatterBR);
                    }
                    return new ParcComissaoDocResponseDTO(doc, data);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ✅ Atualizar uma parcela do documento
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateParcela(@PathVariable Long id, @RequestBody ParcComissaoDocRequestDTO dto) {
        Optional<ParcComissaoDoc> parcelaOpt = parcComissaoDocRepository.findById(id);
        if (parcelaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ParcComissaoDoc parcela = parcelaOpt.get();
        parcela.setValorParcela(dto.valorParcela());
        parcela.setPercentualComissao(dto.percentualComissao());
        parcela.setValorComissao(dto.valorComissao());
        parcela.setDataVencimento(dto.dataVencimento());
        parcela.setRecebido(dto.recebido());

        parcComissaoDocRepository.save(parcela);
        return ResponseEntity.ok("Parcela do documento atualizada com sucesso!");
    }

    // ✅ Marcar uma parcela como recebida
    @PatchMapping("/marcar-recebido/{id}")
    public ResponseEntity<String> marcarParcelaComoRecebida(@PathVariable Long id) {
        Optional<ParcComissaoDoc> parcelaOpt = parcComissaoDocRepository.findById(id);
        
        if (parcelaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parcela não encontrada.");
        }

        ParcComissaoDoc parcela = parcelaOpt.get();
        parcela.setRecebido(true); // Marca como recebida
        parcComissaoDocRepository.save(parcela);

        return ResponseEntity.ok("Parcela marcada como recebida com sucesso!");
    }

    // ✅ Deletar uma parcela do documento
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteParcela(@PathVariable Long id) {
        Optional<ParcComissaoDoc> parcelaOpt = parcComissaoDocRepository.findById(id);
        if (parcelaOpt.isPresent()) {
            parcComissaoDocRepository.deleteById(id);
            return ResponseEntity.ok("Parcela do documento deletada com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parcela do documento não encontrada.");
    }

    @GetMapping("/search")
    public ResponseEntity<List<ParcComissaoDocResponseDTO>> searchContas(
            @RequestParam(required = false) String produtor,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String data,
            @RequestParam(required = false) String status,
            @RequestParam Long corretoraId) {
        List<ParcComissaoDoc> docs = parcComissaoDocRepository.findAll(); // Exemplo: filtra in-memory
        
        // Formatter para o formato brasileiro
        DateTimeFormatter formatterBR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        List<ParcComissaoDoc> filtrados = docs.stream().filter(doc -> {
            // Filtra pela corretora do usuário logado
            if (doc.getProposta() == null || doc.getProposta().getCorretora() == null 
                || !doc.getProposta().getCorretora().getId().equals(corretoraId)) {
                return false;
            }
            // Filtra somente se o tipo for apolice ou endosso
            if (doc.getProposta().getTipo() != null) {
                String tipo = doc.getProposta().getTipo().toLowerCase();
                if (!(tipo.contains("apolice") || tipo.contains("endosso"))) {
                    return false;
                }
            } else {
                return false;
            }
            // Filtro por produtor
            if (produtor != null && !produtor.isEmpty()) {
                if (doc.getProposta().getProdutor() == null ||
                    doc.getProposta().getProdutor().getNome() == null ||
                    !doc.getProposta().getProdutor().getNome().toLowerCase().contains(produtor.toLowerCase())) {
                    return false;
                }
            }
            // Filtro por documento: verifica se o parâmetro aparece em nrApolice ou nrEndosso
            if (documento != null && !documento.isEmpty()) {
                String docQuery = documento.toLowerCase();
                boolean docMatch = false;
                if (doc.getProposta().getNrApolice() != null &&
                    doc.getProposta().getNrApolice().toLowerCase().contains(docQuery)) {
                    docMatch = true;
                }
                if (doc.getProposta().getNrEndosso() != null &&
                    doc.getProposta().getNrEndosso().toLowerCase().contains(docQuery)) {
                    docMatch = true;
                }
                if (!docMatch) {
                    return false;
                }
            }
            // Filtro por data de vencimento (converte ambas para LocalDate para comparação)
            if (data != null && !data.isEmpty()) {
                LocalDate queryDate;
                try {
                    // Tenta converter a data de pesquisa assumindo ISO
                    queryDate = LocalDate.parse(data);
                } catch (Exception e) {
                    // Se não for ISO, tenta o formato brasileiro
                    queryDate = LocalDate.parse(data, formatterBR);
                }
                String docDataStr = doc.getDataVencimento();
                if (docDataStr == null || docDataStr.isEmpty()) {
                    return false;
                }
                LocalDate docDate;
                try {
                    if (docDataStr.contains("-")) {
                        docDate = LocalDate.parse(docDataStr);
                    } else {
                        docDate = LocalDate.parse(docDataStr, formatterBR);
                    }
                } catch (Exception e) {
                    return false;
                }
                if (!docDate.equals(queryDate)) {
                    return false;
                }
            }
            // Filtro por status
            if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("Todos")) {
                if (status.equalsIgnoreCase("Pago") && !doc.getRecebido()) {
                    return false;
                } else if (status.equalsIgnoreCase("Pendente") && doc.getRecebido()) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    
        // Se não houver resultados, retorna lista vazia (200 OK)
        // Ordena por data de vencimento decrescente.
        filtrados.sort((a, b) -> {
            LocalDate dateA;
            LocalDate dateB;
            try {
                if (a.getDataVencimento().contains("-")) {
                    dateA = LocalDate.parse(a.getDataVencimento());
                } else {
                    dateA = LocalDate.parse(a.getDataVencimento(), formatterBR);
                }
                if (b.getDataVencimento().contains("-")) {
                    dateB = LocalDate.parse(b.getDataVencimento());
                } else {
                    dateB = LocalDate.parse(b.getDataVencimento(), formatterBR);
                }
            } catch (Exception e) {
                return 0;
            }
            return dateB.compareTo(dateA);
        });
        
        // Mapeia os documentos para o DTO, convertendo a data para o formato brasileiro se necessário
        List<ParcComissaoDocResponseDTO> response = filtrados.stream()
                .map(doc -> {
                    String formattedDate = doc.getDataVencimento();
                    if (formattedDate != null && formattedDate.contains("-")) {
                        LocalDate ld = LocalDate.parse(formattedDate);
                        formattedDate = ld.format(formatterBR);
                    }
                    return new ParcComissaoDocResponseDTO(doc, formattedDate);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
