package com.nutrifit.client.controller;

import com.nutrifit.client.NutriFitClientApplication;
import com.nutrifit.client.model.ListaItemDto;
import com.nutrifit.client.service.ListaCompraApiClient;
import com.nutrifit.client.session.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class ListaCompraController {

    @FXML private TextField nombreField;
    @FXML private TextField cantidadField;
    @FXML private ComboBox<String> categoriaCombo;
    @FXML private ListView<String> listaView;
    @FXML private Label statusLabel;
    @FXML private Label progresoLabel;

    private final ListaCompraApiClient apiClient = new ListaCompraApiClient();
    private List<ListaItemDto> itemsActuales;

    private static final String STYLE_TEXT_FILL = "-fx-text-fill: ";

    @FXML
    public void initialize() {
        categoriaCombo.setItems(FXCollections.observableArrayList(
                "OTROS", "PROTEINAS", "VERDURAS", "FRUTAS", "LACTEOS", "CEREALES", "BEBIDAS"
        ));
        categoriaCombo.setValue("OTROS");
        cargarLista();
    }

    @FXML
    private void onAnadir() {
        String nombre = nombreField.getText().trim();
        if (nombre.isEmpty()) {
            mostrarEstado("Introduce el nombre del producto", TipoEstado.ERROR);
            return;
        }

        String cantidad = cantidadField.getText().trim();
        String categoria = categoriaCombo.getValue();

        Task<ListaItemDto> task = new Task<>() {
            @Override
            protected ListaItemDto call() throws Exception {
                return apiClient.addItem(SessionManager.getUsuarioId(), nombre,
                        cantidad.isEmpty() ? null : cantidad, categoria);
            }
        };

        task.setOnSucceeded(e -> {
            nombreField.clear();
            cantidadField.clear();
            categoriaCombo.setValue("OTROS");
            cargarLista();
            mostrarEstado("Producto añadido", TipoEstado.EXITO);
        });

        task.setOnFailed(e -> mostrarEstado("Error al añadir: " + task.getException().getMessage(), TipoEstado.ERROR));

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void onToggle() {
        int idx = listaView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || itemsActuales == null || idx >= itemsActuales.size()) {
            mostrarEstado("Selecciona un producto de la lista", TipoEstado.ERROR);
            return;
        }

        ListaItemDto item = itemsActuales.get(idx);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.toggleItem(SessionManager.getUsuarioId(), item.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> cargarLista());
        task.setOnFailed(e -> mostrarEstado("Error al marcar: " + task.getException().getMessage(), TipoEstado.ERROR));

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void onEliminar() {
        int idx = listaView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || itemsActuales == null || idx >= itemsActuales.size()) {
            mostrarEstado("Selecciona un producto de la lista", TipoEstado.ERROR);
            return;
        }

        ListaItemDto item = itemsActuales.get(idx);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.eliminarItem(SessionManager.getUsuarioId(), item.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> { cargarLista(); mostrarEstado("Producto eliminado", TipoEstado.EXITO); });
        task.setOnFailed(e -> mostrarEstado("Error al eliminar: " + task.getException().getMessage(), TipoEstado.ERROR));

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    @FXML
    private void onLimpiarCompletados() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                apiClient.limpiarCompletados(SessionManager.getUsuarioId());
                return null;
            }
        };

        task.setOnSucceeded(e -> { cargarLista(); mostrarEstado("Completados eliminados", TipoEstado.EXITO); });
        task.setOnFailed(e -> mostrarEstado("Error: " + task.getException().getMessage(), TipoEstado.ERROR));

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void cargarLista() {
        Task<List<ListaItemDto>> task = new Task<>() {
            @Override
            protected List<ListaItemDto> call() throws Exception {
                return apiClient.obtenerLista(SessionManager.getUsuarioId());
            }
        };

        task.setOnSucceeded(e -> {
            itemsActuales = task.getValue();
            int total = itemsActuales.size();
            long completados = itemsActuales.stream().filter(ListaItemDto::isCompletado).count();

            List<String> textos = itemsActuales.stream()
                    .map(i -> (i.isCompletado() ? "✓ " : "○ ")
                            + i.getNombre()
                            + (i.getCantidad() != null && !i.getCantidad().isBlank() ? " — " + i.getCantidad() : "")
                            + " [" + i.getCategoria() + "]")
                    .toList();

            Platform.runLater(() -> {
                listaView.setItems(FXCollections.observableArrayList(textos));
                progresoLabel.setText(completados + "/" + total + " completados");
                mostrarEstado("Lista actualizada", TipoEstado.EXITO);
            });
        });

        task.setOnFailed(e -> mostrarEstado("Error al cargar lista: " + task.getException().getMessage(), TipoEstado.ERROR));

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
        Platform.runLater(() -> {
            statusLabel.setText(mensaje);
            statusLabel.setStyle(
                    "-fx-background-color: #020617; " +
                    STYLE_TEXT_FILL + color + "; " +
                    "-fx-padding: 12; " +
                    "-fx-font-size: 13px;"
            );
        });
    }

    private enum TipoEstado { INFO, EXITO, ERROR }
}
