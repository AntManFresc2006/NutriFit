package com.nutrifit.client.controller;

import com.nutrifit.client.NutriFitClientApplication;
import com.nutrifit.client.model.AlimentoFx;
import com.nutrifit.client.model.ComidaDto;
import com.nutrifit.client.model.ComidaItemDto;
import com.nutrifit.client.service.AlimentoApiClient;
import com.nutrifit.client.service.ComidaApiClient;
import com.nutrifit.client.session.SessionManager;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de la vista de registro de comidas.
 * Pantalla dividida: tabla de comidas del día (izquierda) e items de la comida
 * seleccionada con formulario para añadir alimentos (derecha).
 */
public class ComidaController {

    // --- Lado izquierdo: comidas del día ---

    @FXML
    private DatePicker fechaPicker;

    @FXML
    private TableView<ComidaDto> comidasTable;

    @FXML
    private TableColumn<ComidaDto, String> tipoColumn;

    @FXML
    private ComboBox<String> tipoComboBox;

    @FXML
    private Button nuevaComidaButton;

    @FXML
    private Button eliminarComidaButton;

    // --- Lado derecho: items de la comida seleccionada ---

    @FXML
    private TableView<ComidaItemDto> itemsTable;

    @FXML
    private TableColumn<ComidaItemDto, String> itemNombreColumn;

    @FXML
    private TableColumn<ComidaItemDto, Number> itemGramosColumn;

    @FXML
    private TableColumn<ComidaItemDto, Number> itemKcalColumn;

    @FXML
    private TableColumn<ComidaItemDto, Number> itemProteinasColumn;

    @FXML
    private TableColumn<ComidaItemDto, Number> itemGrasasColumn;

    @FXML
    private TableColumn<ComidaItemDto, Number> itemCarbosColumn;

    @FXML
    private TextField buscarAlimentoField;

    @FXML
    private TableView<AlimentoFx> alimentosBusquedaTable;

    @FXML
    private TableColumn<AlimentoFx, String> alimentoNombreColumn;

    @FXML
    private TextField gramosField;

    @FXML
    private Button anadirItemButton;

    @FXML
    private Button eliminarItemButton;

    // --- Compartidos ---

    @FXML
    private Label statusLabel;

    @FXML
    private Button alimentosButton;

    private final ComidaApiClient comidaApiClient = new ComidaApiClient();
    private final AlimentoApiClient alimentoApiClient = new AlimentoApiClient();

