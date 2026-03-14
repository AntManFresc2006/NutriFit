package com.nutrifit.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class FoodController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<?> foodTable;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("Pantalla de alimentos lista");
    }

    @FXML
    private void onBuscar() {
        statusLabel.setText("Buscar todavía no conectado");
    }

    @FXML
    private void onNuevo() {
        statusLabel.setText("Formulario listo para nuevo alimento");
    }

    @FXML
    private void onGuardar() {
        statusLabel.setText("Guardar todavía no conectado");
    }

    @FXML
    private void onEliminar() {
        statusLabel.setText("Eliminar todavía no conectado");
    }

    @FXML
    private void onRecargar() {
        statusLabel.setText("Recargar todavía no conectado");
    }
}