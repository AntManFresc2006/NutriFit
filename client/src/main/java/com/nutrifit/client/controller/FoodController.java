package com.nutrifit.client.controller;

import com.nutrifit.client.model.AlimentoFx;
import com.nutrifit.client.service.AlimentoApiClient;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

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
    private Label statusLabel;

    private final AlimentoApiClient apiClient = new AlimentoApiClient();

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

    private void cargarAlimentos() {
        try {
            foodTable.getItems().setAll(apiClient.getAll());
            statusLabel.setText("Alimentos cargados correctamente");
        } catch (Exception e) {
            statusLabel.setText("Error al cargar alimentos: " + e.getMessage());
        }
    }

    @FXML
    private void onBuscar() {
        statusLabel.setText("Búsqueda aún no implementada");
    }

    @FXML
    private void onNuevo() {
        statusLabel.setText("Formulario listo para nuevo alimento");
    }

    @FXML
    private void onGuardar() {
        statusLabel.setText("Guardar aún no implementado");
    }

    @FXML
    private void onEliminar() {
        statusLabel.setText("Eliminar aún no implementado");
    }

    @FXML
    private void onRecargar() {
        cargarAlimentos();
    }
}