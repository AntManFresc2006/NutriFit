package com.nutrifit.backend.ejercicio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.ejercicio.dto.EjercicioExternoResponse;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WgerService {

    private static final String WGER_URL =
            "https://wger.de/api/v2/exercise/search/?term=%s&language=en&format=json";

    // MET estimado por grupo muscular wger.de → Fuerza/Cardio
    private static final Map<String, Double> MET_BY_CATEGORY = Map.of(
            "abs",       3.5,
            "arms",      4.0,
            "back",      4.0,
            "calves",    3.5,
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
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(WGER_URL, encoded)))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "NutriFit/1.0")
                    .GET()
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return parse(res.body());
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<EjercicioExternoResponse> parse(String body) throws Exception {
        JsonNode root = mapper.readTree(body);
        JsonNode suggestions = root.path("suggestions");
        List<EjercicioExternoResponse> result = new ArrayList<>();

        for (JsonNode s : suggestions) {
            String nombre = s.path("value").asText("").trim();
            if (nombre.isEmpty()) continue;

            String category = s.path("data").path("category").asText("").toLowerCase();
            double met = MET_BY_CATEGORY.getOrDefault(category, 4.0);

            result.add(new EjercicioExternoResponse(nombre, met, "Fuerza"));
        }
        return result;
    }
}
