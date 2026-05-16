package com.nutrifit.backend.alimento.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.alimento.dto.AlimentoExternoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
import java.util.Optional;

@Service
public class OpenFoodFactsService {

    private static final Logger log = LoggerFactory.getLogger(OpenFoodFactsService.class);

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
            String body = res.body();
            log.info("[OpenFoodFacts] status={} bodyLen={}", res.statusCode(), body.length());
            if (res.statusCode() != 200) {
                if (log.isWarnEnabled()) {
                    log.warn("[OpenFoodFacts] non-200: body={}", body.substring(0, Math.min(300, body.length())));
                }
                return List.of();
            }
            return parse(body);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[OpenFoodFacts] interrumpido", e);
            return List.of();
        } catch (Exception e) {
            log.error("[OpenFoodFacts] error: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private List<AlimentoExternoResponse> parse(String body) throws IOException {
        JsonNode root = mapper.readTree(body);
        JsonNode products = root.path("products");
        List<AlimentoExternoResponse> result = new ArrayList<>();

        for (JsonNode p : products) {
            toAlimento(p).ifPresent(result::add);
        }
        return result;
    }

    private Optional<AlimentoExternoResponse> toAlimento(JsonNode p) {
        String nombre = p.path("product_name").asText("").trim();
        if (nombre.isEmpty()) return Optional.empty();

        JsonNode n = p.path("nutriments");
        double kcal = n.path("energy-kcal_100g").asDouble(-1);
        if (kcal < 0) return Optional.empty();

        return Optional.of(new AlimentoExternoResponse(
                nombre,
                round(kcal),
                round(n.path("proteins_100g").asDouble(0)),
                round(n.path("fat_100g").asDouble(0)),
                round(n.path("carbohydrates_100g").asDouble(0)),
                "openfoodfacts"
        ));
    }

    private double round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
