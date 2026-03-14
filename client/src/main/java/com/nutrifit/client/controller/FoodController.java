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

    /**
     * Método de inicialización automática de JavaFX.
     * Configura las columnas de la tabla y carga los datos iniciales desde la API.
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

        cargarAlimentos();
    }

    /**
     * Solicita la lista de alimentos al backend y actualiza la tabla.
     * Si ocurre un error, se refleja en la barra de estado inferior.
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
     * Acción asociada al botón Buscar.
     * Por ahora está preparada como punto de ampliación para búsquedas desde la UI.
     */
    @FXML
    private void onBuscar() {
        statusLabel.setText("Búsqueda aún no implementada");
    }

    /**
     * Limpia el formulario para preparar la creación de un nuevo alimento.
     */
    @FXML
    private void onNuevo() {
        limpiarFormulario();
        statusLabel.setText("Formulario listo para nuevo alimento");
    }

    /**
     * Lee los datos del formulario, crea un nuevo alimento en el backend
     * y recarga la tabla con la información actualizada.
     */
    @FXML
    private void onGuardar() {
        try {
            AlimentoFx alimento = new AlimentoFx();
            alimento.setNombre(nombreField.getText().trim());
            alimento.setPorcionG(Double.parseDouble(porcionField.getText().trim()));
            alimento.setKcalPor100g(Double.parseDouble(kcalField.getText().trim()));
            alimento.setProteinasG(Double.parseDouble(proteinasField.getText().trim()));
            alimento.setGrasasG(Double.parseDouble(grasasField.getText().trim()));
            alimento.setCarbosG(Double.parseDouble(carbosField.getText().trim()));
            alimento.setFuente(fuenteField.getText().trim());

            apiClient.create(alimento);
            cargarAlimentos();
            limpiarFormulario();
            statusLabel.setText("Alimento creado correctamente");
        } catch (NumberFormatException e) {
            statusLabel.setText("Revisa los campos numéricos del formulario");
        } catch (Exception e) {
            statusLabel.setText("Error al guardar: " + e.getMessage());
        }
    }

    /**
     * Acción asociada al botón Eliminar.
     * Queda preparada para implementar la eliminación desde la tabla.
     */
    @FXML
    private void onEliminar() {
        statusLabel.setText("Eliminar aún no implementado");
    }

    /**
     * Vuelve a cargar manualmente la lista de alimentos desde la API.
     */
    @FXML
    private void onRecargar() {
        cargarAlimentos();
    }

    /**
     * Limpia todos los campos del formulario de alimentos.
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