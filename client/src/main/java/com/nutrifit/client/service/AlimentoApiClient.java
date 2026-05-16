package com.nutrifit.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.nutrifit.client.model.AlimentoDto;
import com.nutrifit.client.model.AlimentoFx;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Cliente HTTP del módulo de alimentos. */
public class AlimentoApiClient extends BaseApiClient {

    private static final String BASE_URL = BACKEND_URL + "/api/alimentos";

    public List<AlimentoFx> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener alimentos");

        List<AlimentoDto> dtos = objectMapper.readValue(response.body(), new TypeReference<List<AlimentoDto>>() {});
        return dtos.stream().map(this::toFx).toList();
    }

    public List<AlimentoFx> search(String query) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?q=" + encoded))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al buscar alimentos");

        List<AlimentoDto> dtos = objectMapper.readValue(response.body(), new TypeReference<List<AlimentoDto>>() {});
        return dtos.stream().map(this::toFx).toList();
    }

    public void create(AlimentoFx alimento) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(toDto(alimento));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al crear alimento");
    }

    public void update(AlimentoFx alimento) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(toDto(alimento));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + alimento.getId()))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al actualizar alimento");
    }

    public void delete(long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header(AUTH_HEADER, bearerToken())
                .DELETE().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al eliminar alimento");
    }

    public List<AlimentoFx> buscarEnOpenFoodFacts(String query) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=" + encoded
                + "&json=1&lc=es&page_size=10";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Error al consultar Open Food Facts. Código HTTP: " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode products = root.path("products");
        if (!products.isArray() || products.isEmpty()) return List.of();

        List<AlimentoFx> resultados = new ArrayList<>();
        for (JsonNode p : products) {
            JsonNode n = p.path("nutriments");
            AlimentoFx a = new AlimentoFx();
            a.setNombre(p.path("product_name").asText());
            a.setKcalPor100g(n.path("energy-kcal_100g").asDouble(0.0));
            a.setProteinasG(n.path("proteins_100g").asDouble(0.0));
            a.setGrasasG(n.path("fat_100g").asDouble(0.0));
            a.setCarbosG(n.path("carbohydrates_100g").asDouble(0.0));
            a.setPorcionG(100.0);
            a.setFuente("Open Food Facts");
            resultados.add(a);
        }
        return resultados;
    }

    private AlimentoFx toFx(AlimentoDto dto) {
        AlimentoFx a = new AlimentoFx();
        a.setId(dto.getId() != null ? dto.getId() : 0L);
        a.setNombre(dto.getNombre());
        a.setPorcionG(dto.getPorcionG() != null ? dto.getPorcionG() : 0.0);
        a.setKcalPor100g(dto.getKcalPor100g() != null ? dto.getKcalPor100g() : 0.0);
        a.setProteinasG(dto.getProteinasG() != null ? dto.getProteinasG() : 0.0);
        a.setGrasasG(dto.getGrasasG() != null ? dto.getGrasasG() : 0.0);
        a.setCarbosG(dto.getCarbosG() != null ? dto.getCarbosG() : 0.0);
        a.setFuente(dto.getFuente() != null ? dto.getFuente() : "");
        return a;
    }

    private AlimentoDto toDto(AlimentoFx fx) {
        AlimentoDto dto = new AlimentoDto();
        if (fx.getId() > 0) dto.setId(fx.getId());
        dto.setNombre(fx.getNombre());
        dto.setPorcionG(fx.getPorcionG());
        dto.setKcalPor100g(fx.getKcalPor100g());
        dto.setProteinasG(fx.getProteinasG());
        dto.setGrasasG(fx.getGrasasG());
        dto.setCarbosG(fx.getCarbosG());
        dto.setFuente(fx.getFuente());
        return dto;
    }
}
