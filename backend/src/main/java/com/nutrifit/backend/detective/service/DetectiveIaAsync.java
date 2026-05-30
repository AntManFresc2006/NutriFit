package com.nutrifit.backend.detective.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.detective.repository.DetectiveRepository;
import com.nutrifit.backend.ia.dto.UsuarioIaConfigResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Component
public class DetectiveIaAsync {

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final DetectiveRepository repository;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DetectiveIaAsync(DetectiveRepository repository) {
        this.repository = repository;
    }

    @Async("planGeneratorExecutor")
    public void generar(Long analisisId, String prompt, UsuarioIaConfigResponse userConfig,
                        String modelPrimary, String keyPrimary,
                        String modelFallback, String keyFallback) {
        try {
            String resultado;
            if (userConfig != null) {
                String url = resolveUrl(userConfig.getProxyUrl());
                resultado = callProxy(prompt, url, userConfig.getModel(), userConfig.getApiKey());
            } else {
                try {
                    resultado = callProxy(prompt, OPENROUTER_URL, modelPrimary, keyPrimary);
                } catch (IOException e) {
                    resultado = callProxy(prompt, OPENROUTER_URL, modelFallback, keyFallback);
                }
            }
            repository.updateIa(analisisId, resultado);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            repository.updateError(analisisId, "La generación fue interrumpida.");
        } catch (Exception e) {
            String msg = e instanceof IOException
                    ? "Error de conexión con la IA: " + e.getMessage()
                    : "Error inesperado: " + e.getMessage();
            repository.updateError(analisisId, msg);
        }
    }

    private String callProxy(String userPrompt, String url, String model, String apiKey)
            throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", userPrompt)),
                "max_tokens", 1000
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(90))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("IA error " + response.statusCode() + ": " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.path("choices").get(0).path("message").path("content").asText();
    }

    private String resolveUrl(String proxyUrl) {
        return proxyUrl.endsWith("/chat/completions")
                ? proxyUrl
                : proxyUrl.replaceAll("/+$", "") + "/chat/completions";
    }
}
