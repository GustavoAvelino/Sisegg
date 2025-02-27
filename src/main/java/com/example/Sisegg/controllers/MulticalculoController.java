package com.example.Sisegg.controllers;

import com.example.Sisegg.DTO.MulticalculoRequestDTO;
import com.example.Sisegg.DTO.MulticalculoResponseDTO;
import com.example.Sisegg.models.AnexoMulticalculo;
import com.example.Sisegg.models.Cliente;
import com.example.Sisegg.models.Corretora;
import com.example.Sisegg.models.Multicalculo;
import com.example.Sisegg.models.Veiculo;
import com.example.Sisegg.repositories.AnexoMulticalculoRepository;
import com.example.Sisegg.repositories.ClienteRepository;
import com.example.Sisegg.repositories.CorretoraRepository;
import com.example.Sisegg.repositories.MulticalculoRepository;
import com.example.Sisegg.repositories.VeiculoRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/multicalculo")
@CrossOrigin("*")
public class MulticalculoController {

    // Diretório de upload – os PDFs serão salvos aqui
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "uploads" + File.separator;

    @Autowired
    private MulticalculoRepository multicalculoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private CorretoraRepository corretoraRepository;

    @Autowired
    private AnexoMulticalculoRepository anexoMulticalculoRepository;

    @PostMapping
    public ResponseEntity<String> criarMulticalculo(@RequestBody MulticalculoRequestDTO dto) {
        // Valida os campos obrigatórios
        if (dto.getCorretoraId() == null) {
            return ResponseEntity.badRequest().body("Erro: CorretoraId não pode ser nulo.");
        }
        if (dto.getClienteId() == null) {
            return ResponseEntity.badRequest().body("Erro: ClienteId não pode ser nulo.");
        }
        if (dto.getVeiculosIds() == null || dto.getVeiculosIds().isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: É necessário informar pelo menos um veículo.");
        }

        try {
            // Busca corretora, cliente e veículos
            Corretora corretora = corretoraRepository.findById(dto.getCorretoraId())
                    .orElseThrow(() -> new RuntimeException("Erro: Corretora não encontrada para o ID " + dto.getCorretoraId()));

            Cliente cliente = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Erro: Cliente não encontrado para o ID " + dto.getClienteId()));

            Set<Veiculo> veiculos = veiculoRepository.findAllById(dto.getVeiculosIds())
                    .stream().collect(Collectors.toSet());
            if (veiculos.isEmpty()) {
                return ResponseEntity.badRequest().body("Erro: Nenhum veículo encontrado para os IDs fornecidos.");
            }

            // Cria e persiste o Multicalculo
            Multicalculo multicalculo = new Multicalculo(dto, cliente, veiculos, corretora);
            multicalculo = multicalculoRepository.save(multicalculo);

            // Gera o PDF com os dados da cotação
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Exemplo de conteúdo – adicione os dados que desejar
            document.add(new Paragraph("Cotação de Seguro")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(18));
            document.add(new Paragraph("Cliente: " + cliente.getNome()));
            document.add(new Paragraph("Tipo de Seguro: " + multicalculo.getTipoSeguro()));
            document.add(new Paragraph("Vigência: "
                    + multicalculo.getVigenciaInicio() + " a " + multicalculo.getVigenciaFim()));

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            String pdfFileName = "multicalculo_" + multicalculo.getId() + ".pdf";
            String filePath = UPLOAD_DIR + pdfFileName;

            // Salva o PDF no sistema de arquivos
            File pastaUpload = new File(UPLOAD_DIR);
            if (!pastaUpload.exists() && !pastaUpload.mkdirs()) {
                throw new IOException("Erro ao criar diretório de upload.");
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
            }

            // Cria e persiste o anexo
            AnexoMulticalculo anexo = new AnexoMulticalculo(multicalculo, pdfFileName, filePath);
            anexoMulticalculoRepository.save(anexo);

            return ResponseEntity.ok("Multicálculo criado com sucesso e PDF gerado!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar Multicalculo: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MulticalculoResponseDTO> buscarPorId(@PathVariable Long id) {
        Multicalculo multicalculo = multicalculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Multicálculo não encontrado"));
        MulticalculoResponseDTO responseDTO = new MulticalculoResponseDTO(multicalculo);
        return ResponseEntity.ok(responseDTO);
    }


    @GetMapping
    public ResponseEntity<List<MulticalculoResponseDTO>> listarPorCorretora(
            @RequestParam Long corretoraId,
            @RequestParam(required = false) String tipoSeguro) {
        List<MulticalculoResponseDTO> lista = multicalculoRepository.findByCorretoraId(corretoraId)
                .stream().map(MulticalculoResponseDTO::new)
                .collect(Collectors.toList());
        if (tipoSeguro != null && !tipoSeguro.isEmpty()) {
            lista = lista.stream()
                    .filter(mc -> mc.getTipoSeguro().toString().equalsIgnoreCase(tipoSeguro))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(lista);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMulticalculo(@PathVariable Long id) {
        try {
            Multicalculo multicalculo = multicalculoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Multicálculo não encontrado para o ID " + id));

            // Excluir os anexos: apaga os arquivos e os registros no banco
            List<AnexoMulticalculo> anexos = anexoMulticalculoRepository.findByMulticalculoId(id);
            for (AnexoMulticalculo anexo : anexos) {
                File arquivo = new File(anexo.getCaminhoArquivo());
                if (arquivo.exists()) {
                    arquivo.delete();
                }
                anexoMulticalculoRepository.delete(anexo);
            }
            // Remove o multicalculo
            multicalculoRepository.delete(multicalculo);
            return ResponseEntity.ok("Multicálculo e anexos excluídos com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir Multicalculo: " + e.getMessage());
        }
    }

    @GetMapping("/gerar-pdf/{multicalculoId}")
    public ResponseEntity<byte[]> gerarPdf(@PathVariable Long multicalculoId) {
        Multicalculo multicalculo = multicalculoRepository.findById(multicalculoId)
                .orElseThrow(() -> new RuntimeException("Multicálculo não encontrado"));
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Formatter para datas no padrão brasileiro "dd/MM/yyyy"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Cores e fontes
            DeviceRgb azulPersonalizado = new DeviceRgb(4, 204, 204);
            DeviceRgb fundoTitulo = new DeviceRgb(230, 230, 230);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Logo "Sisegg" no canto superior esquerdo
            Paragraph logo = new Paragraph("Sisegg")
                    .setFont(boldFont)
                    .setFontSize(32)
                    .setFontColor(azulPersonalizado)
                    .setFixedPosition(38, pdfDoc.getDefaultPageSize().getTop() - 80, 250);
            document.add(logo);

            // Espaço após o logo
            document.add(new Paragraph("\n\n"));

            // --- Bloco 1: Dados do Seguro ---
            Paragraph seguroTitulo = new Paragraph("Dados do Seguro")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(fundoTitulo)
                    .setMarginBottom(10)
                    .setMarginTop(10);
            document.add(seguroTitulo);

            document.add(new Paragraph("ID Multicálculo: " + multicalculo.getId())
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Tipo de Seguro: "
                    + multicalculo.getTipoSeguro().toString().replaceAll("_", " "))
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Vigência Início: "
                    + multicalculo.getVigenciaInicio().format(formatter))
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Vigência Fim: "
                    + multicalculo.getVigenciaFim().format(formatter))
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("\n"));

            // --- Bloco 2: Dados do Segurado ---
            Paragraph seguradoTitulo = new Paragraph("Dados do Segurado")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(fundoTitulo)
                    .setMarginBottom(10)
                    .setMarginTop(10);
            document.add(seguradoTitulo);

            Cliente cliente = multicalculo.getCliente();
            document.add(new Paragraph("Nome: " + cliente.getNome())
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("CPF/CNPJ: " + cliente.getCnpjCpf())
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("E-mail: " + cliente.getEmail())
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Telefone: " + cliente.getTelefone())
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Sexo: " + cliente.getSexo())
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("\n"));

            // --- Bloco 3: Dados dos Veículos ---
            Paragraph veiculoTitulo = new Paragraph("Dados dos Veículos")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(fundoTitulo)
                    .setMarginBottom(10)
                    .setMarginTop(10);
            document.add(veiculoTitulo);

            Set<Veiculo> veiculos = multicalculo.getVeiculos();
            if (veiculos != null && !veiculos.isEmpty()) {
                for (Veiculo v : veiculos) {
                    Paragraph veiculoPar = new Paragraph()
                            .setFont(regularFont)
                            .setFontSize(12)
                            .add("Placa: " + v.getPlaca() + "\n")
                            .add("Marca: " + v.getMarca() + "\n")
                            .add("Modelo: " + v.getModelo() + "\n")
                            .add("Ano/Modelo: " + v.getAnoFabricacao() + "/" + v.getAnoModelo() + "\n")
                            .add("Código Fipe: " + v.getCodigoFipe() + "\n")
                            .setMarginBottom(8);
                    document.add(veiculoPar);
                }
            } else {
                document.add(new Paragraph("Nenhum veículo informado.")
                        .setFont(regularFont).setFontSize(12));
            }
            document.add(new Paragraph("\n"));

            // --- Bloco 4: Perfil de Risco ---
            Paragraph riscoTitulo = new Paragraph("Perfil de Risco")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(fundoTitulo)
                    .setMarginBottom(10)
                    .setMarginTop(10);
            document.add(riscoTitulo);

            document.add(new Paragraph("Dependente 17 a 26: "
                    + (multicalculo.getDependente17a26() ? "Sim" : "Não"))
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Idade do Dependente Mais Novo: "
                    + multicalculo.getIdadeDependenteMaisNovo())
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Tempo de Utilização dos Dependentes (meses): "
                    + multicalculo.getTempoUtilizacaoDependentes())
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Sexo Residente: "
                    + multicalculo.getSexoResidente().toString().replaceAll("_", " "))
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Residente Principal 18 a 25: "
                    + (multicalculo.getResidentePrincipal1825() ? "Sim" : "Não"))
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Residência do Principal Condutor: "
                    + multicalculo.getPrincipalCondutorResideEm().toString().replaceAll("_", " "))
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Veículo Utilizado para Trabalho: "
                    + multicalculo.getVeiculoUtilizadoTrabalho().toString().replaceAll("_", " "))
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Estacionamento na Residência: "
                    + multicalculo.getEstacionamentoResidencia().toString().replaceAll("_", " "))
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Veículo Utilizado para Faculdade: "
                    + multicalculo.getVeiculoUtilizadoFaculdade().toString().replaceAll("_", " "))
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Quantidade de Veículos na Residência: "
                    + multicalculo.getQuantidadeVeiculosResidencia())
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Frequência de Utilização para Trabalho (vezes/semana): "
                    + multicalculo.getFrequenciaUtilizacaoTrabalho())
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Média de KM Rodados por Mês: "
                    + multicalculo.getMediaKmMes())
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("Principal Condutor Roubado nos Últimos 2 Anos: "
                    + (multicalculo.getPrincipalCondutorRoubado2Anos() ? "Sim" : "Não"))
                    .setFont(regularFont).setFontSize(12));
            document.add(new Paragraph("\n"));

            // --- Bloco 5: Coberturas (em forma de tabela) ---
            Paragraph coberturasTitulo = new Paragraph("Coberturas")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(fundoTitulo)
                    .setMarginBottom(10)
                    .setMarginTop(10);
            document.add(coberturasTitulo);

            // Criamos uma tabela com 3 colunas: Descrição, Valor, Prêmio Líquido
            Table coverageTable = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                    .useAllAvailableWidth();

            coverageTable.addHeaderCell(new Cell().add(new Paragraph("Cobertura").setFont(boldFont)));
            coverageTable.addHeaderCell(new Cell().add(new Paragraph("Valor").setFont(boldFont)));
            coverageTable.addHeaderCell(new Cell().add(new Paragraph("Prêmio Líquido").setFont(boldFont)));

            // Usamos BigDecimal para cálculo financeiro
            BigDecimal totalPremioLiquido = BigDecimal.ZERO;

            totalPremioLiquido = totalPremioLiquido.add(
                    addCoverageRow(coverageTable, "Danos Materiais",
                            multicalculo.getDanosMateriais() != null ? multicalculo.getDanosMateriais() : BigDecimal.ZERO,
                            regularFont)
            );
            totalPremioLiquido = totalPremioLiquido.add(
                    addCoverageRow(coverageTable, "Danos Corporais",
                            multicalculo.getDanosCorporais() != null ? multicalculo.getDanosCorporais() : BigDecimal.ZERO,
                            regularFont)
            );
            totalPremioLiquido = totalPremioLiquido.add(
                    addCoverageRow(coverageTable, "Danos Morais",
                            multicalculo.getDanosMorais() != null ? multicalculo.getDanosMorais() : BigDecimal.ZERO,
                            regularFont)
            );
            totalPremioLiquido = totalPremioLiquido.add(
                    addCoverageRow(coverageTable, "Morte Passageiro",
                            multicalculo.getMortePassageiro() != null ? multicalculo.getMortePassageiro() : BigDecimal.ZERO,
                            regularFont)
            );
            totalPremioLiquido = totalPremioLiquido.add(
                    addCoverageRow(coverageTable, "Invalidez Permanente Passageiro",
                            multicalculo.getInvalidezPermanentePassageiro() != null
                                    ? multicalculo.getInvalidezPermanentePassageiro() : BigDecimal.ZERO,
                            regularFont)
            );
            totalPremioLiquido = totalPremioLiquido.add(
                    addCoverageRow(coverageTable, "Despesas Hospitalares",
                            multicalculo.getDespesasHospitalares() != null ? multicalculo.getDespesasHospitalares() : BigDecimal.ZERO,
                            regularFont)
            );

            // Linha final para exibir o total do prêmio líquido
            Cell totalLabelCell = new Cell(1, 2)
                    .add(new Paragraph("Total do Prêmio Líquido").setFont(boldFont));
            coverageTable.addCell(totalLabelCell);

            // Ajustamos para exibir com duas casas decimais
            String totalPremioStr = totalPremioLiquido.setScale(2, RoundingMode.HALF_UP).toPlainString();
            coverageTable.addCell(new Cell().add(new Paragraph(totalPremioStr).setFont(boldFont)));

            document.add(coverageTable);

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            // Define o nome do arquivo PDF usando o nome do segurado e o ID do Multicalculo
            String clienteNome = multicalculo.getCliente().getNome().replaceAll("\\s+", "_");
            String pdfFileName = "cotacao_multicalculo_" + clienteNome + "_" + multicalculo.getId() + ".pdf";
            String filePath = UPLOAD_DIR + pdfFileName;

            File pastaUpload = new File(UPLOAD_DIR);
            if (!pastaUpload.exists() && !pastaUpload.mkdirs()) {
                throw new IOException("Erro ao criar diretório de upload.");
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
            }

            // Cria e persiste o anexo do multicalculo com o PDF gerado
            AnexoMulticalculo anexo = new AnexoMulticalculo(multicalculo, pdfFileName, filePath);
            anexoMulticalculoRepository.save(anexo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", pdfFileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Adiciona uma linha à tabela de coberturas usando BigDecimal.
     * Aplica 4,5% sobre o valor para calcular o prêmio líquido.
     * Limita o valor a 2 casas decimais.
     */
    private BigDecimal addCoverageRow(Table table, String coverage, BigDecimal value, PdfFont font) {
        // Prêmio líquido é 4,5% do valor
        BigDecimal premioLiquido = value.multiply(new BigDecimal("0.045"));

        // Definimos cada valor para ter no máximo duas casas decimais
        BigDecimal valueScaled = value.setScale(2, RoundingMode.HALF_UP);
        BigDecimal premioLiquidoScaled = premioLiquido.setScale(2, RoundingMode.HALF_UP);

        // Adiciona célula com a descrição da cobertura
        table.addCell(new Cell().add(new Paragraph(coverage).setFont(font)));
        // Adiciona célula com o valor (duas casas decimais)
        table.addCell(new Cell().add(new Paragraph(valueScaled.toPlainString()).setFont(font)));
        // Adiciona célula com o prêmio líquido (duas casas decimais)
        table.addCell(new Cell().add(new Paragraph(premioLiquidoScaled.toPlainString()).setFont(font)));

        return premioLiquidoScaled;
    }
}
