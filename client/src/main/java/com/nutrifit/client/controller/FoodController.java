package com.nutrifit.client.controller;

import com.nutrifit.client.model.AlimentoFx;
import com.nutrifit.client.service.AlimentoApiClient;
import javafx.fxml.FXML;
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
     * Carga los alimentos desde la API y actualiza la tabla.
     */
    private void cargarAlimentos() {
        try {
            foodTable.getItems().setAll(apiClient.getAll());
            statusLabel.setText("Alimentos cargados correctamente");
        } catch (Exception e) {
            statusLabel.setText("Error al cargar alimentos: " + e.getMessage());
        }
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

    @FXML
    private void onBuscar() {
        try {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                cargarAlimentos();
            } else {
                foodTable.getItems().setAll(apiClient.search(query));
                statusLabel.setText("Búsqueda completada");
            }
        } catch (Exception e) {
            statusLabel.setText("Error al buscar: " + e.getMessage());
        }
    }

    @FXML
    private void onNuevo() {
        foodTable.getSelectionModel().clearSelection();
        alimentoSeleccionado = null;
        limpiarFormulario();
        statusLabel.setText("Formulario listo para nuevo alimento");
    }

    @FXML
    private void onGuardar() {
        try {
            AlimentoFx alimento = leerFormulario();

            if (alimentoSeleccionado == null) {
                apiClient.create(alimento);
                statusLabel.setText("Alimento creado correctamente");
            } else {
                alimento.setId(alimentoSeleccionado.getId());
                apiClient.update(alimento);
                statusLabel.setText("Alimento actualizado correctamente");
            }

            cargarAlimentos();
            limpiarFormulario();
            alimentoSeleccionado = null;
            foodTable.getSelectionModel().clearSelection();
        } catch (NumberFormatException e) {
            statusLabel.setText("Revisa los campos numéricos del formulario");
        } catch (Exception e) {
            statusLabel.setText("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        try {
            if (alimentoSeleccionado == null) {
                statusLabel.setText("Selecciona un alimento para eliminar");
                return;
            }

            apiClient.delete(alimentoSeleccionado.getId());
            cargarAlimentos();
            limpiarFormulario();
            statusLabel.setText("Alimento eliminado correctamente");
            alimentoSeleccionado = null;
            foodTable.getSelectionModel().clearSelection();
        } catch (Exception e) {
            statusLabel.setText("Error al eliminar: " + e.getMessage());
        }
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