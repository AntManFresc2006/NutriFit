package com.nutrifit.backend.alimento.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.alimento.dto.AlimentoExternoResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenFoodFactsService {

    private static final String OFF_URL =
            "https://world.openfoodfacts.org/cgi/search.pl?search_terms=%s&search_simple=1&action=process&json=1&page_size=12";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<AlimentoExternoResponse> buscar(String query) {
        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(OFF_URL, encoded)))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "NutriFit/1.0 (student project)")
                    .GET()
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return parse(res.body());
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<AlimentoExternoResponse> parse(String body) throws Exception {
        JsonNode root = mapper.readTree(body);
        JsonNode products = root.path("products");
        List<AlimentoExternoResponse> result = new ArrayList<>();

        for (JsonNode p : products) {
            String nombre = p.path("product_name").asText("").trim();
            if (nombre.isEmpty()) continue;

            JsonNode n = p.path("nutriments");
            double kcal = n.path("energy-kcal_100g").asDouble(-1);
            if (kcal < 0) continue;

            double proteinas = round(n.path("proteins_100g").asDouble(0));
            double grasas    = round(n.path("fat_100g").asDouble(0));
            double carbos    = round(n.path("carbohydrates_100g").asDouble(0));

            result.add(new AlimentoExternoResponse(nombre, round(kcal), proteinas, grasas, carbos, "openfoodfacts"));
        }
        return result;
    }

    private double round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
