package com.nutrifit.client.controller;

import com.nutrifit.client.model.ResumenDiarioDto;
import com.nutrifit.client.service.ResumenDiarioApiClient;
import com.nutrifit.client.session.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import com.nutrifit.client.NutriFitClientApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.time.LocalDate;

/**
 * Controlador de la pantalla de resumen diario.
 */
public class DiarioController {

    @FXML
    private DatePicker fechaPicker;

    @FXML
    private Label kcalLabel;

    @FXML
    private Label proteinasLabel;

    @FXML
    private Label grasasLabel;

    @FXML
    private Label carbosLabel;

    @FXML
    private Label statusLabel;

    private final ResumenDiarioApiClient resumenDiarioApiClient = new ResumenDiarioApiClient();

    @FXML
    public void initialize() {
        fechaPicker.setValue(LocalDate.now());
        fechaPicker.setOnAction(event -> cargarResumen());
        cargarResumen();
    }

    @FXML
    private void onRecargar() {
        cargarResumen();
    }

    private void cargarResumen() {
        if (!SessionManager.isLoggedIn()) {
            mostrarEstado("No hay sesión activa", false);
            return;
        }

        LocalDate fecha = fechaPicker.getValue();
        if (fecha == null) {
            mostrarEstado("Selecciona una fecha válida", false);
            return;
        }

        Task<ResumenDiarioDto> task = new Task<>() {
            @Override
            protected ResumenDiarioDto call() throws Exception {
                return resumenDiarioApiClient.obtenerResumen(SessionManager.getUsuarioId(), fecha.toString());
            }
        };

        task.setOnSucceeded(event -> {
            ResumenDiarioDto resumen = task.getValue();
            double tdee = SessionManager.getTdee();
            if (tdee > 0) {
                kcalLabel.setText(resumen.getKcalTotales() + " / " + tdee + " kcal");
            } else {
                kcalLabel.setText(String.valueOf(resumen.getKcalTotales()));
            }
            proteinasLabel.setText(String.valueOf(resumen.getProteinasTotales()));
            grasasLabel.setText(String.valueOf(resumen.getGrasasTotales()));
            carbosLabel.setText(String.valueOf(resumen.getCarbosTotales()));
            mostrarEstado("Resumen diario cargado correctamente", true);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error al cargar el resumen: " + (error != null ? error.getMessage() : "Error desconocido"), false);
        });

        mostrarEstado("Cargando resumen diario...", true);
        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarEstado(String mensaje, boolean ok) {
        String color = ok ? "#93c5fd" : "#fca5a5";
        statusLabel.setText(mensaje);
        statusLabel.setStyle(
                "-fx-background-color: #020617; " +
                "-fx-text-fill: " + color + "; " +
                "-fx-padding: 12; " +
                "-fx-font-size: 13px;"
        );
    }
    @FXML
    private void onVolver() {
    try {
        FXMLLoader loader = new FXMLLoader(
                NutriFitClientApplication.class.getResource("/com/nutrifit/client/food-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 1100, 650);

        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.setTitle("NutriFit - Gestión de alimentos");
        stage.setScene(scene);
        stage.show();
    } catch (Exception e) {
        mostrarEstado("No se pudo volver a la pantalla de alimentos: " + e.getMessage(), false);
    }
}

}
