package com.nutrifit.client.controller;

import com.nutrifit.client.NutriFitClientApplication;
import com.nutrifit.client.model.AlimentoFx;
import com.nutrifit.client.service.AlimentoApiClient;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Controlador de la vista principal de gestión de alimentos en JavaFX.
 * Gestiona la tabla, el formulario y la comunicación con la API REST.
 */
public class FoodController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<AlimentoFx> foodTable;

    @FXML
    private TableColumn<AlimentoFx, Long> idColumn;

    @FXML
    private TableColumn<AlimentoFx, String> nombreColumn;

    @FXML
    private TableColumn<AlimentoFx, Double> porcionColumn;

    @FXML
    private TableColumn<AlimentoFx, Double> kcalColumn;

    @FXML
    private TableColumn<AlimentoFx, Double> proteinasColumn;

    @FXML
    private TableColumn<AlimentoFx, Double> grasasColumn;

    @FXML
    private TableColumn<AlimentoFx, Double> carbosColumn;

    @FXML
    private TableColumn<AlimentoFx, String> fuenteColumn;

    @FXML
    private TextField nombreField;

    @FXML
    private TextField porcionField;

    @FXML
    private TextField kcalField;

    @FXML
    private TextField proteinasField;

    @FXML
    private TextField grasasField;

    @FXML
    private TextField carbosField;

    @FXML
    private TextField fuenteField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button buscarButton;

    @FXML
    private Button recargarButton;

    @FXML
    private Button nuevoButton;

    @FXML
    private Button diarioButton;

    @FXML
    private Button perfilButton;

    @FXML
    private Button guardarButton;

    @FXML
    private Button eliminarButton;

    private final AlimentoApiClient apiClient = new AlimentoApiClient();
    private AlimentoFx alimentoSeleccionado;

    /**
     * Configura columnas, eventos y estado inicial de la pantalla.
     */
    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        porcionColumn.setCellValueFactory(new PropertyValueFactory<>("porcionG"));
        kcalColumn.setCellValueFactory(new PropertyValueFactory<>("kcalPor100g"));
        proteinasColumn.setCellValueFactory(new PropertyValueFactory<>("proteinasG"));
        grasasColumn.setCellValueFactory(new PropertyValueFactory<>("grasasG"));
        carbosColumn.setCellValueFactory(new PropertyValueFactory<>("carbosG"));
        fuenteColumn.setCellValueFactory(new PropertyValueFactory<>("fuente"));

        foodTable.setPlaceholder(new Label("No hay alimentos para mostrar"));

        foodTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            alimentoSeleccionado = newValue;
            if (newValue != null) {
                cargarFormularioDesdeSeleccion(newValue);
                mostrarEstado("Alimento seleccionado: " + newValue.getNombre(), TipoEstado.INFO);
            }
            actualizarModoFormulario();
        });

        // Enter en buscador = buscar
        searchField.setOnAction(event -> onBuscar());

        // Enter en los campos del formulario = guardar
        nombreField.setOnAction(event -> onGuardar());
        porcionField.setOnAction(event -> onGuardar());
        kcalField.setOnAction(event -> onGuardar());
        proteinasField.setOnAction(event -> onGuardar());
        grasasField.setOnAction(event -> onGuardar());
        carbosField.setOnAction(event -> onGuardar());
        fuenteField.setOnAction(event -> onGuardar());

        actualizarModoFormulario();
        cargarAlimentos();
    }

    /**
     * Ejecuta una tarea en segundo plano para evitar bloquear la interfaz.
     */
    private void ejecutarEnSegundoPlano(Task<?> task, String mensajeCargando) {
        setControlesDeshabilitados(true);
        mostrarEstado(mensajeCargando, TipoEstado.INFO);

        task.stateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case SUCCEEDED, FAILED, CANCELLED -> setControlesDeshabilitados(false);
            }
        });

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

    /**
     * Habilita o deshabilita los controles principales mientras se ejecuta una tarea.
     */
    private void setControlesDeshabilitados(boolean deshabilitados) {
        searchField.setDisable(deshabilitados);
        foodTable.setDisable(deshabilitados);
        nombreField.setDisable(deshabilitados);
        porcionField.setDisable(deshabilitados);
        kcalField.setDisable(deshabilitados);
        proteinasField.setDisable(deshabilitados);
        grasasField.setDisable(deshabilitados);
        carbosField.setDisable(deshabilitados);
        fuenteField.setDisable(deshabilitados);

        buscarButton.setDisable(deshabilitados);
        recargarButton.setDisable(deshabilitados);
        nuevoButton.setDisable(deshabilitados);
        guardarButton.setDisable(deshabilitados);
        eliminarButton.setDisable(deshabilitados || alimentoSeleccionado == null);
    }

    /**
     * Ajusta el modo visual del formulario según haya o no un alimento seleccionado.
     */
    private void actualizarModoFormulario() {
        boolean haySeleccion = alimentoSeleccionado != null;
        guardarButton.setText(haySeleccion ? "Actualizar" : "Crear");
        eliminarButton.setDisable(!haySeleccion);
    }

    /**
     * Muestra mensajes de estado con color según el tipo.
     */
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

    /**
     * Carga los alimentos desde la API y actualiza la tabla.
     */
    private void cargarAlimentos() {
        Task<java.util.List<AlimentoFx>> task = new Task<>() {
            @Override
            protected java.util.List<AlimentoFx> call() throws Exception {
                return apiClient.getAll();
            }
        };

        task.setOnSucceeded(event -> {
            foodTable.getItems().setAll(task.getValue());
            mostrarEstado("Alimentos cargados correctamente", TipoEstado.EXITO);
        });

        ejecutarEnSegundoPlano(task, "Cargando alimentos...");
    }

    /**
     * Copia los datos del alimento seleccionado al formulario.
     */
    private void cargarFormularioDesdeSeleccion(AlimentoFx alimento) {
        nombreField.setText(alimento.getNombre());
        porcionField.setText(String.valueOf(alimento.getPorcionG()));
        kcalField.setText(String.valueOf(alimento.getKcalPor100g()));
        proteinasField.setText(String.valueOf(alimento.getProteinasG()));
        grasasField.setText(String.valueOf(alimento.getGrasasG()));
        carbosField.setText(String.valueOf(alimento.getCarbosG()));
        fuenteField.setText(alimento.getFuente());
    }

    /**
     * Construye un objeto AlimentoFx a partir de los campos del formulario.
     */
    private AlimentoFx leerFormulario() {
        AlimentoFx alimento = new AlimentoFx();
        alimento.setNombre(nombreField.getText().trim());
        alimento.setPorcionG(Double.parseDouble(porcionField.getText().trim()));
        alimento.setKcalPor100g(Double.parseDouble(kcalField.getText().trim()));
        alimento.setProteinasG(Double.parseDouble(proteinasField.getText().trim()));
        alimento.setGrasasG(Double.parseDouble(grasasField.getText().trim()));
        alimento.setCarbosG(Double.parseDouble(carbosField.getText().trim()));
        alimento.setFuente(fuenteField.getText().trim());
        return alimento;
    }

    /**
     * Valida los campos del formulario antes de enviar datos al backend.
     */
    private String validarFormulario() {
        if (nombreField.getText() == null || nombreField.getText().trim().isEmpty()) {
            nombreField.requestFocus();
            return "El nombre es obligatorio";
        }

        try {
            double porcion = Double.parseDouble(porcionField.getText().trim());
            if (porcion <= 0) {
                porcionField.requestFocus();
                return "La porción debe ser mayor que 0";
            }
        } catch (NumberFormatException e) {
            porcionField.requestFocus();
            return "La porción debe ser un número válido";
        }

        try {
            double kcal = Double.parseDouble(kcalField.getText().trim());
            if (kcal < 0) {
                kcalField.requestFocus();
                return "Las kcal no pueden ser negativas";
            }
        } catch (NumberFormatException e) {
            kcalField.requestFocus();
            return "Las kcal deben ser un número válido";
        }

        try {
            double proteinas = Double.parseDouble(proteinasField.getText().trim());
            if (proteinas < 0) {
                proteinasField.requestFocus();
                return "Las proteínas no pueden ser negativas";
            }
        } catch (NumberFormatException e) {
            proteinasField.requestFocus();
            return "Las proteínas deben ser un número válido";
        }

        try {
            double grasas = Double.parseDouble(grasasField.getText().trim());
            if (grasas < 0) {
                grasasField.requestFocus();
                return "Las grasas no pueden ser negativas";
            }
        } catch (NumberFormatException e) {
            grasasField.requestFocus();
            return "Las grasas deben ser un número válido";
        }

        try {
            double carbos = Double.parseDouble(carbosField.getText().trim());
            if (carbos < 0) {
                carbosField.requestFocus();
                return "Los carbohidratos no pueden ser negativos";
            }
        } catch (NumberFormatException e) {
            carbosField.requestFocus();
            return "Los carbohidratos deben ser un número válido";
        }

        return null;
    }

    @FXML
    private void onBuscar() {
        String query = searchField.getText().trim();

        if (query.isEmpty()) {
            cargarAlimentos();
            return;
        }

        Task<java.util.List<AlimentoFx>> task = new Task<>() {
            @Override
            protected java.util.List<AlimentoFx> call() throws Exception {
                return apiClient.search(query);
            }
        };

        task.setOnSucceeded(event -> {
            foodTable.getItems().setAll(task.getValue());
            mostrarEstado("Búsqueda completada", TipoEstado.EXITO);
        });

        ejecutarEnSegundoPlano(task, "Buscando alimentos...");
    }

    @FXML
    private void onAbrirPerfil() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NutriFitClientApplication.class.getResource("/com/nutrifit/client/perfil-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 700, 600);

            Stage stage = (Stage) foodTable.getScene().getWindow();
            stage.setTitle("NutriFit - Mi perfil");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            mostrarEstado("No se pudo abrir la pantalla de perfil: " + e.getMessage(), TipoEstado.ERROR);
        }
    }

    @FXML
