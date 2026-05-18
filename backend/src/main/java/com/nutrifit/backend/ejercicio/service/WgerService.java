package com.nutrifit.backend.ejercicio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.ejercicio.dto.EjercicioExternoResponse;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Servicio que consume la API externa Wger para obtener un catálogo amplio de ejercicios.
 *
 * <p>Cachea los datos de la API durante 6 horas para reducir el número de peticiones.
 * Extrae el nombre en inglés y asigna un MET estimado según la categoría.</p>
 */
@Service
public class WgerService {

    private static final Logger log = LoggerFactory.getLogger(WgerService.class);

    private static final String WGER_URL =
            "https://wger.de/api/v2/exerciseinfo/?format=json&language=2&limit=845";

    private static final long CACHE_TTL_MS = 6 * 3600_000L;

    private static final Map<String, Double> MET_BY_CATEGORY = Map.of(
            "abs",       3.5,
            "arms",      4.0,
            "back",      4.0,
            "calves",    3.5,
            "cardio",    7.0,
            "chest",     4.0,
            "legs",      5.0,
            "shoulders", 4.0
    );

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    private final AtomicReference<List<EjercicioExternoResponse>> cache = new AtomicReference<>(null);
    private volatile long cacheTimestamp = 0;

    /**
     * Busca ejercicios en el catálogo Wger que contengan la consulta en su nombre.
     *
     * @param query texto a buscar
     * @return lista de hasta 10 ejercicios coincidentes
     */
    public List<EjercicioExternoResponse> buscar(String query) {
        try {
            List<EjercicioExternoResponse> all = getAll();
            String q = query.toLowerCase().trim();
            List<EjercicioExternoResponse> result = all.stream()
                    .filter(e -> e.nombre().toLowerCase().contains(q))
                    .limit(10)
                    .toList();
            log.info("[Wger] query='{}' cache={} resultados={}", q, all.size(), result.size());
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Wger] interrumpido buscando '{}'", query, e);
            return List.of();
        } catch (IOException e) {
            log.error("[Wger] error buscando '{}': {}", query, e.getMessage(), e);
            return List.of();
        }
    }

    private List<EjercicioExternoResponse> getAll() throws IOException, InterruptedException {
        List<EjercicioExternoResponse> cached = cache.get();
        if (cached != null && (System.currentTimeMillis() - cacheTimestamp) < CACHE_TTL_MS) {
            log.debug("[Wger] cache hit ({} ejercicios)", cached.size());
            return cached;
        }
        log.info("[Wger] fetching {} ...", WGER_URL);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(WGER_URL))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "NutriFit/1.0")
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        log.info("[Wger] status={} bodyLen={}", res.statusCode(), res.body().length());
        List<EjercicioExternoResponse> parsed = parseAll(res.body());
        log.info("[Wger] parseados {} ejercicios", parsed.size());
        cache.set(parsed);
        cacheTimestamp = System.currentTimeMillis();
        return parsed;
    }

    private List<EjercicioExternoResponse> parseAll(String body) throws JsonProcessingException {
        JsonNode root = mapper.readTree(body);
        List<EjercicioExternoResponse> result = new ArrayList<>();

        for (JsonNode exercise : root.path("results")) {
            String catName = exercise.path("category").path("name").asText("Strength");
            double met = MET_BY_CATEGORY.getOrDefault(catName.toLowerCase(), 4.0);
            findEnglishName(exercise).ifPresent(name ->
                    result.add(new EjercicioExternoResponse(name, met, catName)));
        }
        return result;
    }

    private java.util.Optional<String> findEnglishName(JsonNode exercise) {
        for (JsonNode t : exercise.path("translations")) {
            if (t.path("language").asInt() == 2) {
                String name = t.path("name").asText("").trim();
                if (!name.isEmpty()) return java.util.Optional.of(name);
            }
        }
        return java.util.Optional.empty();
    }
}
