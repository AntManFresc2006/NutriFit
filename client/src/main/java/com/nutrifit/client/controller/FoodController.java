package com.nutrifit.client.controller;

import com.nutrifit.client.model.AlimentoFx;
import com.nutrifit.client.service.AlimentoApiClient;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

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
    private Button guardarButton;

    @FXML
    private Button eliminarButton;

    private final AlimentoApiClient apiClient = new AlimentoApiClient();
    private AlimentoFx alimentoSeleccionado;

    /**
     * Configura columnas, carga datos iniciales y prepara la selección de filas.
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

        foodTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            alimentoSeleccionado = newValue;
            if (newValue != null) {
                cargarFormularioDesdeSeleccion(newValue);
                statusLabel.setText("Alimento seleccionado: " + newValue.getNombre());
            }
        });

        cargarAlimentos();
    }

    /**
     * Ejecuta una tarea en segundo plano para evitar bloquear la interfaz.
     */
    private void ejecutarEnSegundoPlano(Task<?> task, String mensajeCargando) {
    setControlesDeshabilitados(true);
    statusLabel.setText(mensajeCargando);

    task.stateProperty().addListener((obs, oldState, newState) -> {
        switch (newState) {
            case SUCCEEDED, FAILED, CANCELLED -> setControlesDeshabilitados(false);
        }
    });

    task.setOnFailed(event -> {
        Throwable error = task.getException();
        statusLabel.setText("Error: " + (error != null ? error.getMessage() : "Error desconocido"));
    });

    Thread hilo = new Thread(task);
    hilo.setDaemon(true);
    hilo.start();
}

    /**
     * Habilita o deshabilita los controles principales de la pantalla mientras se
     * ejecuta una tarea.
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
        eliminarButton.setDisable(deshabilitados);
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
            statusLabel.setText("Alimentos cargados correctamente");
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
     *
     * @return mensaje de error si algo no es válido, o null si todo está correcto
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
            statusLabel.setText("Búsqueda completada");
        });

        ejecutarEnSegundoPlano(task, "Buscando alimentos...");
    }

    @FXML
    private void onNuevo() {
        foodTable.getSelectionModel().clearSelection();
        alimentoSeleccionado = null;
        limpiarFormulario();
        nombreField.requestFocus();
        statusLabel.setText("Formulario listo para nuevo alimento");
    }

    @FXML
    private void onGuardar() {
        String errorValidacion = validarFormulario();
        if (errorValidacion != null) {
            statusLabel.setText(errorValidacion);
            return;
        }

        AlimentoFx alimento;
        try {
            alimento = leerFormulario();
        } catch (Exception e) {
            statusLabel.setText("Error al leer el formulario");
            return;
        }

        final boolean esNuevo = (alimentoSeleccionado == null);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (alimentoSeleccionado == null) {
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
            statusLabel.setText(esNuevo
                    ? "Alimento creado correctamente"
                    : "Alimento actualizado correctamente");
        });

        String mensaje = alimentoSeleccionado == null ? "Guardando nuevo alimento..." : "Actualizando alimento...";
        ejecutarEnSegundoPlano(task, mensaje);
    }

    @FXML
    private void onEliminar() {
        if (alimentoSeleccionado == null) {
            statusLabel.setText("Selecciona un alimento para eliminar");
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
            statusLabel.setText("Alimento eliminado correctamente: " + nombre);
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
}