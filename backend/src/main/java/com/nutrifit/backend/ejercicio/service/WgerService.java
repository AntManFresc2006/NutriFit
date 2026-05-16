package com.nutrifit.backend.ejercicio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.ejercicio.dto.EjercicioExternoResponse;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WgerService {

    private static final String WGER_URL =
            "https://wger.de/api/v2/exerciseinfo/?format=json&language=2&limit=100";

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
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<EjercicioExternoResponse> buscar(String query) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(WGER_URL))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "NutriFit/1.0")
                    .GET()
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return parse(res.body(), query.toLowerCase().trim());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<EjercicioExternoResponse> parse(String body, String query) throws Exception {
        JsonNode root = mapper.readTree(body);
        List<EjercicioExternoResponse> result = new ArrayList<>();

        for (JsonNode exercise : root.path("results")) {
            if (result.size() >= 10) break;

            String catName = exercise.path("category").path("name").asText("Fuerza");
            double met = MET_BY_CATEGORY.getOrDefault(catName.toLowerCase(), 4.0);

            for (JsonNode t : exercise.path("translations")) {
                String name = t.path("name").asText("").trim();
                if (!name.isEmpty() && name.toLowerCase().contains(query)) {
                    result.add(new EjercicioExternoResponse(name, met, catName));
                    break;
                }
            }
        }
        return result;
    }
}
