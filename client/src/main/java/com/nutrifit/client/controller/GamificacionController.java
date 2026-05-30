package com.nutrifit.client.controller;

import com.nutrifit.client.NutriFitClientApplication;
import com.nutrifit.client.model.GamificacionDto;
import com.nutrifit.client.service.GamificacionApiClient;
import com.nutrifit.client.session.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.LocalDate;

public class GamificacionController {

    @FXML private Label rachaLabel;
    @FXML private Label nutriScoreLabel;
    @FXML private Label nutriGradeLabel;
    @FXML private Label cumpleProteinaLabel;
    @FXML private Label cumpleBalanceLabel;
    @FXML private Label cumpleEjercicioLabel;
    @FXML private Label cumpleVariedadLabel;
    @FXML private Label statusLabel;

    private final GamificacionApiClient gamificacionApiClient = new GamificacionApiClient();

    @FXML
    public void initialize() {
        cargarGamificacion();
    }

    private void cargarGamificacion() {
        if (!SessionManager.isLoggedIn()) {
            mostrarEstado("No hay sesión activa", TipoEstado.ERROR);
            return;
        }

        Task<GamificacionDto> task = new Task<>() {
            @Override
            protected GamificacionDto call() throws Exception {
                return gamificacionApiClient.obtenerGamificacion(SessionManager.getUsuarioId(), LocalDate.now());
            }
        };

        task.setOnSucceeded(event -> {
            GamificacionDto gamificacion = task.getValue();

            rachaLabel.setText(String.valueOf(gamificacion.getRacha()));
            nutriScoreLabel.setText(String.valueOf(gamificacion.getNutriScore()));
            nutriGradeLabel.setText(gamificacion.getNutriGrade() != null ? gamificacion.getNutriGrade() : "—");
            cumpleProteinaLabel.setText(gamificacion.isCumpleProteina() ? "✓ Completado" : "✗ Pendiente");
            cumpleBalanceLabel.setText(gamificacion.isCumpleBalance() ? "✓ Completado" : "✗ Pendiente");
            cumpleEjercicioLabel.setText(gamificacion.isCumpleEjercicio() ? "✓ Completado" : "✗ Pendiente");
            cumpleVariedadLabel.setText(gamificacion.isCumpleVariedad() ? "✓ Completado" : "✗ Pendiente");

            mostrarEstado("Gamificación cargada correctamente", TipoEstado.EXITO);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error: " + (error != null ? error.getMessage() : "Error desconocido"), TipoEstado.ERROR);
        });

        mostrarEstado("Cargando gamificación...", TipoEstado.INFO);
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
            mostrarEstado("No se pudo volver: " + e.getMessage(), TipoEstado.ERROR);
        }
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
}
