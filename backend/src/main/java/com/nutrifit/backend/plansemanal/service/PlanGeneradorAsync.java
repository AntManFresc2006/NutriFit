package com.nutrifit.backend.plansemanal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.ia.dto.UsuarioIaConfigResponse;
import com.nutrifit.backend.plansemanal.repository.PlanSemanalRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Componente separado para ejecutar la generación del plan en segundo plano.
 * Debe estar en un bean distinto de quien lo llama para que @Async funcione vía proxy.
 */
@Component
public class PlanGeneradorAsync {

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final PlanSemanalRepository repository;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PlanGeneradorAsync(PlanSemanalRepository repository) {
        this.repository = repository;
    }

    @Async("planGeneratorExecutor")
    public void generar(Long planId, String prompt, UsuarioIaConfigResponse userConfig,
                        String modelPrimary, String keyPrimary,
                        String modelFallback, String keyFallback) {
        try {
            String planJson;
            if (userConfig != null) {
                String url = resolveUrl(userConfig.getProxyUrl());
                planJson = callProxy(prompt, url, userConfig.getModel(), userConfig.getApiKey());
            } else {
                try {
                    planJson = callProxy(prompt, OPENROUTER_URL, modelPrimary, keyPrimary);
                } catch (IOException e) {
                    planJson = callProxy(prompt, OPENROUTER_URL, modelFallback, keyFallback);
                }
            }
            repository.updateFinalizado(planId, planJson, "LISTO", null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            repository.updateFinalizado(planId, null, "ERROR", "La generación fue interrumpida.");
        } catch (Exception e) {
            String msg = e instanceof IOException
                    ? "Error de conexión con la IA: " + e.getMessage()
                    : "Error inesperado: " + e.getMessage();
            repository.updateFinalizado(planId, null, "ERROR", msg);
        }
    }

    private String callProxy(String userPrompt, String url, String model, String apiKey)
            throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", userPrompt)),
                "max_tokens", 4000
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
        String content = json.path("choices").get(0).path("message").path("content").asText();
        return limpiarJson(content);
    }

    private String resolveUrl(String proxyUrl) {
        return proxyUrl.endsWith("/chat/completions")
                ? proxyUrl
                : proxyUrl.replaceAll("/+$", "") + "/chat/completions";
    }

    private String limpiarJson(String raw) {
        if (raw == null) return raw;
        String s = raw.strip();
        if (s.startsWith("```")) {
            int first = s.indexOf('\n');
            int last  = s.lastIndexOf("```");
            if (first != -1 && last > first) {
                s = s.substring(first + 1, last).strip();
            }
        }
        return s;
    }
}
