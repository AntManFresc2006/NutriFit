package com.nutrifit.client.controller;

import com.nutrifit.client.NutriFitClientApplication;
import com.nutrifit.client.model.PesoHistorialDto;
import com.nutrifit.client.model.PesoHistorialRequestDto;
import com.nutrifit.client.service.PesoHistorialApiClient;
import com.nutrifit.client.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class PesoHistorialController {

    @FXML private DatePicker fechaPicker;
    @FXML private TextField pesoField;
    @FXML private TableView<PesoHistorialDto> pesoTable;
    @FXML private Label statusLabel;

    private final PesoHistorialApiClient pesoHistorialApiClient = new PesoHistorialApiClient();

    @FXML
    public void initialize() {
        fechaPicker.setValue(LocalDate.now());
        cargarHistorial();
    }

    private void cargarHistorial() {
        if (!SessionManager.isLoggedIn()) {
            mostrarEstado("No hay sesión activa", TipoEstado.ERROR);
            return;
        }

        Task<List<PesoHistorialDto>> task = new Task<>() {
            @Override
            protected List<PesoHistorialDto> call() throws Exception {
                return pesoHistorialApiClient.obtenerHistorial(SessionManager.getUsuarioId(), 30);
            }
        };

        task.setOnSucceeded(event -> {
            List<PesoHistorialDto> historial = task.getValue();
            pesoTable.setItems(FXCollections.observableArrayList(historial));
            mostrarEstado("Historial cargado correctamente", TipoEstado.EXITO);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error: " + (error != null ? error.getMessage() : "Error desconocido"), TipoEstado.ERROR);
        });

        mostrarEstado("Cargando historial...", TipoEstado.INFO);
        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void onRegistrarPeso() {
        if (!SessionManager.isLoggedIn()) {
            mostrarEstado("No hay sesión activa", TipoEstado.ERROR);
            return;
        }

        LocalDate fecha = fechaPicker.getValue();
        if (fecha == null) {
            mostrarEstado("Selecciona una fecha válida", TipoEstado.ERROR);
            return;
        }

        String pesoStr = pesoField.getText().trim();
        if (pesoStr.isEmpty()) {
            mostrarEstado("Ingresa el peso", TipoEstado.ERROR);
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(pesoStr);
            if (peso < 20.0 || peso > 500.0) {
                mostrarEstado("El peso debe estar entre 20 y 500 kg", TipoEstado.ERROR);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarEstado("Peso inválido. Usa formato numérico (ej: 70.5)", TipoEstado.ERROR);
            return;
        }

        PesoHistorialRequestDto request = new PesoHistorialRequestDto(fecha.toString(), peso);

        Task<PesoHistorialDto> task = new Task<>() {
            @Override
            protected PesoHistorialDto call() throws Exception {
                return pesoHistorialApiClient.registrarPeso(SessionManager.getUsuarioId(), request);
            }
        };

        task.setOnSucceeded(event -> {
            pesoField.clear();
            cargarHistorial();
            mostrarEstado("Peso registrado correctamente", TipoEstado.EXITO);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error: " + (error != null ? error.getMessage() : "Error desconocido"), TipoEstado.ERROR);
        });

        mostrarEstado("Registrando peso...", TipoEstado.INFO);
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
