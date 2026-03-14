package com.nutrifit.client.service;

import com.nutrifit.client.model.AlimentoFx;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlimentoApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/alimentos";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public List<AlimentoFx> getAll() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseAlimentos(response.body());
    }

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
}