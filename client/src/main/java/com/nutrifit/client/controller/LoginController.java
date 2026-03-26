package com.nutrifit.client.controller;

import com.nutrifit.client.NutriFitClientApplication;
import com.nutrifit.client.service.AuthApiClient;
import com.nutrifit.client.service.PerfilApiClient;
import com.nutrifit.client.session.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Controlador de la pantalla de login y registro.
 */
public class LoginController {

    @FXML
    private TextField loginEmailField;

    @FXML
    private PasswordField loginPasswordField;

    @FXML
    private TextField registerNombreField;

    @FXML
    private TextField registerEmailField;

    @FXML
    private PasswordField registerPasswordField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private final AuthApiClient authApiClient = new AuthApiClient();
    private final PerfilApiClient perfilApiClient = new PerfilApiClient();

    @FXML
    public void initialize() {
        loginEmailField.setOnAction(event -> onLogin());
        loginPasswordField.setOnAction(event -> onLogin());
        registerNombreField.setOnAction(event -> onRegister());
        registerEmailField.setOnAction(event -> onRegister());
        registerPasswordField.setOnAction(event -> onRegister());
    }

    @FXML
    private void onLogin() {
        String email = loginEmailField.getText().trim();
        String password = loginPasswordField.getText().trim();

        if (email.isEmpty()) {
            mostrarEstado("El email de login es obligatorio", false);
            loginEmailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            mostrarEstado("La contraseña de login es obligatoria", false);
            loginPasswordField.requestFocus();
            return;
        }

        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                Map<String, Object> resp = authApiClient.login(email, password);
                cargarTdeeEnSesion(resp);
                return resp;
            }
        };

        task.setOnSucceeded(event -> {
            Map<String, Object> response = task.getValue();

            Long usuarioId = ((Number) response.get("usuarioId")).longValue();
            String nombre = (String) response.get("nombre");
            String emailResp = (String) response.get("email");
            String token = (String) response.get("token");

            SessionManager.setSession(usuarioId, nombre, emailResp, token);
            mostrarEstado("Login correcto. Bienvenido, " + nombre, true);
            abrirPantallaAlimentos();
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error al iniciar sesión: " + (error != null ? error.getMessage() : "Error desconocido"), false);
            setBotonesDeshabilitados(false);
        });

        setBotonesDeshabilitados(true);
        mostrarEstado("Iniciando sesión...", true);
        ejecutarTask(task);
    }

    @FXML
    private void onRegister() {
        String nombre = registerNombreField.getText().trim();
        String email = registerEmailField.getText().trim();
        String password = registerPasswordField.getText().trim();

        if (nombre.isEmpty()) {
            mostrarEstado("El nombre de registro es obligatorio", false);
            registerNombreField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            mostrarEstado("El email de registro es obligatorio", false);
            registerEmailField.requestFocus();
            return;
        }

        if (password.length() < 6) {
            mostrarEstado("La contraseña debe tener al menos 6 caracteres", false);
            registerPasswordField.requestFocus();
            return;
        }

        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                Map<String, Object> resp = authApiClient.register(nombre, email, password);
                cargarTdeeEnSesion(resp);
                return resp;
            }
        };

        task.setOnSucceeded(event -> {
            Map<String, Object> response = task.getValue();

            Long usuarioId = ((Number) response.get("usuarioId")).longValue();
            String nombreResp = (String) response.get("nombre");
            String emailResp = (String) response.get("email");
            String token = (String) response.get("token");

            SessionManager.setSession(usuarioId, nombreResp, emailResp, token);
            mostrarEstado("Registro correcto. Bienvenido, " + nombreResp, true);
            abrirPantallaAlimentos();
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado("Error al registrar: " + (error != null ? error.getMessage() : "Error desconocido"), false);
            setBotonesDeshabilitados(false);
        });

        setBotonesDeshabilitados(true);
        mostrarEstado("Registrando usuario...", true);
        ejecutarTask(task);
    }

    /**
     * Llama a GET /api/perfil/{id} y almacena el TDEE en SessionManager.
     * Se ejecuta en el hilo de la tarea — un fallo no bloquea el login.
     */
    private void cargarTdeeEnSesion(Map<String, Object> loginResp) {
        try {
            Long uid = ((Number) loginResp.get("usuarioId")).longValue();
            double tdee = perfilApiClient.getPerfil(uid).getTdee();
            SessionManager.setTdee(tdee);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            // El TDEE se actualizará cuando el usuario visite la pantalla de perfil
        }
    }

    private void ejecutarTask(Task<?> task) {
        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void setBotonesDeshabilitados(boolean deshabilitados) {
        loginButton.setDisable(deshabilitados);
        registerButton.setDisable(deshabilitados);
    }

    private void mostrarEstado(String mensaje, boolean exitoInfo) {
        String color = exitoInfo ? "#93c5fd" : "#fca5a5";
        statusLabel.setText(mensaje);
        statusLabel.setStyle(
                "-fx-background-color: #020617; " +
                "-fx-text-fill: " + color + "; " +
                "-fx-padding: 12; " +
                "-fx-font-size: 13px;"
        );
    }

    private void abrirPantallaAlimentos() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NutriFitClientApplication.class.getResource("/com/nutrifit/client/food-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 1100, 650);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("NutriFit - Gestión de alimentos");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            mostrarEstado("No se pudo abrir la pantalla principal: " + e.getMessage(), false);
            setBotonesDeshabilitados(false);
        }
    }
}
