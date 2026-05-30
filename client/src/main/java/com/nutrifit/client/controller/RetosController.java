package com.nutrifit.client.controller;

import com.nutrifit.client.NutriFitClientApplication;
import com.nutrifit.client.model.RetoDto;
import com.nutrifit.client.service.RetosApiClient;
import com.nutrifit.client.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.List;

public class RetosController {

    @FXML private TableView<RetoDto> retosTable;
    @FXML private Label statusLabel;

    private final RetosApiClient retosApiClient = new RetosApiClient();

    @FXML
    public void initialize() {
        cargarRetos();
    }

    private void cargarRetos() {
        if (!SessionManager.isLoggedIn()) {
            mostrarEstado("No hay sesión activa", TipoEstado.ERROR);
            return;
        }

        Task<List<RetoDto>> task = new Task<>() {
            @Override
            protected List<RetoDto> call() throws Exception {
                return retosApiClient.obtenerRetos(SessionManager.getUsuarioId());
            }
        };

        task.setOnSucceeded(event -> {
            List<RetoDto> retos = task.getValue();
            retosTable.setItems(FXCollections.observableArrayList(retos));
            mostrarEstado("Retos cargados correctamente", TipoEstado.EXITO);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error: " + (error != null ? error.getMessage() : "Error desconocido"), TipoEstado.ERROR);
        });

        mostrarEstado("Cargando retos...", TipoEstado.INFO);
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
