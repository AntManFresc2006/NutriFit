package com.nutrifit.backend.ia.model;

public class UsuarioIaConfig {

    private Long usuarioId;
    private String proxyUrl;
    private String model;
    private String apiKey;

    public UsuarioIaConfig() {
    }

    public UsuarioIaConfig(Long usuarioId, String proxyUrl, String model, String apiKey) {
        this.usuarioId = usuarioId;
        this.proxyUrl = proxyUrl;
        this.model = model;
        this.apiKey = apiKey;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
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
