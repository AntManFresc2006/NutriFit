package com.nutrifit.backend.evaluacion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nutrifit.backend.perfil.dto.PerfilResponse;
import com.nutrifit.backend.perfil.service.PerfilService;
import com.nutrifit.backend.resumen.dto.ResumenDiarioResponse;
import com.nutrifit.backend.resumen.service.ResumenDiarioService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

/**
 * Llama a OpenRouter para evaluar el día nutricional del usuario.
 *
 * <p>Recoge el resumen del día y el perfil del usuario, construye un prompt con todos
 * los datos relevantes y devuelve el texto de evaluación generado por el modelo.</p>
 *
 * <p>La API key se lee de la variable de entorno {@code OPENROUTER_API_KEY}.
 * Si no está configurada, se devuelve un mensaje de error en lugar de lanzar
 * una excepción, para que el cliente pueda mostrarlo directamente en pantalla.</p>
 */
@Service
public class EvaluacionIaServiceImpl implements EvaluacionIaService {

    private static final String CLAUDE_API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "openrouter/free";
    private static final int MAX_TOKENS = 400;

    private final ResumenDiarioService resumenDiarioService;
    private final PerfilService perfilService;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EvaluacionIaServiceImpl(ResumenDiarioService resumenDiarioService,
                                   PerfilService perfilService) {
        this.resumenDiarioService = resumenDiarioService;
        this.perfilService = perfilService;
    }

    @Override
    public String evaluarDia(Long usuarioId, LocalDate fecha) {
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return "OPENROUTER_API_KEY no configurada";
        }

        ResumenDiarioResponse resumen = resumenDiarioService.obtenerResumenDiario(usuarioId, fecha);
        PerfilResponse perfil = perfilService.getPerfil(usuarioId);

        String prompt = construirPrompt(resumen, perfil, fecha);
        String requestBody = construirRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CLAUDE_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Error en la API de OpenRouter. Código HTTP: " + response.statusCode());
            }
            return extraerTextoRespuesta(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error al comunicarse con la API de OpenRouter: " + e.getMessage(), e);
        }
    }

    private String construirPrompt(ResumenDiarioResponse resumen, PerfilResponse perfil, LocalDate fecha) {
        String objetivo = determinarObjetivo(perfil.getPesoKgActual(), perfil.getPesoObjetivo());

        return String.format("""
                Eres un asistente nutricional. Evalúa de forma breve y práctica el día %s del usuario.

                Datos del día:
                - TDEE (gasto energético diario estimado): %.0f kcal
                - Kcal ingeridas: %.0f kcal
                - Proteínas: %.1f g
                - Grasas: %.1f g
                - Carbohidratos: %.1f g
                - Kcal quemadas por ejercicio: %.0f kcal
                - Balance neto (ingeridas − quemadas): %.0f kcal

                Objetivo del usuario: %s (peso actual %.1f kg, peso objetivo %s).

                Proporciona una evaluación en español de máximo 150 palabras: indica si el día ha sido bueno \
                respecto al objetivo, señala los puntos más destacables y da 1-2 recomendaciones concretas.
                """,
                fecha,
                perfil.getTdee(),
                resumen.getKcalTotales(),
                resumen.getProteinasTotales(),
                resumen.getGrasasTotales(),
                resumen.getCarbosTotales(),
                resumen.getKcalQuemadasTotales(),
                resumen.getBalanceNeto(),
                objetivo,
                perfil.getPesoKgActual(),
                perfil.getPesoObjetivo() != null ? String.format("%.1f kg", perfil.getPesoObjetivo()) : "no definido"
        );
    }

    private String determinarObjetivo(double pesoActual, Double pesoObjetivo) {
        if (pesoObjetivo == null) return "mantener peso";
        if (pesoActual > pesoObjetivo) return "perder peso";
        if (pesoActual < pesoObjetivo) return "ganar peso";
        return "mantener peso";
    }

    private String construirRequestBody(String prompt) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", MODEL);
            body.put("max_tokens", MAX_TOKENS);

            ArrayNode messages = body.putArray("messages");
            ObjectNode message = messages.addObject();
            message.put("role", "user");
            message.put("content", prompt);

            return objectMapper.writeValueAsString(body);
        } catch (IOException e) {
            throw new RuntimeException("Error al construir el cuerpo de la petición a Claude", e);
        }
    }

    private String extraerTextoRespuesta(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
            throw new RuntimeException("Respuesta de OpenRouter sin contenido de texto");
        } catch (IOException e) {
            throw new RuntimeException("Error al parsear la respuesta de OpenRouter: " + e.getMessage(), e);
        }
    }
}
