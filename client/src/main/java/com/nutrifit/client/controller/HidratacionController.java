package com.nutrifit.client.controller;

import com.nutrifit.client.NutriFitClientApplication;
import com.nutrifit.client.model.HidratacionDto;
import com.nutrifit.client.service.HidratacionApiClient;
import com.nutrifit.client.session.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class HidratacionController {

    @FXML private DatePicker fechaPicker;
    @FXML private Label totalLabel;
    @FXML private Label objetivoLabel;
    @FXML private Label porcentajeLabel;
    @FXML private ProgressBar progresoBar;
    @FXML private TextField cantidadField;
    @FXML private ComboBox<String> fuenteCombo;
    @FXML private Label statusLabel;

    private final HidratacionApiClient apiClient = new HidratacionApiClient();

    private static final String STYLE_TEXT_FILL = "-fx-text-fill: ";

    @FXML
    public void initialize() {
        fechaPicker.setValue(LocalDate.now());
        fechaPicker.setOnAction(e -> cargarResumen());

        fuenteCombo.setItems(FXCollections.observableArrayList("AGUA", "TÉ", "CAFÉ", "OTRO"));
        fuenteCombo.setValue("AGUA");

        cargarResumen();
    }

    @FXML
    private void onRegistrar250() {
        registrar(250);
    }

    @FXML
    private void onRegistrar500() {
        registrar(500);
    }

    @FXML
    private void onRegistrar750() {
        registrar(750);
    }

    @FXML
    private void onRegistrarPersonalizado() {
        String texto = cantidadField.getText().trim();
        if (texto.isEmpty()) {
            mostrarEstado("Introduce una cantidad en ml", TipoEstado.ERROR);
            return;
        }
        try {
            int ml = Integer.parseInt(texto);
            if (ml <= 0) throw new NumberFormatException();
            registrar(ml);
            cantidadField.clear();
        } catch (NumberFormatException e) {
            mostrarEstado("Cantidad inválida — introduce un número positivo", TipoEstado.ERROR);
        }
    }

    private void registrar(int ml) {
        String fecha = fechaPicker.getValue().toString();
        String fuente = fuenteCombo.getValue();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.registrarHidratacion(SessionManager.getUsuarioId(), ml, fuente, fecha);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            cargarResumen();
            mostrarEstado("+" + ml + " ml registrados", TipoEstado.EXITO);
        });

        task.setOnFailed(e -> mostrarEstado("Error al registrar: " + task.getException().getMessage(), TipoEstado.ERROR));

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void cargarResumen() {
        String fecha = fechaPicker.getValue().toString();

        Task<HidratacionDto> task = new Task<>() {
            @Override
            protected HidratacionDto call() throws Exception {
                return apiClient.obtenerResumen(SessionManager.getUsuarioId(), fecha);
            }
        };

        task.setOnSucceeded(e -> {
            HidratacionDto dto = task.getValue();
            Platform.runLater(() -> {
                totalLabel.setText(String.format("%.0f ml", dto.getTotalMl()));
                objetivoLabel.setText(String.format("/ %.0f ml", dto.getObjetivoMl()));
                porcentajeLabel.setText(dto.getPorcentaje() + "%");

                double progreso = Math.min(1.0, dto.getTotalMl() / Math.max(1, dto.getObjetivoMl()));
                progresoBar.setProgress(progreso);

                String colorProgreso = progreso >= 1.0 ? "#86efac" : progreso >= 0.5 ? "#fcd34d" : "#93c5fd";
                porcentajeLabel.setStyle(STYLE_TEXT_FILL + colorProgreso + "; -fx-font-size: 22px; -fx-font-weight: bold;");

                mostrarEstado("Datos de hidratación cargados", TipoEstado.EXITO);
            });
        });

        task.setOnFailed(e -> mostrarEstado("Error al cargar: " + task.getException().getMessage(), TipoEstado.ERROR));

        mostrarEstado("Cargando...", TipoEstado.INFO);
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
        Platform.runLater(() -> {
            statusLabel.setText(mensaje);
            statusLabel.setStyle(
                    "-fx-background-color: #020617; " +
                    STYLE_TEXT_FILL + color + "; " +
                    "-fx-padding: 12; " +
                    "-fx-font-size: 13px;"
            );
        });
    }

    private enum TipoEstado { INFO, EXITO, ERROR }
}
