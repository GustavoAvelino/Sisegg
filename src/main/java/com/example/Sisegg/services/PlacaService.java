package com.example.Sisegg.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class PlacaService {

    private static final String API_URL = "https://wdapi2.com.br/consulta/{placa}/{token}";
    private static final String TOKEN = "8a91b56f203359dba178d1920cd5425f"; // Seu token de acesso

    public Map<String, Object> consultarPlaca(String placa) {
        RestTemplate restTemplate = new RestTemplate();
        // Monta a URL com placa e token
        String url = API_URL.replace("{placa}", placa).replace("{token}", TOKEN);

        // Faz a requisição
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);

        // Verifica se deu 2xx
        if (response.getStatusCode().is2xxSuccessful()) {
            Object body = response.getBody();
            if (body == null) {
                throw new RuntimeException("Resposta da API veio vazia (null).");
            }

            // Trata a resposta recursivamente
            return extrairMap(body);
        }

        // Se não for 2xx
        throw new RuntimeException("Erro ao consultar a API de placas. Status: " + response.getStatusCode());
    }

    /**
     * Função recursiva que:
     * 1) Se for Map, retorna o Map.
     * 2) Se for List, pega o primeiro item e chama extrairMap de novo.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extrairMap(Object body) {
        if (body instanceof Map) {
            return (Map<String, Object>) body;
        } else if (body instanceof List) {
            List<?> lista = (List<?>) body;
            if (lista.isEmpty()) {
                throw new RuntimeException("Lista vazia. Não há dados para processar.");
            }
            // Pega o primeiro item e tenta processar novamente
            return extrairMap(lista.get(0));
        } else {
            // Caso não seja nem Map nem List (ex: String, Number, Boolean, etc.)
            throw new RuntimeException("Formato de resposta inesperado: " + body.getClass());
        }
    }
}
