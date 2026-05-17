package com.nutrifit.backend.escaner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.backend.common.exception.ResourceNotFoundException;
import com.nutrifit.backend.escaner.dto.EscanerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Service
public class EscanerServiceImpl implements EscanerService {

    private static final Logger log = LoggerFactory.getLogger(EscanerServiceImpl.class);

    private static final String OFF_BARCODE_URL = "https://world.openfoodfacts.org/api/v0/product/%s.json";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public EscanerResponse buscarPorBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            throw new ResourceNotFoundException("Código de barras vacío");
        }

        try {
            String url = String.format(OFF_BARCODE_URL, barcode.trim());
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "NutriFit/1.0 (student project)")
                    .GET()
                    .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            String body = res.body();
            log.info("[OpenFoodFacts-Barcode] status={} barcode={} bodyLen={}", res.statusCode(), barcode, body.length());

            if (res.statusCode() != 200) {
                if (log.isWarnEnabled()) {
                    log.warn("[OpenFoodFacts-Barcode] non-200 response for barcode={}", barcode);
                }
                throw new ResourceNotFoundException("No se encontró producto para el código de barras: " + barcode);
            }

            return parse(body, barcode)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró información nutricional para el código de barras: " + barcode));

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[OpenFoodFacts-Barcode] interrumpido para barcode={}", barcode, e);
            throw new ResourceNotFoundException("La búsqueda fue interrumpida");
        } catch (Exception e) {
            log.error("[OpenFoodFacts-Barcode] error para barcode={}: {}", barcode, e.getMessage(), e);
            throw new ResourceNotFoundException("Error al buscar el producto: " + e.getMessage());
        }
    }

    private Optional<EscanerResponse> parse(String body, String barcode) throws Exception {
        JsonNode root = mapper.readTree(body);
        JsonNode product = root.path("product");

        if (product.isMissingNode()) {
            return Optional.empty();
        }

        String nombre = product.path("product_name").asText("").trim();
        if (nombre.isEmpty()) {
            return Optional.empty();
        }

        JsonNode nutriments = product.path("nutriments");
        double kcal = nutriments.path("energy-kcal_100g").asDouble(-1);
        if (kcal < 0) {
            return Optional.empty();
        }

        String marca = product.path("brands").asText("").trim();
        String imagenUrl = product.path("image_url").asText(null);

        return Optional.of(new EscanerResponse(
                nombre,
                marca.isEmpty() ? "No especificada" : marca,
                round(kcal),
                round(nutriments.path("proteins_100g").asDouble(0)),
                round(nutriments.path("fat_100g").asDouble(0)),
                round(nutriments.path("carbohydrates_100g").asDouble(0)),
                imagenUrl
        ));
    }

    private double round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
