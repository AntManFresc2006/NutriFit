package com.nutrifit.client.controller;

import com.nutrifit.client.NutriFitClientApplication;
import com.nutrifit.client.model.PerfilDto;
import com.nutrifit.client.service.PerfilApiClient;
import com.nutrifit.client.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador de la pantalla de perfil del usuario.
 * Permite ver y actualizar los datos biométricos, y muestra TMB y TDEE calculados.
 */
public class PerfilController {

    @FXML private ComboBox<String> sexoCombo;
    @FXML private DatePicker fechaNacimientoPicker;
    @FXML private TextField alturaField;
    @FXML private TextField pesoField;
    @FXML private TextField pesoObjetivoField;
    @FXML private ComboBox<String> nivelActividadCombo;
    @FXML private Label tmbLabel;
    @FXML private Label tdeeLabel;
    @FXML private Label statusLabel;
    @FXML private Button guardarButton;

    private final PerfilApiClient perfilApiClient = new PerfilApiClient();

    @FXML
    public void initialize() {
        sexoCombo.setItems(FXCollections.observableArrayList("H", "M"));
        nivelActividadCombo.setItems(FXCollections.observableArrayList(
                "SEDENTARIO", "LIGERO", "MODERADO", "ALTO", "MUY_ALTO"
        ));
        cargarPerfil();
    }

    private void cargarPerfil() {
        Task<PerfilDto> task = new Task<>() {
            @Override
            protected PerfilDto call() throws Exception {
                return perfilApiClient.getPerfil(SessionManager.getUsuarioId());
            }
        };

        task.setOnSucceeded(event -> {
            poblarFormulario(task.getValue());
            mostrarEstado("Perfil cargado correctamente", true);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error al cargar el perfil: " + (error != null ? error.getMessage() : "Error desconocido"), false);
        });

        mostrarEstado("Cargando perfil...", true);
        ejecutar(task);
    }

    @FXML
    private void onGuardar() {
        String errorValidacion = validar();
        if (errorValidacion != null) {
            mostrarEstado(errorValidacion, false);
            return;
        }

        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("sexo", sexoCombo.getValue());
        datos.put("fechaNacimiento", fechaNacimientoPicker.getValue().toString());
        datos.put("alturaCm", Integer.parseInt(alturaField.getText().trim()));
        datos.put("pesoKgActual", Double.parseDouble(pesoField.getText().trim()));

        String pesoObj = pesoObjetivoField.getText().trim();
        datos.put("pesoObjetivo", pesoObj.isEmpty() ? null : Double.parseDouble(pesoObj));
        datos.put("nivelActividad", nivelActividadCombo.getValue());

        Task<PerfilDto> task = new Task<>() {
            @Override
            protected PerfilDto call() throws Exception {
                return perfilApiClient.updatePerfil(SessionManager.getUsuarioId(), datos);
            }
        };

        task.setOnSucceeded(event -> {
            PerfilDto actualizado = task.getValue();
            poblarFormulario(actualizado);
            SessionManager.setTdee(actualizado.getTdee());
            mostrarEstado("Perfil actualizado correctamente", true);
            guardarButton.setDisable(false);
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error al guardar: " + (error != null ? error.getMessage() : "Error desconocido"), false);
            guardarButton.setDisable(false);
        });

        guardarButton.setDisable(true);
        mostrarEstado("Guardando cambios...", true);
        ejecutar(task);
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

    private void poblarFormulario(PerfilDto perfil) {
        sexoCombo.setValue(perfil.getSexo());
        fechaNacimientoPicker.setValue(LocalDate.parse(perfil.getFechaNacimiento()));
        alturaField.setText(String.valueOf(perfil.getAlturaCm()));
        pesoField.setText(String.valueOf(perfil.getPesoKgActual()));
        pesoObjetivoField.setText(perfil.getPesoObjetivo() != null ? String.valueOf(perfil.getPesoObjetivo()) : "");
        nivelActividadCombo.setValue(perfil.getNivelActividad());
        tmbLabel.setText(String.valueOf(perfil.getTmb()));
        tdeeLabel.setText(String.valueOf(perfil.getTdee()));
    }

    private String validar() {
        if (sexoCombo.getValue() == null) {
            return "Selecciona el sexo";
        }
        if (fechaNacimientoPicker.getValue() == null || fechaNacimientoPicker.getValue().isAfter(LocalDate.now())) {
            return "Introduce una fecha de nacimiento válida";
        }
        try {
            int altura = Integer.parseInt(alturaField.getText().trim());
            if (altura < 100 || altura > 250) return "La altura debe estar entre 100 y 250 cm";
        } catch (NumberFormatException e) {
            return "La altura debe ser un número entero";
        }
        try {
            double peso = Double.parseDouble(pesoField.getText().trim());
            if (peso < 20) return "El peso debe ser mayor que 20 kg";
        } catch (NumberFormatException e) {
            return "El peso debe ser un número válido";
        }
        String pesoObj = pesoObjetivoField.getText().trim();
        if (!pesoObj.isEmpty()) {
            try {
                Double.parseDouble(pesoObj);
            } catch (NumberFormatException e) {
                return "El peso objetivo debe ser un número válido";
            }
        }
        if (nivelActividadCombo.getValue() == null) {
            return "Selecciona el nivel de actividad";
        }
        return null;
    }

    private void ejecutar(Task<?> task) {
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
}
