package com.nutrifit.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NutriFitClientApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                NutriFitClientApplication.class.getResource("/com/nutrifit/client/login-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 1000, 620);

        stage.setTitle("NutriFit - Acceso");
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}