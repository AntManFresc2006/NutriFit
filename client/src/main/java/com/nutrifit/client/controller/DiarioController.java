package com.nutrifit.client.controller;

import com.nutrifit.client.NutriFitClientApplication;
import com.nutrifit.client.model.EvaluacionIaRequest;
import com.nutrifit.client.model.ResumenDiarioDto;
import com.nutrifit.client.service.ResumenDiarioApiClient;
import com.nutrifit.client.session.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDate;

public class DiarioController {

    @FXML private DatePicker fechaPicker;
    @FXML private Label kcalLabel;
    @FXML private Label proteinasLabel;
    @FXML private Label grasasLabel;
    @FXML private Label carbosLabel;
    @FXML private Label kcalQuemadasLabel;
    @FXML private Label tdeeLabel;
    @FXML private Label balanceRealLabel;
    @FXML private Label estadoBalanceLabel;
    @FXML private TextArea iaTextArea;
    @FXML private Label statusLabel;

    private static final String FMT_KCAL = "%.0f kcal";
    private static final String FMT_GRAMOS = "%.1f g";
    private static final String STYLE_TEXT_FILL = "-fx-text-fill: ";

    private final ResumenDiarioApiClient resumenDiarioApiClient = new ResumenDiarioApiClient();
    private ResumenDiarioDto currentResumen = null;

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
            currentResumen = resumen;

            kcalLabel.setText(String.format(FMT_KCAL, resumen.getKcalTotales()));
            proteinasLabel.setText(String.format(FMT_GRAMOS, resumen.getProteinasTotales()));
            grasasLabel.setText(String.format(FMT_GRAMOS, resumen.getGrasasTotales()));
            carbosLabel.setText(String.format(FMT_GRAMOS, resumen.getCarbosTotales()));
            kcalQuemadasLabel.setText(String.format(FMT_KCAL, resumen.getKcalQuemadasTotales()));
            tdeeLabel.setText(String.format(FMT_KCAL, resumen.getTdee()));

            double balanceReal = resumen.getBalanceReal();
            String estado = resumen.getEstadoBalance() != null ? resumen.getEstadoBalance() : "MANTENIMIENTO";
            balanceRealLabel.setText(String.format("%+.0f kcal", balanceReal));
            estadoBalanceLabel.setText(estado);

            String colorBalance = switch (estado) {
                case "SUPERAVIT" -> "#86efac";
                case "DEFICIT"   -> "#fca5a5";
                default          -> "#94a3b8";
            };
            String styleBalance = STYLE_TEXT_FILL + colorBalance + "; -fx-font-size: 18px; -fx-font-weight: bold;";
            balanceRealLabel.setStyle(styleBalance);
            estadoBalanceLabel.setStyle(styleBalance);

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

    @FXML
    private void onEvaluarConIa() {
        if (currentResumen == null) {
            mostrarEstado("Carga el resumen antes de evaluar", TipoEstado.ERROR);
            return;
        }

        EvaluacionIaRequest request = new EvaluacionIaRequest();
        request.setUsuarioId(SessionManager.getUsuarioId());
        request.setFecha(fechaPicker.getValue().toString());
        request.setKcalConsumidas(currentResumen.getKcalTotales());
        request.setKcalQuemadas(currentResumen.getKcalQuemadasTotales());
        request.setProteinasTotales(currentResumen.getProteinasTotales());
        request.setGrasasTotales(currentResumen.getGrasasTotales());
        request.setCarbosTotales(currentResumen.getCarbosTotales());
        request.setTdee(currentResumen.getTdee());
        request.setBalanceReal(currentResumen.getBalanceReal());

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return resumenDiarioApiClient.evaluarConIa(request);
            }
        };

        task.setOnSucceeded(event -> {
            iaTextArea.setText(task.getValue());
            mostrarEstado("Evaluación generada", TipoEstado.EXITO);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error IA: " + (error != null ? error.getMessage() : "Error desconocido"), TipoEstado.ERROR);
        });

        iaTextArea.setText("Analizando tu día...");
        mostrarEstado("Consultando IA...", TipoEstado.INFO);
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
                STYLE_TEXT_FILL + color + "; " +
                "-fx-padding: 12; " +
                "-fx-font-size: 13px;"
        );
    }

    private enum TipoEstado { INFO, EXITO, ERROR }
}
