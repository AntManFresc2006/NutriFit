package com.nutrifit.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.client.model.AlimentoDto;
import com.nutrifit.client.model.AlimentoFx;
import com.nutrifit.client.session.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Cliente HTTP del módulo de alimentos.
 * Se encarga de comunicar la interfaz JavaFX con la API REST del backend.
 */
public class AlimentoApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/alimentos";
    private static final String AUTH_HEADER = "Authorization";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String bearerToken() {
        return "Bearer " + SessionManager.getToken();
    }

    /**
     * Solicita al backend la lista completa de alimentos.
     */
    public List<AlimentoFx> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header(AUTH_HEADER, bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener alimentos");

        List<AlimentoDto> dtos = objectMapper.readValue(
                response.body(),
                new TypeReference<List<AlimentoDto>>() {}
        );

        return dtos.stream().map(this::toFx).toList();
    }

    /**
     * Busca alimentos por nombre usando el parámetro q del backend.
     */
    public List<AlimentoFx> search(String query) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?q=" + encoded))
                .header(AUTH_HEADER, bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al buscar alimentos");

        List<AlimentoDto> dtos = objectMapper.readValue(
                response.body(),
                new TypeReference<List<AlimentoDto>>() {}
        );

        return dtos.stream().map(this::toFx).toList();
    }

    /**
     * Envía al backend un nuevo alimento.
     */
    public void create(AlimentoFx alimento) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(toDto(alimento));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al crear alimento");
    }

    /**
     * Actualiza un alimento existente.
     */
    public void update(AlimentoFx alimento) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(toDto(alimento));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + alimento.getId()))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al actualizar alimento");
    }

    /**
     * Elimina un alimento por su id.
     */
    public void delete(long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .header(AUTH_HEADER, bearerToken())
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al eliminar alimento");
    }

    /**
     * Convierte un DTO plano en el modelo observable usado por JavaFX.
     */
    private AlimentoFx toFx(AlimentoDto dto) {
        AlimentoFx alimento = new AlimentoFx();
        alimento.setId(dto.getId() != null ? dto.getId() : 0L);
        alimento.setNombre(dto.getNombre());
        alimento.setPorcionG(dto.getPorcionG() != null ? dto.getPorcionG() : 0.0);
        alimento.setKcalPor100g(dto.getKcalPor100g() != null ? dto.getKcalPor100g() : 0.0);
        alimento.setProteinasG(dto.getProteinasG() != null ? dto.getProteinasG() : 0.0);
        alimento.setGrasasG(dto.getGrasasG() != null ? dto.getGrasasG() : 0.0);
        alimento.setCarbosG(dto.getCarbosG() != null ? dto.getCarbosG() : 0.0);
        alimento.setFuente(dto.getFuente() != null ? dto.getFuente() : "");
        return alimento;
    }

    /**
     * Convierte el modelo observable de JavaFX en un DTO plano para enviarlo a la API.
     */
    private AlimentoDto toDto(AlimentoFx fx) {
        AlimentoDto dto = new AlimentoDto();
        if (fx.getId() > 0) {
            dto.setId(fx.getId());
        }
        dto.setNombre(fx.getNombre());
        dto.setPorcionG(fx.getPorcionG());
        dto.setKcalPor100g(fx.getKcalPor100g());
        dto.setProteinasG(fx.getProteinasG());
        dto.setGrasasG(fx.getGrasasG());
        dto.setCarbosG(fx.getCarbosG());
        dto.setFuente(fx.getFuente());
        return dto;
    }

    /**
     * Lanza una excepción si el backend no responde con un código 2xx.
     */
    private void validarRespuesta(HttpResponse<String> response, String prefijo) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(prefijo + ". Código HTTP: " + response.statusCode() + " - " + response.body());
        }
    }
}