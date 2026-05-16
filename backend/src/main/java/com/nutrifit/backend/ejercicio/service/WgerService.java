package com.nutrifit.backend.ejercicio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.ejercicio.dto.EjercicioExternoResponse;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
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

@Service
public class WgerService {

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

    public List<EjercicioExternoResponse> buscar(String query) {
        try {
            List<EjercicioExternoResponse> all = getAll();
            String q = query.toLowerCase().trim();
            return all.stream()
                    .filter(e -> e.nombre().toLowerCase().contains(q))
                    .limit(10)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        } catch (IOException e) {
            return List.of();
        }
    }

    private List<EjercicioExternoResponse> getAll() throws IOException, InterruptedException {
        List<EjercicioExternoResponse> cached = cache.get();
        if (cached != null && (System.currentTimeMillis() - cacheTimestamp) < CACHE_TTL_MS) {
            return cached;
        }
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(WGER_URL))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "NutriFit/1.0")
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        List<EjercicioExternoResponse> parsed = parseAll(res.body());
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
