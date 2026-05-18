package com.nutrifit.backend.ia.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para actualizar la configuración de IA personalizada.
 */
public class UsuarioIaConfigRequest {

    @NotBlank(message = "proxyUrl es obligatorio")
    private String proxyUrl;

    @NotBlank(message = "model es obligatorio")
    private String model;

    @NotBlank(message = "apiKey es obligatorio")
    private String apiKey;

    public UsuarioIaConfigRequest() {
    }

    public UsuarioIaConfigRequest(String proxyUrl, String model, String apiKey) {
        this.proxyUrl = proxyUrl;
        this.model = model;
        this.apiKey = apiKey;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
