package com.nutrifit.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.client.model.EjercicioDto;
import com.nutrifit.client.session.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EjercicioApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/ejercicios";
    private static final String AUTH_HEADER = "Authorization";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static String bearerToken() {
        return "Bearer " + SessionManager.getToken();
    }

    public List<EjercicioDto> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header(AUTH_HEADER, bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener ejercicios");

        return objectMapper.readValue(response.body(), new TypeReference<List<EjercicioDto>>() {});
    }

    public List<EjercicioDto> search(String query) throws IOException, InterruptedException {
        String url = BASE_URL + "?q=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al buscar ejercicios");

        return objectMapper.readValue(response.body(), new TypeReference<List<EjercicioDto>>() {});
    }

    public EjercicioDto crear(String nombre, double met, String categoria) throws IOException, InterruptedException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", nombre);
        body.put("met", met);
        body.put("categoria", categoria);
        String json = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al crear ejercicio");

        return objectMapper.readValue(response.body(), EjercicioDto.class);
    }

    public List<CategoriaWger> obtenerCategorias() throws IOException, InterruptedException {
        String url = "https://wger.de/api/v2/exercisecategory/?format=json";
        HttpResponse<String> resp = httpClient.send(
                HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(10)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("Error al obtener categorías wger. Código HTTP: " + resp.statusCode());
        }
        JsonNode root = objectMapper.readTree(resp.body());
        JsonNode results = root.path("results");
        List<CategoriaWger> categorias = new ArrayList<>();
        if (!results.isArray()) return categorias;
        for (JsonNode c : results) {
            int id = c.path("id").asInt();
            String nombre = traducirCategoria(c.path("name").asText());
            categorias.add(new CategoriaWger(id, nombre));
        }
        return categorias;
    }

    public List<WgerEjercicio> cargarCatalogoWger() throws IOException, InterruptedException {
        // Primera página para obtener el total
        String primeraUrl = "https://wger.de/api/v2/exerciseinfo/?format=json&language=2&limit=100&offset=0";
        HttpResponse<String> primeraResp = httpClient.send(
                HttpRequest.newBuilder().uri(URI.create(primeraUrl)).timeout(Duration.ofSeconds(15)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        if (primeraResp.statusCode() < 200 || primeraResp.statusCode() >= 300) {
            throw new IOException("Error al consultar wger.de. Código HTTP: " + primeraResp.statusCode());
        }

        JsonNode primeraRoot = objectMapper.readTree(primeraResp.body());
        int total = primeraRoot.path("count").asInt();

        List<WgerEjercicio> catalogo = new ArrayList<>(parsearPagina(primeraRoot));

        // Páginas restantes en paralelo
        List<CompletableFuture<List<WgerEjercicio>>> paginas = new ArrayList<>();
        for (int offset = 100; offset < total; offset += 100) {
            String url = "https://wger.de/api/v2/exerciseinfo/?format=json&language=2&limit=100&offset=" + offset;
            paginas.add(httpClient.sendAsync(
                    HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(15)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            ).thenApply(r -> {
                try { return parsearPagina(objectMapper.readTree(r.body())); }
                catch (Exception e) { return List.<WgerEjercicio>of(); }
            }).exceptionally(e -> List.of()));
        }
        for (CompletableFuture<List<WgerEjercicio>> f : paginas) catalogo.addAll(f.join());

        return catalogo;
    }

    private List<WgerEjercicio> parsearPagina(JsonNode root) {
        List<WgerEjercicio> resultado = new ArrayList<>();
        JsonNode results = root.path("results");
        if (!results.isArray()) return resultado;
        for (JsonNode r : results) {
            String nombre = "";
            for (JsonNode t : r.path("translations")) {
                if (t.path("language").asInt() == 2) { nombre = t.path("name").asText(); break; }
            }
            if (nombre.isBlank()) continue;
            int catId = r.path("category").path("id").asInt();
            JsonNode catNode = r.path("category").path("name");
            String cat = catNode.isMissingNode() || catNode.isNull() ? "General" : catNode.asText();
            resultado.add(new WgerEjercicio(nombre, traducirCategoria(cat), catId));
        }
        return resultado;
    }

    private static String traducirCategoria(String cat) {
        return switch (cat) {
            case "Abs"       -> "Abdominales";
            case "Arms"      -> "Brazos";
            case "Back"      -> "Espalda";
            case "Calves"    -> "Pantorrillas";
            case "Cardio"    -> "Cardio";
            case "Chest"     -> "Pecho";
            case "Legs"      -> "Piernas";
            case "Shoulders" -> "Hombros";
            default          -> cat;
        };
    }

    private void validarRespuesta(HttpResponse<String> response, String prefijo) throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(prefijo + ". Código HTTP: " + response.statusCode() + " - " + response.body());
        }
    }

    public static class WgerEjercicio {
        private final String nombre;
        private final String categoria;
        private final int categoriaId;

        public WgerEjercicio(String nombre, String categoria, int categoriaId) {
            this.nombre = nombre;
            this.categoria = categoria;
            this.categoriaId = categoriaId;
        }

        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public int getCategoriaId() { return categoriaId; }

        @Override
        public String toString() { return nombre + " (" + categoria + ")"; }
    }

    public static class CategoriaWger {
        private final int id;
        private final String nombre;

        public CategoriaWger(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }

        @Override
        public String toString() { return nombre; }
    }
}
