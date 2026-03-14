package com.nutrifit.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada del cliente JavaFX de NutriFit.
 * Carga la vista principal de gestión de alimentos.
 */

public class NutriFitClientApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                NutriFitClientApplication.class.getResource("/com/nutrifit/client/food-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 1100, 650);

        stage.setTitle("NutriFit - Gestión de alimentos");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}