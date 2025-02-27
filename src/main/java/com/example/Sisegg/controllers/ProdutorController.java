package com.example.Sisegg.controllers;

import com.example.Sisegg.DTO.ComissaoProdutorDTO;
import com.example.Sisegg.DTO.ProdutorRequestDTO;
import com.example.Sisegg.DTO.ProdutorResponseDTO;
import com.example.Sisegg.models.Produtor;
import com.example.Sisegg.models.Corretora;
import com.example.Sisegg.models.ParcComissaoDoc;
import com.example.Sisegg.repositories.CorretoraRepository;
import com.example.Sisegg.repositories.ProdutorRepository;
import com.example.Sisegg.repositories.ParcComissaoDocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("produtor")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProdutorController {
    @Autowired
    private ParcComissaoDocRepository parcComissaoDocRepository;

   
    @Autowired
    private ProdutorRepository produtorRepository;

     @Autowired
    private CorretoraRepository corretoraRepository;

    // CREATE - Salvar um novo Produtor
    @PostMapping("/save")
    public ResponseEntity<String> saveProdutor(@RequestBody ProdutorRequestDTO data) {
    if (data.corretoraId() == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("CorretoraId é obrigatório para cadastrar um produtor.");
    }

    Optional<Corretora> corretoraOpt = corretoraRepository.findById(data.corretoraId());
    if (corretoraOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Corretora com o ID fornecido não encontrada.");
    }

    // Criando produtor e associando a corretora corretamente
    Produtor produtor = new Produtor(data);
    produtor.setCorretora(corretoraOpt.get()); // 🚀 Agora a corretora será salva corretamente!

    produtorRepository.save(produtor);
    return ResponseEntity.status(HttpStatus.CREATED).body("Produtor criado com sucesso!");
}

    // READ ALL - Buscar todos os produtores
    @GetMapping
    public ResponseEntity<List<ProdutorResponseDTO>> getAll() {
        List<Produtor> lista = produtorRepository.findAll();
        List<ProdutorResponseDTO> produtores = lista.stream().map(ProdutorResponseDTO::new).toList();
        return ResponseEntity.ok(produtores);
    }

    // READ SINGLE - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProdutorResponseDTO> getById(@PathVariable Long id) {
        Optional<Produtor> produtorOpt = produtorRepository.findById(id);
        return produtorOpt.map(produtor -> ResponseEntity.ok(new ProdutorResponseDTO(produtor)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // UPDATE - Atualizar um Produtor
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateProdutor(@PathVariable Long id, @RequestBody ProdutorRequestDTO data) {
        Optional<Produtor> produtorOptional = produtorRepository.findById(id);

        if (produtorOptional.isPresent()) {
            Produtor produtor = produtorOptional.get();
            produtor.setNome(data.nome());
            produtor.setCpf(data.cpf());
            produtor.setCnpj(data.cnpj());
            produtor.setDataNascimento(data.dataNascimento());
            produtor.setSexo(data.sexo());
            produtor.setEmail(data.email());
            produtor.setTelefone(data.telefone());
            produtor.setEndereco(data.endereco());
            produtor.setImposto(data.imposto());
            produtor.setRepasse(data.repasse());
            produtor.setRepasseSobre(data.repasseSobre());
            produtor.setFormaRepasse(data.formaRepasse());

            produtorRepository.save(produtor);
            return ResponseEntity.status(HttpStatus.OK).body("Produtor atualizado com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produtor não encontrado.");
    }

    // DELETE - Remover um Produtor
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProdutor(@PathVariable Long id) {
        Optional<Produtor> produtorOpt = produtorRepository.findById(id);
        if (produtorOpt.isPresent()) {
            produtorRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Produtor deletado com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produtor não encontrado.");
    }

    // SEARCH - Filtrar por ID, CPF, CNPJ ou Nome
    @GetMapping("/search")
    public ResponseEntity<List<ProdutorResponseDTO>> searchProdutor(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long corretoraId
    ) {
        List<Produtor> produtores;

        if (id != null) {
            Optional<Produtor> produtorOpt = produtorRepository.findById(id);
            if (produtorOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            produtores = List.of(produtorOpt.get());
        }
        else if (cpf != null && !cpf.isBlank()) {
            produtores = produtorRepository.findByCpf(cpf);
        }
        else if (cnpj != null && !cnpj.isBlank()) {
            produtores = produtorRepository.findByCnpj(cnpj);
        }
        else if (nome != null && !nome.isBlank()) {
            produtores = produtorRepository.findByNomeContainingIgnoreCase(nome);
        }
        else {
            produtores = produtorRepository.findAll();
        }

        if (corretoraId != null) {
            produtores = produtores.stream()
                    .filter(c -> c.getCorretora() != null && c.getCorretora().getId().equals(corretoraId))
                    .toList();
        }

        if (produtores.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<ProdutorResponseDTO> dtoList = produtores.stream().map(ProdutorResponseDTO::new).toList();
        return ResponseEntity.ok(dtoList);
    }

     @GetMapping("/relatorio-comissao/{produtorId}")
public ResponseEntity<List<ComissaoProdutorDTO>> relatorioComissaoProdutor(@PathVariable Long produtorId) {

    // 1) Carregar todas as parcelas que pertencem ao produtor solicitado
    List<ParcComissaoDoc> todasParcelas = parcComissaoDocRepository.findAll();
    List<ParcComissaoDoc> filtradas = todasParcelas.stream()
        .filter(doc -> doc.getProposta() != null
            && doc.getProposta().getProdutor() != null
            && doc.getProposta().getProdutor().getId().equals(produtorId))
        .toList();

    if (filtradas.isEmpty()) {
        // Você pode retornar lista vazia (200 OK) ou 404, conforme sua preferência
        return ResponseEntity.ok(List.of());
    }

    // 2) Agrupar as parcelas pelo "id" da Proposta (cada "documento" é um grupo)
    //    Assim, poderemos somar a comissão total da corretora para cada documento.
    Map<Long, List<ParcComissaoDoc>> parcelasPorProposta = filtradas.stream()
        .collect(Collectors.groupingBy(doc -> doc.getProposta().getId()));

    // 3) Montar a lista final de ComissaoProdutorDTO
    List<ComissaoProdutorDTO> resultado = new ArrayList<>();

    // Varremos cada grupo (ou seja, cada Proposta/Documento)
    for (Map.Entry<Long, List<ParcComissaoDoc>> entry : parcelasPorProposta.entrySet()) {
        List<ParcComissaoDoc> docsDesteDocumento = entry.getValue();
        if (docsDesteDocumento.isEmpty()) continue;

        // Pegamos a primeira parcela só para ler informações comuns:
        ParcComissaoDoc docExemplo = docsDesteDocumento.get(0);
        var proposta = docExemplo.getProposta();
        var produtor = proposta.getProdutor();

        // Lê o repasse e forma de cálculo
        double repasse = (produtor.getRepasse() != null) ? produtor.getRepasse() : 0.0;
        String repasseSobre = (produtor.getRepasseSobre() != null) 
                                ? produtor.getRepasseSobre() : "Comissão Corretora";
        String formaRepasse = (produtor.getFormaRepasse() != null) 
                                ? produtor.getFormaRepasse() : "Antecipado 1 Parcela";

        // Se "Premio Líquido", a base de cálculo será o valor do prêmio da Proposta
        // Se "Comissão Corretora", precisamos da soma total de valorComissao de todas as parcelas
        Double premioLiquido = proposta.getPremioLiquido(); // ajuste se tiver outro nome
        double somaComissaoCorretora = docsDesteDocumento.stream()
            .mapToDouble(ParcComissaoDoc::getValorComissao)
            .sum();

        double baseCalculo;
        if ("Premio Líquido".equalsIgnoreCase(repasseSobre)) {
            baseCalculo = (premioLiquido != null) ? premioLiquido : 0.0;
        } else { // "Comissão Corretora"
            baseCalculo = somaComissaoCorretora;
        }

        // "comissaoCompleta" é o valor total que o produtor tem direito nesse documento
        double comissaoCompleta = baseCalculo * (repasse / 100.0);

        // Aplicar a lógica de "formaRepasse":
        // - Antecipado 1 Parcela: toda a comissãoCompleta na 1ª parcela, demais 0
        // - Antecipado na Parcela: ratear igualmente entre as parcelas
        // - Antecipado Emissão da Apólice: toda na 1ª parcela (exemplo), demais 0
        int totalParcelasDoc = docsDesteDocumento.size(); 
        // ou use proposta.getQuantidadeParcelas() se for confiável

        // Montamos o DTO para cada parcela desse documento
        for (ParcComissaoDoc doc : docsDesteDocumento) {
            int numeroParcela = doc.getNumeroParcela();

            double comissaoProdutorEstaParcela;

            switch (formaRepasse) {
                case "Antecipado 1 Parcela":
                    // Se for a 1ª parcela, paga tudo, se não, 0
                    if (numeroParcela == 1) {
                        comissaoProdutorEstaParcela = comissaoCompleta;
                    } else {
                        comissaoProdutorEstaParcela = 0.0;
                    }
                    break;

                case "Antecipado na Parcela":
                    // Ratear entre todas as parcelas do documento
                    comissaoProdutorEstaParcela = comissaoCompleta / totalParcelasDoc;
                    break;

                case "Antecipado Emissão da Apólice":
                    // Exemplo: se for a 1ª parcela, paga tudo, se não, 0
                    // (outra lógica poderia ser usar dataEmissao, etc.)
                    if (numeroParcela == 1) {
                        comissaoProdutorEstaParcela = comissaoCompleta;
                    } else {
                        comissaoProdutorEstaParcela = 0.0;
                    }
                    break;

                default:
                    // Caso não identificado, assumimos pagar normal
                    comissaoProdutorEstaParcela = comissaoCompleta / totalParcelasDoc;
                    break;
            }

            // Determina o "numeroDocumento" e "tipoDocumento"
            String numeroDocumento;
            if (proposta.getNrEndosso() != null && !proposta.getNrEndosso().isEmpty()) {
                numeroDocumento = proposta.getNrEndosso();
            } else if (proposta.getNrApolice() != null && !proposta.getNrApolice().isEmpty()) {
                numeroDocumento = proposta.getNrApolice();
            } else if (proposta.getNrProposta() != null && !proposta.getNrProposta().isEmpty()) {
                numeroDocumento = proposta.getNrProposta();
            } else {
                numeroDocumento = "-";
            }

            String tipoDocumento;
            if (proposta.getNrEndosso() != null && !proposta.getNrEndosso().isEmpty()) {
                tipoDocumento = "Endosso";
            } else if (proposta.getNrApolice() != null && !proposta.getNrApolice().isEmpty()) {
                tipoDocumento = "Apólice";
            } else {
                tipoDocumento = "Proposta";
            }

            // Monta o DTO final para cada parcela
            ComissaoProdutorDTO dto = new ComissaoProdutorDTO(
                doc.getId(),
                numeroDocumento,
                doc.getDataVencimento(),
                doc.getNumeroParcela(),
                // Informativo: quanto era o prêmio líquido
                premioLiquido != null ? premioLiquido : 0.0,
                // Informativo: quanto era a soma total da comissão corretora
                somaComissaoCorretora,
                // Quanto efetivamente essa parcela paga ao produtor
                comissaoProdutorEstaParcela,

                repasseSobre,
                repasse,
                formaRepasse,
                tipoDocumento,
                produtor.getNome()
            );

            resultado.add(dto);
        }
    }

    // 4) Retornar a lista com todas as parcelas de todos os documentos
    return ResponseEntity.ok(resultado);
}

}
