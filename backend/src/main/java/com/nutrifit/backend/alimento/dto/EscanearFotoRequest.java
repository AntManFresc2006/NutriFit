package com.nutrifit.backend.alimento.dto;

import jakarta.validation.constraints.NotBlank;

public class EscanearFotoRequest {

    @NotBlank(message = "La imagen en base64 es obligatoria")
    private String imagenBase64;

    @NotBlank(message = "El tipo MIME es obligatorio")
    private String mimeType;

    public EscanearFotoRequest() {
    }

    public EscanearFotoRequest(String imagenBase64, String mimeType) {
        this.imagenBase64 = imagenBase64;
        this.mimeType = mimeType;
    }

    public String getImagenBase64() {
        return imagenBase64;
    }

    public void setImagenBase64(String imagenBase64) {
        this.imagenBase64 = imagenBase64;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