private void onAbrirDiario() {
    try {
        FXMLLoader loader = new FXMLLoader(
                NutriFitClientApplication.class.getResource("/com/nutrifit/client/diario-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 1000, 620);

        Stage stage = (Stage) foodTable.getScene().getWindow();
        stage.setTitle("NutriFit - Resumen diario");
        stage.setScene(scene);
        stage.show();
    } catch (Exception e) {
        mostrarEstado("No se pudo abrir la pantalla de diario: " + e.getMessage(), TipoEstado.ERROR);
    }
}


    @FXML
    private void onNuevo() {
        foodTable.getSelectionModel().clearSelection();
        alimentoSeleccionado = null;
        limpiarFormulario();
        actualizarModoFormulario();
        nombreField.requestFocus();
        mostrarEstado("Formulario listo para nuevo alimento", TipoEstado.INFO);
    }

    @FXML
    private void onGuardar() {
        String errorValidacion = validarFormulario();
        if (errorValidacion != null) {
            mostrarEstado(errorValidacion, TipoEstado.ERROR);
            return;
        }

        AlimentoFx alimento;
        try {
            alimento = leerFormulario();
        } catch (Exception e) {
            mostrarEstado("Error al leer el formulario", TipoEstado.ERROR);
            return;
        }

        final boolean esNuevo = (alimentoSeleccionado == null);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (esNuevo) {
                    apiClient.create(alimento);
                } else {
                    alimento.setId(alimentoSeleccionado.getId());
                    apiClient.update(alimento);
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            cargarAlimentos();
            limpiarFormulario();
            alimentoSeleccionado = null;
            foodTable.getSelectionModel().clearSelection();
            actualizarModoFormulario();
            mostrarEstado(
                    esNuevo ? "Alimento creado correctamente" : "Alimento actualizado correctamente",
                    TipoEstado.EXITO
            );
        });

        ejecutarEnSegundoPlano(task, esNuevo ? "Creando alimento..." : "Actualizando alimento...");
    }

    @FXML
    private void onEliminar() {
        if (alimentoSeleccionado == null) {
            mostrarEstado("Selecciona un alimento para eliminar", TipoEstado.ERROR);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("Eliminar alimento");
        alert.setContentText("¿Seguro que quieres eliminar \"" + alimentoSeleccionado.getNombre() + "\"?");

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            mostrarEstado("Eliminación cancelada", TipoEstado.INFO);
            return;
        }

        long id = alimentoSeleccionado.getId();
        String nombre = alimentoSeleccionado.getNombre();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.delete(id);
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            cargarAlimentos();
            limpiarFormulario();
            alimentoSeleccionado = null;
            foodTable.getSelectionModel().clearSelection();
            actualizarModoFormulario();
            mostrarEstado("Alimento eliminado correctamente: " + nombre, TipoEstado.EXITO);
        });

        ejecutarEnSegundoPlano(task, "Eliminando alimento...");
    }

    @FXML
    private void onRecargar() {
        cargarAlimentos();
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarFormulario() {
        nombreField.clear();
        porcionField.clear();
        kcalField.clear();
        proteinasField.clear();
        grasasField.clear();
        carbosField.clear();
        fuenteField.clear();
    }

    private enum TipoEstado {
        INFO,
        EXITO,
        ERROR
    }
}