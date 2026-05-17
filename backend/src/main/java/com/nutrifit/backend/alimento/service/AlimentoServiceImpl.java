package com.nutrifit.backend.alimento.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.alimento.dto.AlimentoRequest;
import com.nutrifit.backend.alimento.dto.AlimentoResponse;
import com.nutrifit.backend.alimento.dto.EscanearFotoResponse;
import com.nutrifit.backend.alimento.model.Alimento;
import com.nutrifit.backend.alimento.repository.AlimentoRepository;
import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Implementación de la capa de servicio del módulo de alimentos.
 * Aquí se centraliza la lógica de negocio del CRUD, incluyendo:
 * - búsqueda general o filtrada
 * - comprobación de existencia por id
 * - conversión entre DTOs y modelo de dominio
 */
@Service
public class AlimentoServiceImpl implements AlimentoService {

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "google/gemma-3-27b-it:free";

    @Value("${openrouter.gemma.api.key}")
    private String gemmaApiKey;

    private final AlimentoRepository alimentoRepository;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Inyección del repositorio encargado del acceso a datos.
     */
    public AlimentoServiceImpl(AlimentoRepository alimentoRepository) {
        this.alimentoRepository = alimentoRepository;
    }

    /**
     * Devuelve todos los alimentos o filtra por nombre si se recibe una búsqueda.
     *
     * @param query texto opcional para filtrar alimentos por nombre
     * @return lista de alimentos en formato DTO de respuesta
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlimentoResponse> findAll(String query) {
        List<Alimento> alimentos;

        if (query == null || query.isBlank()) {
            alimentos = alimentoRepository.findAll();
        } else {
            alimentos = alimentoRepository.searchByNombre(query.trim());
        }

        return alimentos.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Obtiene un alimento a partir de su id.
     * Si no existe, lanza una excepción controlada que después se transforma en un 404.
     *
     * @param id identificador del alimento
     * @return alimento encontrado en formato DTO de respuesta
     */
    @Override
    @Transactional(readOnly = true)
    public AlimentoResponse findById(Long id) {
        Alimento alimento = alimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un alimento con id " + id));

        return toResponse(alimento);
    }

    /**
     * Crea un nuevo alimento a partir de los datos recibidos desde la API.
     *
     * @param request datos del alimento enviados por el cliente
     * @return alimento creado en formato DTO de respuesta
     */
    @Override
    @Transactional
    public AlimentoResponse save(AlimentoRequest request) {
        Alimento alimento = toModel(request);
        Alimento guardado = alimentoRepository.save(alimento);
        return toResponse(guardado);
    }

    /**
     * Actualiza un alimento existente.
     * Antes de actualizar, se verifica que el id exista en base de datos.
     *
     * @param id identificador del alimento a actualizar
     * @param request nuevos datos del alimento
     * @return alimento actualizado en formato DTO de respuesta
     */
    @Override
    @Transactional
    public AlimentoResponse update(Long id, AlimentoRequest request) {
        alimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un alimento con id " + id));

        Alimento alimento = toModel(request);
        Alimento actualizado = alimentoRepository.update(id, alimento);
        return toResponse(actualizado);
    }

    /**
     * Elimina un alimento existente por su id.
     * Si no existe, se lanza una excepción controlada.
     *
     * @param id identificador del alimento a eliminar
     * @return true si la eliminación se realizó correctamente
     */
    @Override
    @Transactional
    public boolean deleteById(Long id) {
        alimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un alimento con id " + id));

        return alimentoRepository.deleteById(id);
    }

    /**
     * Convierte el DTO de entrada en el modelo interno de dominio.
     *
     * @param request datos recibidos desde el cliente
     * @return objeto Alimento listo para persistir
     */
    private Alimento toModel(AlimentoRequest request) {
        Alimento alimento = new Alimento();
        alimento.setNombre(request.getNombre().trim());
        alimento.setPorcionG(request.getPorcionG());
        alimento.setKcalPor100g(request.getKcalPor100g());
        alimento.setProteinasG(request.getProteinasG());
        alimento.setGrasasG(request.getGrasasG());
        alimento.setCarbosG(request.getCarbosG());
        alimento.setFuente(request.getFuente());
        return alimento;
    }

    /**
     * Convierte el modelo interno de dominio en un DTO de respuesta.
     *
     * @param alimento entidad obtenida o persistida en base de datos
     * @return DTO preparado para enviarse al cliente
     */
    private AlimentoResponse toResponse(Alimento alimento) {
        return new AlimentoResponse(
                alimento.getId(),
                alimento.getNombre(),
                alimento.getPorcionG(),
                alimento.getKcalPor100g(),
                alimento.getProteinasG(),
                alimento.getGrasasG(),
                alimento.getCarbosG(),
                alimento.getFuente()
        );
    }

    @Override
    public EscanearFotoResponse escanearFoto(String imagenBase64, String mimeType) throws Exception {
        String prompt = """
                Analiza esta imagen de un producto alimentario o su código de barras.
                Identifica el producto y estima sus valores nutricionales por 100g.
                Si es una imagen de código de barras, intenta reconocer el producto.
                Responde SOLO con JSON válido (sin markdown, sin explicaciones):
                {"nombre":"...","kcalPor100g":0,"proteinas":0,"grasas":0,"carbos":0,"porcion":100}
                """;

        String responseJson = callOpenRouterWithVision(imagenBase64, mimeType, prompt);
        return parseNutritionResponse(responseJson);
    }

    private String callOpenRouterWithVision(String imagenBase64, String mimeType, String prompt) throws IOException, InterruptedException {
        Map<String, Object> imageContent = Map.of(
                "type", "image_url",
                "image_url", Map.of("url", "data:" + mimeType + ";base64," + imagenBase64)
        );

        Map<String, Object> textContent = Map.of(
                "type", "text",
                "text", prompt
        );

        String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", MODEL,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(imageContent, textContent)
                )),
                "max_tokens", 200
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENROUTER_URL))
                .header("Authorization", "Bearer " + gemmaApiKey)
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenRouter error " + response.statusCode() + ": " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        String content = json.path("choices").get(0).path("message").path("content").asText();
        return limpiarJson(content);
    }

    private EscanearFotoResponse parseNutritionResponse(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return new EscanearFotoResponse(
                    node.path("nombre").asText("Producto desconocido"),
                    node.path("kcalPor100g").asDouble(0.0),
                    node.path("proteinas").asDouble(0.0),
                    node.path("grasas").asDouble(0.0),
                    node.path("carbos").asDouble(0.0),
                    node.path("porcion").asDouble(100.0)
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar respuesta de IA: " + e.getMessage(), e);
        }
    }

    private String limpiarJson(String raw) {
        if (raw == null) return raw;
        String s = raw.strip();
        if (s.startsWith("```")) {
            int first = s.indexOf('\n');
            int last = s.lastIndexOf("```");
            if (first != -1 && last > first) {
                s = s.substring(first + 1, last).strip();
            }
        }
        return s;
    }
}