package com.nutrifit.backend.resumen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.service.PerfilService;
import com.nutrifit.backend.resumen.dto.EvaluacionIaRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class EvaluacionIaService {

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL_PRIMARY  = "google/gemma-4-31b-it:free";
    private static final String MODEL_FALLBACK = "deepseek/deepseek-v4-flash:free";

    @Value("${openrouter.gemma.api.key}")
    private String gemmaApiKey;

    @Value("${openrouter.deepseek.api.key}")
    private String deepseekApiKey;

    private final PerfilService perfilService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EvaluacionIaService(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    public String evaluar(EvaluacionIaRequest req) throws IOException, InterruptedException {
        PerfilResponse perfil = null;
        try {
            perfil = perfilService.getPerfil(req.getUsuarioId());
        } catch (Exception e) {
            // usuario sin perfil: se omiten datos biométricos del prompt
        }
        String prompt = buildPrompt(req, perfil);
        try {
            return callOpenRouter(prompt, MODEL_PRIMARY, gemmaApiKey);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw ie;
        } catch (IOException primary) {
            return callOpenRouter(prompt, MODEL_FALLBACK, deepseekApiKey);
        }
    }

    private String buildPrompt(EvaluacionIaRequest req, PerfilResponse perfil) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un nutricionista y entrenador personal experto. ");
        sb.append("Analiza los datos nutricionales del usuario para el día ").append(req.getFecha()).append(".\n\n");

        if (perfil != null) {
            sb.append("DATOS DEL USUARIO:\n");
            sb.append("- Peso actual: ").append(perfil.getPesoKgActual()).append(" kg\n");
            sb.append("- Altura: ").append(perfil.getAlturaCm()).append(" cm\n");
            sb.append("- Nivel de actividad: ").append(perfil.getNivelActividad()).append("\n");
            if (perfil.getPesoObjetivo() != null) {
                sb.append("- Objetivo de peso: ").append(perfil.getPesoObjetivo()).append(" kg\n");
            }
            sb.append("- TDEE (calorías de mantenimiento): ").append(Math.round(req.getTdee())).append(" kcal\n\n");
        }

        String estado = estadoDesdeBalance(req.getBalanceReal());

        sb.append("DATOS DEL DÍA:\n");
        sb.append("- Calorías consumidas: ").append(Math.round(req.getKcalConsumidas())).append(" kcal\n");
        sb.append("- Calorías quemadas en ejercicio: ").append(Math.round(req.getKcalQuemadas())).append(" kcal\n");
        sb.append("- Proteínas: ").append(String.format("%.1f", req.getProteinasTotales())).append(" g\n");
        sb.append("- Grasas: ").append(String.format("%.1f", req.getGrasasTotales())).append(" g\n");
        sb.append("- Carbohidratos: ").append(String.format("%.1f", req.getCarbosTotales())).append(" g\n");
        sb.append("- Balance real vs mantenimiento: ").append(Math.round(req.getBalanceReal()))
          .append(" kcal (").append(estado).append(")\n\n");

        sb.append("Proporciona una evaluación en español de 3-4 párrafos breves que incluya:\n");
        sb.append("1. Balance del día: valora el déficit/superávit y si es adecuado al objetivo.\n");
        sb.append("2. Análisis de macros: evalúa si las proporciones de proteínas, grasas y carbohidratos son correctas.\n");
        sb.append("3. Recomendación concreta para el día siguiente o para mejorar.\n");
        sb.append("Sé directo, motivador y concreto. No uses asteriscos ni formato markdown.");

        return sb.toString();
    }

    private static String estadoDesdeBalance(double balanceReal) {
        if (balanceReal > 100) return "SUPERÁVIT";
        if (balanceReal < -100) return "DÉFICIT";
        return "MANTENIMIENTO";
    }

    private String callOpenRouter(String userPrompt, String model, String key) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", userPrompt)),
                "max_tokens", 600
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENROUTER_URL))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenRouter error " + response.statusCode() + ": " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        return json.path("choices").get(0).path("message").path("content").asText();
    }
}
