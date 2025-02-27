package com.example.Sisegg.services;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import com.example.Sisegg.DTO.ImportacaoDTO;

@Service
public class ImportacaoService {

    public ImportacaoDTO processarArquivo(MultipartFile file) throws Exception {
        // 1) Cria arquivo temporário
        File tempFile = File.createTempFile("upload", ".pdf");
        file.transferTo(tempFile);

        // 2) Extrai o texto do PDF
        String textoExtraido = extrairTextoDoPDF(tempFile);

        // 3) Decide se é novo ou antigo
        ImportacaoDTO dto;
        // Se tiver "No Proposta:" ou "Nº Proposta:", chamamos o método novo
        if (textoExtraido.matches("(?s).*(N[ºo]\\s*Proposta:).*")) {
            dto = parseTextoNovo(textoExtraido);
        } else {
            dto = parseTextoAntigo(textoExtraido);
        }

        // 4) Exclui arquivo temporário
        tempFile.delete();

        return dto;
    }

    private String extrairTextoDoPDF(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String texto = stripper.getText(document);

            System.out.println("======= TEXTO EXTRAÍDO =======");
            System.out.println(texto);
            System.out.println("================================");

            return texto;
        }
    }

    // ================================================================
    // A) Método Antigo
    // ================================================================
    private ImportacaoDTO parseTextoAntigo(String texto) {
        // Normaliza
        texto = Normalizer.normalize(texto, Normalizer.Form.NFKC)
                .replaceAll("\\u00A0", " ")
                .replaceAll("\\r", "")
                .trim();

        ImportacaoDTO dto = new ImportacaoDTO();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Proposta
        Matcher matcherProposta = Pattern.compile("Proposta:\\s*(\\d+)").matcher(texto);
        if (matcherProposta.find()) {
            dto.setNrProposta(matcherProposta.group(1));
        }

        // Apólice
        Matcher matcherApolice = Pattern.compile("Apólice:\\s+([\\d\\.]+)", Pattern.MULTILINE | Pattern.DOTALL)
                .matcher(texto);
        if (matcherApolice.find()) {
            String apoliceStr = matcherApolice.group(1).replace(",", "").trim();
            dto.setNrApolice(apoliceStr);
        }

        // Segurado (Nome e CPF)
        Matcher matcherSegurado = Pattern.compile("Segurado:\\s*([^\\n]+)").matcher(texto);
        if (matcherSegurado.find()) {
            String seguradoRaw = matcherSegurado.group(1).trim();
            Pattern patternNameCpf = Pattern.compile("(.*?)\\s*CPF:\\s*([\\d\\.\\-]+)");
            Matcher m = patternNameCpf.matcher(seguradoRaw);
            if (m.find()) {
                dto.setClienteNome(m.group(1).trim());
                dto.setClienteCpf(m.group(2).trim());
            } else {
                dto.setClienteNome(seguradoRaw);
            }
        }

        // Vigência
        Matcher matcherVigencia = Pattern.compile(
            "Vigência do Seguro:\\s*Das\\s*\\d{1,2}:\\d{2}h\\s*de\\s*(\\d{2}/\\d{2}/\\d{4})\\s*até\\s*às\\s*\\d{1,2}:\\d{2}h\\s*de\\s*(\\d{2}/\\d{2}/\\d{4})",
            Pattern.MULTILINE | Pattern.DOTALL
        ).matcher(texto);
        if (matcherVigencia.find()) {
            dto.setDataVigenciaInicio(LocalDate.parse(matcherVigencia.group(1), formatter));
            dto.setDataVigenciaFim(LocalDate.parse(matcherVigencia.group(2), formatter));
        }

        // Prêmio
        Matcher matcherPremio = Pattern.compile(
            "Valor Total do Seguro\\s+([\\d\\.,]+)",
            Pattern.MULTILINE | Pattern.DOTALL
        ).matcher(texto);
        if (matcherPremio.find()) {
            String premioStr = matcherPremio.group(1).replace(".", "").replace(",", ".");
            try {
                dto.setPremio(new BigDecimal(premioStr));
            } catch (NumberFormatException e) {
                // se falhar, deixa null
            }
        }

        // Placa
        Matcher matcherPlaca = Pattern.compile("Placa:\\s*(\\S+)", Pattern.MULTILINE).matcher(texto);
        if (matcherPlaca.find()) {
            dto.setPlaca(matcherPlaca.group(1).trim());
        }

        // Emissão
        Matcher matcherEmissao = Pattern.compile("Emissão:\\s*(\\d{2}/\\d{2}/\\d{4})").matcher(texto);
        if (matcherEmissao.find()) {
            dto.setDataEmissao(LocalDate.parse(matcherEmissao.group(1), formatter));
        }

        // Mostra as linhas
        String[] lines = texto.split("\\n");
        System.out.println("======= LINHAS EXTRAÍDAS (ANTIGO) =======");
        for (int i = 0; i < lines.length; i++) {
            System.out.println("Linha " + i + ": [" + lines[i] + "]");
        }
        System.out.println("================================");

        // Parser atualizado para as parcelas
        parseParcelasLineByLineAntigo(lines, dto);

        return dto;
    }

    /**
     * Agora este método não depende mais de encontrar "Parcela 1" ou "Parcela 2".
     * Em vez disso, busca pares de (valor, data) nas linhas. 
     * Exemplo de blocos:
     *  - 652,08
     *  - 27/12/2021
     *  - 2
     *  - ...
     *  - 580,89
     *  - 21/02/2022
     */
    private void parseParcelasLineByLineAntigo(String[] lines, ImportacaoDTO dto) {
        // Regex para reconhecer valor no formato "305,21" ou "1.305,21", etc.
        Pattern pValor = Pattern.compile("^\\d{1,3}(?:\\.\\d{3})*,\\d{2}$");
        // Regex para reconhecer data "dd/MM/yyyy"
        Pattern pData = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");

        int parcelaCount = 0;

        for (int i = 0; i < lines.length - 1; i++) {
            String lineValor = lines[i].trim();
            String lineData = lines[i + 1].trim();

            // Se a linha atual for um valor e a próxima for uma data, 
            // consideramos que achamos uma nova parcela
            if (pValor.matcher(lineValor).matches() && pData.matcher(lineData).matches()) {
                parcelaCount++;

                BigDecimal valor = parseBigDecimal(lineValor);
                LocalDate data = parseLocalDate(lineData);

                if (parcelaCount == 1) {
                    dto.setValorPrimeiraParcela(valor);
                    dto.setDataPrimeiraParcela(data);
                } else if (parcelaCount == 2) {
                    dto.setValorSegundaParcela(valor);
                    dto.setDataSegundaParcela(data);
                }
                // Pula 1 linha extra, pois já processamos lineData
                i++;
            }
        }

        // Garante ao menos 2 parcelas, se não encontradas
        if (parcelaCount < 2) {
            parcelaCount = 2;
        }
        dto.setNumeroParcelas(parcelaCount);
    }

    // ================================================================
    // B) Método Novo (para "No Proposta:")
    // ================================================================
    private ImportacaoDTO parseTextoNovo(String texto) {
        // Normaliza
        texto = Normalizer.normalize(texto, Normalizer.Form.NFKC)
                .replaceAll("\\u00A0", " ")
                .replaceAll("\\r", "")
                .trim();

        ImportacaoDTO dto = new ImportacaoDTO();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Ex.: "No Proposta: 44372159"
        Matcher matcherProposta = Pattern.compile("N[ºo]\\s*Proposta:\\s*(\\d+)", Pattern.CASE_INSENSITIVE)
                .matcher(texto);
        if (matcherProposta.find()) {
            dto.setNrProposta(matcherProposta.group(1));
        }

        // Segurado
        Matcher segMatcher = Pattern.compile(
            "Segurado:\\s*(.*?)\\s*CPF:\\s*([\\d\\.\\-]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        ).matcher(texto);
        if (segMatcher.find()) {
            dto.setClienteNome(segMatcher.group(1).trim());
            dto.setClienteCpf(segMatcher.group(2).trim());
        }

        // Placa
        Matcher matcherPlaca = Pattern.compile("Placa:\\s*(\\S+)", Pattern.CASE_INSENSITIVE).matcher(texto);
        if (matcherPlaca.find()) {
            dto.setPlaca(matcherPlaca.group(1).trim());
        }

        // Emissão: 28/12/2021
        Matcher matcherEmissao = Pattern.compile("Emissão:\\s*(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE)
                .matcher(texto);
        if (matcherEmissao.find()) {
            dto.setDataEmissao(LocalDate.parse(matcherEmissao.group(1), formatter));
        }

        // Vigência do Seguro
        Matcher matcherVigencia = Pattern.compile(
            "Vigência do Seguro:\\s*A partir das 24 horas de\\s*(\\d{2}/\\d{2}/\\d{4})\\s*às\\s*24 horas de\\s*(\\d{2}/\\d{2}/\\d{4})",
            Pattern.CASE_INSENSITIVE
        ).matcher(texto);
        if (matcherVigencia.find()) {
            dto.setDataVigenciaInicio(LocalDate.parse(matcherVigencia.group(1), formatter));
            dto.setDataVigenciaFim(LocalDate.parse(matcherVigencia.group(2), formatter));
        }

        // Valor Total do Seguro
        Matcher matcherPremio = Pattern.compile("Valor Total do Seguro[^\\d]+([\\d\\.,]+)", Pattern.CASE_INSENSITIVE)
                .matcher(texto);
        if (matcherPremio.find()) {
            String premioStr = matcherPremio.group(1).replace(".", "").replace(",", ".");
            try {
                dto.setPremio(new BigDecimal(premioStr));
            } catch (NumberFormatException e) {
                // se falhar, deixa null
            }
        }

        // Exibe linhas
        String[] lines = texto.split("\\n");
        System.out.println("======= LINHAS EXTRAÍDAS (NOVO) =======");
        for (int i = 0; i < lines.length; i++) {
            System.out.println("Linha " + i + ": [" + lines[i] + "]");
        }
        System.out.println("================================");

        parseParcelasLineByLineNovo(lines, dto);

        return dto;
    }

    private void parseParcelasLineByLineNovo(String[] lines, ImportacaoDTO dto) {
        // Observando seu log, as parcelas aparecem:
        // Linha 84: [No de Parcelas Primeira Parcela  Demais  Parcelas]
        // Linha 85: [10 588,00  587,99]
        // Linha 87: [Dia  de Vencimento  27/12/2021  20]

        int maxParcela = 0;

        // Acha a linha de cabeçalho "No de Parcelas"
        int indexCabecalho = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].toLowerCase().contains("no de parcelas")
                    && lines[i].toLowerCase().contains("primeira parcela")) {
                indexCabecalho = i;
                break;
            }
        }
        if (indexCabecalho != -1 && indexCabecalho + 1 < lines.length) {
            // linha de valores ex.: "10 588,00  587,99"
            String linhaVal = lines[indexCabecalho + 1].trim().replaceAll("\\s+", " ");
            String[] parts = linhaVal.split(" ");
            if (parts.length >= 3) {
                try {
                    maxParcela = Integer.parseInt(parts[0]); // "10"
                } catch (NumberFormatException e) {
                    // se falhar, fica 0
                }
                dto.setNumeroParcelas(maxParcela);

                dto.setValorPrimeiraParcela(parseBigDecimal(parts[1]));
                dto.setValorSegundaParcela(parseBigDecimal(parts[2]));
            }
        }

        // Pegar datas. Ex.: Linha 87 => "Dia de Vencimento 27/12/2021 20"
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim().replaceAll("\\s+", " ");
            if (line.toLowerCase().contains("dia de vencimento")) {
                // Ex.: "Dia de Vencimento 27/12/2021 20"
                String[] parts = line.split(" ");
                // ["Dia","de","Vencimento","27/12/2021","20"]
                if (parts.length >= 4) {
                    dto.setDataPrimeiraParcela(parseLocalDate(parts[3]));
                    // "20" não é data dd/MM/yyyy, então ignoramos
                }
            }
        }

        if (maxParcela < 2) {
            dto.setNumeroParcelas(2);
        }
    }

    // ================================================================
    // Métodos Auxiliares
    // ================================================================
    private BigDecimal parseBigDecimal(String str) {
        if (str == null || str.isEmpty()) return null;
        // Remove pontos e troca vírgula por ponto: "1.305,21" -> "1305.21"
        str = str.replace(".", "").replace(",", ".");
        try {
            return new BigDecimal(str);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseLocalDate(String str) {
        if (str == null || str.isEmpty()) return null;
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(str, fmt);
        } catch (Exception e) {
            return null;
        }
    }

}