    private ComidaDto comidaSeleccionada;
    private ComidaItemDto itemSeleccionado;
    private AlimentoFx alimentoBusquedaSeleccionado;

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @FXML
    public void initialize() {
        // Columna de comidas
        tipoColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTipo()));

        // Columnas de items
        itemNombreColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));
        itemGramosColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getGramos()));
        itemKcalColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getKcalEstimadas()));
        itemProteinasColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getProteinasEstimadas()));
        itemGrasasColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getGrasasEstimadas()));
        itemCarbosColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getCarbosEstimados()));

        // Columna de búsqueda de alimentos
        alimentoNombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        // Tipos de comida disponibles
        tipoComboBox.getItems().addAll("DESAYUNO", "ALMUERZO", "MERIENDA", "CENA", "SNACK");
        tipoComboBox.setValue("DESAYUNO");

        // Fecha: hoy por defecto
        fechaPicker.setValue(LocalDate.now());

        comidasTable.setPlaceholder(new Label("Sin comidas registradas para este día"));
        itemsTable.setPlaceholder(new Label("Selecciona una comida para ver sus alimentos"));
        alimentosBusquedaTable.setPlaceholder(new Label("Busca un alimento para añadirlo"));

        // Selección de comida → cargar sus items
        comidasTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            comidaSeleccionada = nuevo;
            itemsTable.getItems().clear();
            alimentosBusquedaTable.getItems().clear();
            actualizarBotonesComida();
            if (nuevo != null) {
                cargarItems(nuevo.getId());
            }
        });

        // Selección de item
        itemsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            itemSeleccionado = nuevo;
            eliminarItemButton.setDisable(nuevo == null);
        });

        // Selección de alimento en búsqueda
        alimentosBusquedaTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            alimentoBusquedaSeleccionado = nuevo;
            anadirItemButton.setDisable(nuevo == null || comidaSeleccionada == null);
        });

        buscarAlimentoField.setOnAction(event -> onBuscarAlimento());

        actualizarBotonesComida();
        cargarComidas();
    }

    private void cargarComidas() {
        String fecha = fechaPicker.getValue().format(FECHA_FMT);
        Long usuarioId = SessionManager.getUsuarioId();

        Task<List<ComidaDto>> task = new Task<>() {
            @Override
            protected List<ComidaDto> call() throws Exception {
                return comidaApiClient.getByFecha(usuarioId, fecha);
            }
        };

        task.setOnSucceeded(event -> {
            comidasTable.getItems().setAll(task.getValue());
            itemsTable.getItems().clear();
            comidaSeleccionada = null;
            actualizarBotonesComida();
            mostrarEstado("Comidas cargadas para " + fecha, TipoEstado.EXITO);
        });

        ejecutarEnSegundoPlano(task, "Cargando comidas...");
    }

    private void cargarItems(Long comidaId) {
        Task<List<ComidaItemDto>> task = new Task<>() {
            @Override
            protected List<ComidaItemDto> call() throws Exception {
                return comidaApiClient.getItems(comidaId);
            }
        };

        task.setOnSucceeded(event -> {
            itemsTable.getItems().setAll(task.getValue());
            itemSeleccionado = null;
            eliminarItemButton.setDisable(true);
            mostrarEstado("Items cargados", TipoEstado.INFO);
        });

        ejecutarEnSegundoPlano(task, "Cargando alimentos de la comida...");
    }

    @FXML
    private void onCargarFecha() {
        comidasTable.getItems().clear();
        itemsTable.getItems().clear();
        comidaSeleccionada = null;
        actualizarBotonesComida();
        cargarComidas();
    }

    @FXML
    private void onNuevaComida() {
        String tipo = tipoComboBox.getValue();
        String fecha = fechaPicker.getValue().format(FECHA_FMT);
        Long usuarioId = SessionManager.getUsuarioId();

        Task<ComidaDto> task = new Task<>() {
            @Override
            protected ComidaDto call() throws Exception {
                return comidaApiClient.crear(usuarioId, fecha, tipo);
            }
        };

        task.setOnSucceeded(event -> {
            cargarComidas();
            mostrarEstado("Comida " + tipo + " creada para " + fecha, TipoEstado.EXITO);
        });

        ejecutarEnSegundoPlano(task, "Creando comida...");
    }

    @FXML
    private void onEliminarComida() {
        if (comidaSeleccionada == null) {
            mostrarEstado("Selecciona una comida para eliminar", TipoEstado.ERROR);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("Eliminar comida");
        alert.setContentText("¿Eliminar la comida \"" + comidaSeleccionada.getTipo() + "\"? Se borrarán todos sus alimentos.");

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            mostrarEstado("Eliminación cancelada", TipoEstado.INFO);
            return;
        }

        Long comidaId = comidaSeleccionada.getId();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                comidaApiClient.eliminar(comidaId);
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            cargarComidas();
            mostrarEstado("Comida eliminada", TipoEstado.EXITO);
        });

        ejecutarEnSegundoPlano(task, "Eliminando comida...");
    }

    @FXML
    private void onBuscarAlimento() {
        String query = buscarAlimentoField.getText().trim();
        if (query.isEmpty()) {
            alimentosBusquedaTable.getItems().clear();
            return;
        }

        Task<List<AlimentoFx>> task = new Task<>() {
            @Override
            protected List<AlimentoFx> call() throws Exception {
                return alimentoApiClient.search(query);
            }
        };

        task.setOnSucceeded(event -> {
            alimentosBusquedaTable.getItems().setAll(task.getValue());
            mostrarEstado("Búsqueda completada", TipoEstado.INFO);
        });

        ejecutarEnSegundoPlano(task, "Buscando alimentos...");
    }

    @FXML
    private void onAnadirItem() {
        if (comidaSeleccionada == null) {
            mostrarEstado("Selecciona una comida primero", TipoEstado.ERROR);
            return;
        }
        if (alimentoBusquedaSeleccionado == null) {
            mostrarEstado("Selecciona un alimento de la búsqueda", TipoEstado.ERROR);
            return;
        }

        double gramos;
        try {
            gramos = Double.parseDouble(gramosField.getText().trim());
            if (gramos <= 0) {
                mostrarEstado("Los gramos deben ser mayores que 0", TipoEstado.ERROR);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarEstado("Introduce un valor numérico válido para los gramos", TipoEstado.ERROR);
            gramosField.requestFocus();
            return;
        }

        Long comidaId = comidaSeleccionada.getId();
        Long alimentoId = alimentoBusquedaSeleccionado.getId();
        String nombre = alimentoBusquedaSeleccionado.getNombre();
        double gramosFinal = gramos;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                comidaApiClient.addItem(comidaId, alimentoId, gramosFinal);
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            cargarItems(comidaId);
            gramosField.clear();
            mostrarEstado(nombre + " añadido (" + gramosFinal + " g)", TipoEstado.EXITO);
        });

        ejecutarEnSegundoPlano(task, "Añadiendo alimento...");
    }

    @FXML
    private void onEliminarItem() {
        if (itemSeleccionado == null) {
            mostrarEstado("Selecciona un alimento para eliminar", TipoEstado.ERROR);
            return;
        }

        Long comidaId = itemSeleccionado.getComidaId();
        Long itemId = itemSeleccionado.getItemId();
        String nombre = itemSeleccionado.getNombre();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                comidaApiClient.eliminarItem(comidaId, itemId);
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            cargarItems(comidaId);
            mostrarEstado(nombre + " eliminado de la comida", TipoEstado.EXITO);
        });

        ejecutarEnSegundoPlano(task, "Eliminando alimento de la comida...");
    }

    @FXML
    private void onVolverAlimentos() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    NutriFitClientApplication.class.getResource("/com/nutrifit/client/food-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 1100, 650);

            Stage stage = (Stage) statusLabel.getScene().getWindow();
            stage.setTitle("NutriFit - Alimentos");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            mostrarEstado("No se pudo volver a la pantalla de alimentos: " + e.getMessage(), TipoEstado.ERROR);
        }
    }

    private void actualizarBotonesComida() {
        boolean haySeleccion = comidaSeleccionada != null;
        eliminarComidaButton.setDisable(!haySeleccion);
        anadirItemButton.setDisable(true);
        eliminarItemButton.setDisable(true);
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
