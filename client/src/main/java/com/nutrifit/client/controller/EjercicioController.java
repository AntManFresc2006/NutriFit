package com.nutrifit.client.controller;

import com.nutrifit.client.NutriFitClientApplication;
import com.nutrifit.client.model.EjercicioDto;
import com.nutrifit.client.model.RegistroEjercicioDto;
import com.nutrifit.client.service.EjercicioApiClient;
import com.nutrifit.client.service.RegistroEjercicioApiClient;
import com.nutrifit.client.session.SessionManager;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de la vista de registro de ejercicios.
 * Permite cargar registros por fecha, registrar nuevos ejercicios y eliminarlos.
 */
public class EjercicioController {

    @FXML
    private DatePicker fechaPicker;

    @FXML
    private TableView<RegistroEjercicioDto> registrosTable;

    @FXML
    private TableColumn<RegistroEjercicioDto, String> colEjercicio;

    @FXML
    private TableColumn<RegistroEjercicioDto, Number> colDuracion;

    @FXML
    private TableColumn<RegistroEjercicioDto, Number> colKcal;

    @FXML
    private ComboBox<EjercicioDto> ejercicioComboBox;

    @FXML
    private TextField duracionField;

    @FXML
    private Button registrarButton;

    @FXML
    private Button eliminarButton;

    @FXML
    private Label statusLabel;

    private final EjercicioApiClient ejercicioApiClient = new EjercicioApiClient();
    private final RegistroEjercicioApiClient registroApiClient = new RegistroEjercicioApiClient();

    private RegistroEjercicioDto registroSeleccionado;

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @FXML
    public void initialize() {
        // Configurar columnas de la tabla con lambdas
        colEjercicio.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombreEjercicio()));
        colDuracion.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getDuracionMin()));
        colKcal.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getKcalQuemadas()));

        registrosTable.setPlaceholder(new Label("Sin ejercicios registrados para este día"));

        // Fecha por defecto: hoy
        fechaPicker.setValue(LocalDate.now());

        // Listener de selección en la tabla
        registrosTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            registroSeleccionado = nuevo;
            eliminarButton.setDisable(nuevo == null);
        });

        // Cargar catálogo de ejercicios en el ComboBox
        Task<List<EjercicioDto>> taskEjercicios = new Task<>() {
            @Override
            protected List<EjercicioDto> call() throws Exception {
                return ejercicioApiClient.getAll();
            }
        };

        taskEjercicios.setOnSucceeded(event -> {
            ejercicioComboBox.getItems().setAll(taskEjercicios.getValue());
            mostrarEstado("Catálogo de ejercicios cargado", TipoEstado.INFO);
        });

        ejecutarEnSegundoPlano(taskEjercicios, "Cargando catálogo de ejercicios...");

        // Estado inicial del botón eliminar
        eliminarButton.setDisable(true);

        cargarRegistros();
    }

    private void cargarRegistros() {
        String fecha = fechaPicker.getValue().format(FECHA_FMT);
        Long usuarioId = SessionManager.getUsuarioId();

        Task<List<RegistroEjercicioDto>> task = new Task<>() {
            @Override
            protected List<RegistroEjercicioDto> call() throws Exception {
                return registroApiClient.getByFecha(usuarioId, fecha);
            }
        };

        task.setOnSucceeded(event -> {
            registrosTable.getItems().setAll(task.getValue());
            registroSeleccionado = null;
            eliminarButton.setDisable(true);
            mostrarEstado("Ejercicios cargados para " + fecha, TipoEstado.EXITO);
        });

        ejecutarEnSegundoPlano(task, "Cargando ejercicios...");
    }

    @FXML
    private void onCargarFecha() {
        registrosTable.getItems().clear();
        registroSeleccionado = null;
        eliminarButton.setDisable(true);
        cargarRegistros();
    }

    @FXML
    private void onRegistrar() {
        EjercicioDto ejercicio = ejercicioComboBox.getValue();
        if (ejercicio == null) {
            mostrarEstado("Selecciona un ejercicio del catálogo", TipoEstado.ERROR);
            return;
        }

        String duracionTexto = duracionField.getText().trim();
        if (duracionTexto.isEmpty()) {
            mostrarEstado("Introduce la duración en minutos", TipoEstado.ERROR);
            duracionField.requestFocus();
            return;
        }

        int duracionMin;
        try {
            duracionMin = Integer.parseInt(duracionTexto);
            if (duracionMin <= 0) {
                mostrarEstado("La duración debe ser mayor que 0", TipoEstado.ERROR);
                duracionField.requestFocus();
                return;
            }
            if (duracionMin > 999) {
                mostrarEstado("La duración no puede superar 999 minutos", TipoEstado.ERROR);
                duracionField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            mostrarEstado("Introduce un valor numérico válido para la duración", TipoEstado.ERROR);
            duracionField.requestFocus();
            return;
        }

        Long usuarioId = SessionManager.getUsuarioId();
        Long ejercicioId = ejercicio.getId();
        String fecha = fechaPicker.getValue().format(FECHA_FMT);
        int duracionFinal = duracionMin;

        Task<RegistroEjercicioDto> task = new Task<>() {
            @Override
            protected RegistroEjercicioDto call() throws Exception {
                return registroApiClient.registrar(usuarioId, ejercicioId, fecha, duracionFinal);
            }
        };

        task.setOnSucceeded(event -> {
            cargarRegistros();
            duracionField.clear();
            mostrarEstado("Ejercicio registrado: " + ejercicio.getNombre() + " (" + duracionFinal + " min)", TipoEstado.EXITO);
        });

        ejecutarEnSegundoPlano(task, "Registrando ejercicio...");
    }

    @FXML
    private void onEliminar() {
        if (registroSeleccionado == null) {
            mostrarEstado("Selecciona un registro para eliminar", TipoEstado.ERROR);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("Eliminar registro de ejercicio");
        alert.setContentText("¿Eliminar el registro de \"" + registroSeleccionado.getNombreEjercicio() + "\"?");

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            mostrarEstado("Eliminación cancelada", TipoEstado.INFO);
            return;
        }

        Long usuarioId = SessionManager.getUsuarioId();
        Long registroId = registroSeleccionado.getId();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                registroApiClient.eliminar(usuarioId, registroId);
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            cargarRegistros();
            registroSeleccionado = null;
            mostrarEstado("Registro eliminado", TipoEstado.EXITO);
        });

        ejecutarEnSegundoPlano(task, "Eliminando registro...");
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

    private void ejecutarEnSegundoPlano(Task<?> task, String mensajeCargando) {
        mostrarEstado(mensajeCargando, TipoEstado.INFO);

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            mostrarEstado(
                    "Error: " + (error != null ? error.getMessage() : "Error desconocido"),
                    TipoEstado.ERROR
            );
        });

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarEstado(String mensaje, TipoEstado tipo) {
        String colorTexto;
        switch (tipo) {
            case EXITO -> colorTexto = "#86efac";
            case ERROR -> colorTexto = "#fca5a5";
            default -> colorTexto = "#93c5fd";
        }
        statusLabel.setText(mensaje);
        statusLabel.setStyle(
                "-fx-background-color: #020617; " +
                "-fx-text-fill: " + colorTexto + "; " +
                "-fx-padding: 12; " +
                "-fx-font-size: 13px;"
        );
    }

    private enum TipoEstado {
        INFO, EXITO, ERROR
    }
}
