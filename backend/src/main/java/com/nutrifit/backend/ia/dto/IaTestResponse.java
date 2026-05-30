package com.nutrifit.backend.ia.dto;

public class IaTestResponse {

    private final boolean ok;
    private final String error;

    public IaTestResponse(boolean ok, String error) {
        this.ok = ok;
        this.error = error;
    }

    public boolean isOk() {
        return ok;
    }

    public String getError() {
        return error;
    }
}
