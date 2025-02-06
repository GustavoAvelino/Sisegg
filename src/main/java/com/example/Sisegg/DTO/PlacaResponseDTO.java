package com.example.Sisegg.DTO;

import java.util.List;
import java.util.Map;

public record PlacaResponseDTO(
    String chassiMascarado,
    String chassiCompleto,
    String marca,
    String modelo,
    String submodelo,
    String versao,
    Integer ano,
    Integer anoModelo,
    String cor,
    String combustivel,
    String municipio,
    String uf,
    String placa,
    String codigoFipe,
    Double valorFipe,
    Integer passageiros
) {
    public PlacaResponseDTO(Map<String, Object> dados) {
        this(
            // 1) Chassi censurado que vem no campo principal
            (String) dados.get("chassi"),

            // 2) Chassi completo em extra.chassi
            (String) unrollToMap(dados.get("extra")).get("chassi"),

            // 3) Campos básicos no nível superior
            (String) dados.get("marca"),
            (String) dados.get("modelo"),
            (String) dados.get("submodelo"),
            (String) dados.get("versao"),
            toInteger(dados.get("ano")),
            toInteger(dados.get("anoModelo")),
            (String) dados.get("cor"),

            // 4) Combustível em extra.combustivel
            (String) unrollToMap(dados.get("extra")).get("combustivel"),

            (String) dados.get("municipio"),
            (String) dados.get("uf"),
            (String) dados.get("placa"),

            // 5) FIPE -> dados -> codigo_fipe e texto_valor
            extractCodigoFipe(dados),
            extractValorFipe(dados),

            // 6) Passageiros (ex.: quantidade_passageiro em extra)
            toInteger(unrollToMap(dados.get("extra")).get("quantidade_passageiro"))
        );
    }

    // -------------------------------------------------
    // ----------- MÉTODOS AUXILIARES ESTÁTICOS ---------
    // -------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Map<String, Object> unrollToMap(Object obj) {
        if (obj == null) {
            // Se 'extra' ou 'fipe' não existirem, retorna Map vazio
            return Map.of();
        }
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        if (obj instanceof List) {
            List<?> lista = (List<?>) obj;
            if (lista.isEmpty()) {
                return Map.of();
            }
            // Pega o primeiro item e chama recursivo
            return unrollToMap(lista.get(0));
        }
        throw new RuntimeException(
            "Valor inesperado ao tentar converter em Map: " + obj.getClass()
        );
    }

    private static Integer toInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            return Integer.valueOf((String) obj);
        }
        throw new RuntimeException("Formato inesperado ao converter para Integer: " + obj);
    }

    private static String extractCodigoFipe(Map<String, Object> dados) {
        Map<String, Object> fipeMap = unrollToMap(dados.get("fipe"));
        Map<String, Object> fipeDadosMap = unrollToMap(fipeMap.get("dados"));
        Object codigoFipe = fipeDadosMap.get("codigo_fipe");
        return (codigoFipe == null) ? null : codigoFipe.toString();
    }

    private static Double extractValorFipe(Map<String, Object> dados) {
        Map<String, Object> fipeMap = unrollToMap(dados.get("fipe"));
        Map<String, Object> fipeDadosMap = unrollToMap(fipeMap.get("dados"));
        Object textoValorObj = fipeDadosMap.get("texto_valor");
        if (textoValorObj == null) {
            return null;
        }
        String textoValor = textoValorObj.toString();
        // Ex: "R$ 28.799,00" → remove "R$ ", "." e troca "," por "."
        textoValor = textoValor.replace("R$ ", "").replace(".", "").replace(",", ".");
        return Double.valueOf(textoValor);
    }
}
