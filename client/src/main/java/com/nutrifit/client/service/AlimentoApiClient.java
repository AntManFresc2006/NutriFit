package com.nutrifit.client.service;

import com.nutrifit.client.model.AlimentoFx;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
     *
     * @return lista de alimentos adaptados al modelo JavaFX
     * @throws IOException si ocurre un error de comunicación
     * @throws InterruptedException si la petición es interrumpida
     */
    public List<AlimentoFx> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseAlimentos(response.body());
    }

    /**
     * Envía al backend un nuevo alimento en formato JSON.
     *
     * @param alimento datos del alimento a crear
     * @throws IOException si la petición falla o el backend devuelve un error
     * @throws InterruptedException si la petición es interrumpida
     */
    public void create(AlimentoFx alimento) throws IOException, InterruptedException {
        String json = """
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

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Error al crear alimento. Código HTTP: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Convierte la respuesta JSON del backend en una lista de objetos JavaFX.
     * Por ahora se usa un parseo simple basado en expresiones regulares.
     *
     * @param json respuesta JSON devuelta por la API
     * @return lista de alimentos adaptados al cliente
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
     * Escapa comillas dobles para evitar romper el JSON generado manualmente.
     *
     * @param value texto de entrada
     * @return texto seguro para insertarse dentro del JSON
     */
    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }
}