package com.nutrifit.client.controller;

import com.nutrifit.client.model.EvaluacionIaDto;
import com.nutrifit.client.model.ResumenDiarioDto;
import com.nutrifit.client.service.EvaluacionIaApiClient;
import com.nutrifit.client.service.ResumenDiarioApiClient;
import com.nutrifit.client.session.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

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
    private Label kcalQuemadasLabel;

    @FXML
    private Label balanceNetoLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea evaluacionArea;

    private final ResumenDiarioApiClient resumenDiarioApiClient = new ResumenDiarioApiClient();
    private final EvaluacionIaApiClient evaluacionIaApiClient = new EvaluacionIaApiClient();

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
            mostrarEstado("No hay sesión activa", TipoEstado.ERROR);
            return;
        }

        LocalDate fecha = fechaPicker.getValue();
        if (fecha == null) {
            mostrarEstado("Selecciona una fecha válida", TipoEstado.ERROR);
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
                kcalLabel.setText(String.format("%.0f / %.0f kcal", resumen.getKcalTotales(), tdee));
            } else {
                kcalLabel.setText(String.format("%.0f kcal", resumen.getKcalTotales()));
            }
            proteinasLabel.setText(String.format("%.1f g", resumen.getProteinasTotales()));
            grasasLabel.setText(String.format("%.1f g", resumen.getGrasasTotales()));
            carbosLabel.setText(String.format("%.1f g", resumen.getCarbosTotales()));
            kcalQuemadasLabel.setText(String.format("%.0f kcal", resumen.getKcalQuemadasTotales()));
            balanceNetoLabel.setText(String.format("%.0f kcal", resumen.getBalanceNeto()));
            mostrarEstado("Resumen diario cargado correctamente", TipoEstado.EXITO);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error al cargar el resumen: " + (error != null ? error.getMessage() : "Error desconocido"), TipoEstado.ERROR);
        });

        mostrarEstado("Cargando resumen diario...", TipoEstado.INFO);
        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarEstado(String mensaje, TipoEstado tipo) {
        String color = switch (tipo) {
            case EXITO -> "#86efac";
            case ERROR -> "#fca5a5";
            default    -> "#93c5fd";
        };
        statusLabel.setText(mensaje);
        statusLabel.setStyle(
                "-fx-background-color: #020617; " +
                "-fx-text-fill: " + color + "; " +
                "-fx-padding: 12; " +
                "-fx-font-size: 13px;"
        );
    }

    private enum TipoEstado { INFO, EXITO, ERROR }

    @FXML
    private void onEvaluarDia() {
        if (!SessionManager.isLoggedIn()) {
            mostrarEstado("No hay sesión activa", TipoEstado.ERROR);
            return;
        }

        LocalDate fecha = fechaPicker.getValue();
        if (fecha == null) {
            mostrarEstado("Selecciona una fecha válida", TipoEstado.ERROR);
            return;
        }

        Task<EvaluacionIaDto> task = new Task<>() {
            @Override
            protected EvaluacionIaDto call() throws Exception {
                return evaluacionIaApiClient.evaluarDia(SessionManager.getUsuarioId(), fecha.toString());
            }
        };

        task.setOnSucceeded(event -> {
            evaluacionArea.setText(task.getValue().getEvaluacion());
            mostrarEstado("Evaluación generada correctamente", TipoEstado.EXITO);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error al evaluar el día: " + (error != null ? error.getMessage() : "Error desconocido"), TipoEstado.ERROR);
        });

        evaluacionArea.setText("Consultando a la IA...");
        mostrarEstado("Generando evaluación nutricional...", TipoEstado.INFO);
        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
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
        mostrarEstado("No se pudo volver a la pantalla de alimentos: " + e.getMessage(), TipoEstado.ERROR);
    }
}

}
