package com.nutrifit.client.service;

import com.nutrifit.client.model.AlimentoFx;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cliente HTTP del módulo de alimentos.
 * Se encarga de comunicar la interfaz JavaFX con la API REST del backend.
 */
public class AlimentoApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/alimentos";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Solicita al backend la lista completa de alimentos.
     */
    public List<AlimentoFx> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener alimentos");
        return parseAlimentos(response.body());
    }

    /**
     * Busca alimentos por nombre usando el parámetro q del backend.
     */
    public List<AlimentoFx> search(String query) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?q=" + encoded))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al buscar alimentos");
        return parseAlimentos(response.body());
    }

    /**
     * Envía al backend un nuevo alimento en formato JSON.
     */
    public void create(AlimentoFx alimento) throws IOException, InterruptedException {
        String json = toJson(alimento);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al crear alimento");
    }

    /**
     * Actualiza un alimento existente en backend.
     */
    public void update(AlimentoFx alimento) throws IOException, InterruptedException {
        String json = toJson(alimento);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + alimento.getId()))
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
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al eliminar alimento");
    }

    /**
     * Convierte la respuesta JSON del backend en una lista de objetos JavaFX.
     * Por ahora se usa un parseo simple basado en expresiones regulares.
     */
    private List<AlimentoFx> parseAlimentos(String json) {
        List<AlimentoFx> alimentos = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "\\{\\s*\"id\":\\s*(\\d+),\\s*\"nombre\":\\s*\"([^\"]+)\",\\s*\"porcionG\":\\s*([\\d.]+),\\s*\"kcalPor100g\":\\s*([\\d.]+),\\s*\"proteinasG\":\\s*([\\d.]+),\\s*\"grasasG\":\\s*([\\d.]+),\\s*\"carbosG\":\\s*([\\d.]+),\\s*\"fuente\":\\s*\"([^\"]*)\"\\s*\\}"
        );

        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            AlimentoFx alimento = new AlimentoFx();
            alimento.setId(Long.parseLong(matcher.group(1)));
            alimento.setNombre(matcher.group(2));
            alimento.setPorcionG(Double.parseDouble(matcher.group(3)));
            alimento.setKcalPor100g(Double.parseDouble(matcher.group(4)));
            alimento.setProteinasG(Double.parseDouble(matcher.group(5)));
            alimento.setGrasasG(Double.parseDouble(matcher.group(6)));
            alimento.setCarbosG(Double.parseDouble(matcher.group(7)));
            alimento.setFuente(matcher.group(8));
            alimentos.add(alimento);
        }

        return alimentos;
    }

    /**
     * Genera el JSON de entrada para create y update.
     */
    private String toJson(AlimentoFx alimento) {
        return """
                {
                  "nombre": "%s",
                  "porcionG": %s,
                  "kcalPor100g": %s,
                  "proteinasG": %s,
                  "grasasG": %s,
                  "carbosG": %s,
                  "fuente": "%s"
                }
                """.formatted(
                escapeJson(alimento.getNombre()),
                alimento.getPorcionG(),
                alimento.getKcalPor100g(),
                alimento.getProteinasG(),
                alimento.getGrasasG(),
                alimento.getCarbosG(),
                escapeJson(alimento.getFuente())
        );
    }

    /**
     * Escapa comillas dobles para no romper el JSON generado manualmente.
     */
    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }

    /**
     * Lanza una excepción si el backend no responde con código 2xx.
     */
    private void validarRespuesta(HttpResponse<String> response, String prefijo) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(prefijo + ". Código HTTP: " + response.statusCode() + " - " + response.body());
        }
    }
}