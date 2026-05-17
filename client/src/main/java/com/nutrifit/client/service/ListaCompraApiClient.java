package com.nutrifit.client.service;

import com.nutrifit.client.model.ListaItemDto;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ListaCompraApiClient extends BaseApiClient {

    private static final String BASE_URL = BACKEND_URL + "/api/usuarios";

    public List<ListaItemDto> obtenerLista(Long usuarioId) throws IOException, InterruptedException {
        String url = BASE_URL + "/" + usuarioId + "/lista-compra";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .GET().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al obtener lista de compra");

        ListaItemDto[] items = objectMapper.readValue(response.body(), ListaItemDto[].class);
        return Arrays.asList(items);
    }

    public ListaItemDto addItem(Long usuarioId, String nombre, String cantidad, String categoria) throws IOException, InterruptedException {
        String url = BASE_URL + "/" + usuarioId + "/lista-compra";

        ListaItemDto nuevoItem = new ListaItemDto();
        nuevoItem.setNombre(nombre);
        nuevoItem.setCantidad(cantidad);
        nuevoItem.setCategoria(categoria);
        nuevoItem.setCompletado(false);

        String body = objectMapper.writeValueAsString(nuevoItem);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al añadir item a la lista");
        return objectMapper.readValue(response.body(), ListaItemDto.class);
    }

    public void toggleItem(Long usuarioId, Long itemId) throws IOException, InterruptedException {
        String url = BASE_URL + "/" + usuarioId + "/lista-compra/" + itemId + "/toggle";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .PUT(HttpRequest.BodyPublishers.noBody()).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al marcar/desmarcar item");
    }

    public void eliminarItem(Long usuarioId, Long itemId) throws IOException, InterruptedException {
        String url = BASE_URL + "/" + usuarioId + "/lista-compra/" + itemId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .DELETE().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al eliminar item");
    }

    public void limpiarCompletados(Long usuarioId) throws IOException, InterruptedException {
        String url = BASE_URL + "/" + usuarioId + "/lista-compra/completados";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(AUTH_HEADER, bearerToken())
                .DELETE().build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validarRespuesta(response, "Error al limpiar completados");
    }
}
