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

/**
 * Implementación del servicio de escaneo de códigos de barras.
 * Orquesta las consultas a la API Open Food Facts para obtener información de productos.
 */
@Service
public class EscanerServiceImpl implements EscanerService {

    private static final Logger log = LoggerFactory.getLogger(EscanerServiceImpl.class);

    private static final String OFF_BARCODE_URL =
            "https://world.openfoodfacts.org/api/v2/product/%s?fields=product_name,product_name_es,nutriments,brands,image_url&lc=es";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Busca un producto en la API Open Food Facts a partir de su código de barras.
     *
     * @param barcode código de barras del producto
     * @return información nutricional y detalles del producto encontrado
     * @throws ResourceNotFoundException si no se encuentra el producto o hay error en la búsqueda
     */
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

    private Optional<EscanerResponse> parse(String body, String barcode) throws java.io.IOException {
        JsonNode root = mapper.readTree(body);

        // v2 API devuelve "product" directamente; v0 también
        JsonNode product = root.path("product");
        if (product.isMissingNode() || product.isNull()) {
            return Optional.empty();
        }

        // Preferir nombre en español
        String nombre = product.path("product_name_es").asText("").trim();
        if (nombre.isEmpty()) {
            nombre = product.path("product_name").asText("").trim();
        }
        if (nombre.isEmpty()) {
            return Optional.empty();
        }

        JsonNode n = product.path("nutriments");

        double kcal = resolverKcal(n);
        if (kcal < 0) {
            return Optional.empty();
        }

        String marca = product.path("brands").asText("").trim();
        String imagenUrl = product.path("image_url").asText(null);
        if (imagenUrl != null && imagenUrl.isBlank()) imagenUrl = null;

        return Optional.of(new EscanerResponse(
                nombre,
                marca.isEmpty() ? "No especificada" : marca,
                round(kcal),
                round(n.path("proteins_100g").asDouble(0)),
                round(n.path("fat_100g").asDouble(0)),
                round(n.path("carbohydrates_100g").asDouble(0)),
                imagenUrl
        ));
    }

    private double resolverKcal(JsonNode n) {
        double kcal = n.path("energy-kcal_100g").asDouble(-1);
        if (kcal >= 0) return kcal;

        double kj = n.path("energy_100g").asDouble(-1);
        if (kj >= 0) return kj / 4.184;

        double prot = n.path("proteins_100g").asDouble(0);
        double fat  = n.path("fat_100g").asDouble(0);
        double carb = n.path("carbohydrates_100g").asDouble(0);
        if (prot > 0 || fat > 0 || carb > 0) return 4 * prot + 9 * fat + 4 * carb;

        return -1;
    }

    private double round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
