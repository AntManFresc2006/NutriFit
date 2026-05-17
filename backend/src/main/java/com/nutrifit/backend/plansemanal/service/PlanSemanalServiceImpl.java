package com.nutrifit.backend.plansemanal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.service.PerfilService;
import com.nutrifit.backend.plansemanal.dto.PlanSemanalResponse;
import com.nutrifit.backend.plansemanal.repository.PlanSemanalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class PlanSemanalServiceImpl implements PlanSemanalService {

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL_PRIMARY = "google/gemma-4-31b-it:free";
    private static final String MODEL_FALLBACK = "deepseek/deepseek-v4-flash:free";

    @Value("${openrouter.gemma.api.key}")
    private String gemmaApiKey;

    @Value("${openrouter.deepseek.api.key}")
    private String deepseekApiKey;

    private final PlanSemanalRepository repository;
    private final PerfilService perfilService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PlanSemanalServiceImpl(PlanSemanalRepository repository, PerfilService perfilService) {
        this.repository = repository;
        this.perfilService = perfilService;
    }

    @Override
    @Transactional
    public PlanSemanalResponse generarORecuperarPlan(Long usuarioId, LocalDate semanaInicio) throws RuntimeException {
        var existente = repository.findByUsuarioAndSemana(usuarioId, semanaInicio);
        if (existente.isPresent()) {
            return existente.get();
        }

        try {
            PerfilResponse perfil = perfilService.getPerfil(usuarioId);
            String prompt = buildPrompt(semanaInicio, perfil);
            String planJson = generarPlanConIA(prompt);
            return repository.save(usuarioId, semanaInicio, planJson);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error al generar plan con IA (interrumpido): " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar plan con IA: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PlanSemanalResponse getPlan(Long usuarioId, LocalDate semanaInicio) {
        return repository.findByUsuarioAndSemana(usuarioId, semanaInicio)
                .orElse(null);
    }

    @Override
    @Transactional
    public void eliminarPlan(Long usuarioId, LocalDate semanaInicio) {
        repository.deleteByUsuarioAndSemana(usuarioId, semanaInicio);
    }

    private String buildPrompt(LocalDate semanaInicio, PerfilResponse perfil) {
        double proteinasDiarias = perfil.getPesoKgActual() * 0.8;

        StringBuilder sb = new StringBuilder();
        sb.append("Eres un nutricionista experto. Genera un plan de alimentación semanal para 7 días (lunes a domingo) comenzando el ").append(semanaInicio).append(".\n\n");

        sb.append("Datos del usuario:\n");
        sb.append("- TDEE: ").append(Math.round(perfil.getTdee())).append(" kcal/día\n");
        sb.append("- Objetivo calórico diario: ").append(Math.round(perfil.getTdee())).append(" kcal\n");
        sb.append("- Proteínas objetivo: ").append(String.format("%.1f", proteinasDiarias)).append("g/día (aprox)\n\n");

        sb.append("Genera el plan en formato JSON estrictamente así (sin markdown, sin explicaciones, solo el JSON):\n");
        sb.append("{\n");
        sb.append("  \"dias\": [\n");
        sb.append("    {\n");
        sb.append("      \"dia\": \"Lunes\",\n");
        sb.append("      \"fecha\": \"YYYY-MM-DD\",\n");
        sb.append("      \"comidas\": {\n");
        sb.append("        \"desayuno\": { \"descripcion\": \"...\", \"kcal\": 400, \"proteinas\": 20, \"carbos\": 45, \"grasas\": 15 },\n");
        sb.append("        \"almuerzo\": { \"descripcion\": \"...\", \"kcal\": 600, \"proteinas\": 35, \"carbos\": 60, \"grasas\": 20 },\n");
        sb.append("        \"merienda\": { \"descripcion\": \"...\", \"kcal\": 200, \"proteinas\": 10, \"carbos\": 25, \"grasas\": 5 },\n");
        sb.append("        \"cena\": { \"descripcion\": \"...\", \"kcal\": 500, \"proteinas\": 30, \"carbos\": 45, \"grasas\": 18 }\n");
        sb.append("      },\n");
        sb.append("      \"totalKcal\": 1700,\n");
        sb.append("      \"totalProteinas\": 95,\n");
        sb.append("      \"totalCarbos\": 175,\n");
        sb.append("      \"totalGrasas\": 58\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n\n");

        sb.append("Varía los alimentos cada día. Usa alimentos mediterráneos típicos. Las fechas de cada día deben ser correlativos comenzando en ").append(semanaInicio).append(".");

        return sb.toString();
    }

    private String generarPlanConIA(String prompt) throws IOException, InterruptedException {
        try {
            return callOpenRouter(prompt, MODEL_PRIMARY, gemmaApiKey);
        } catch (IOException primary) {
            return callOpenRouter(prompt, MODEL_FALLBACK, deepseekApiKey);
        }
    }

    private String callOpenRouter(String userPrompt, String model, String key) throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", userPrompt)),
                "max_tokens", 2000
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENROUTER_URL))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(60))
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
