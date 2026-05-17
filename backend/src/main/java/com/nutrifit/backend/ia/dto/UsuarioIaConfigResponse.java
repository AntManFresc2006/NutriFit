package com.nutrifit.backend.ia.dto;

public class UsuarioIaConfigResponse {

    private String proxyUrl;
    private String model;
    private String apiKey;

    public UsuarioIaConfigResponse() {
    }

    public UsuarioIaConfigResponse(String proxyUrl, String model, String apiKey) {
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
