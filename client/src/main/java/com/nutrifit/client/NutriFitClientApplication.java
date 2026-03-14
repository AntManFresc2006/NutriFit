package com.nutrifit.client;

import com.nutrifit.client.session.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NutriFitClientApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SessionManager.setSession(1L, "Antonio Manuel", "antonio@example.com", "token-demo");

        FXMLLoader fxmlLoader = new FXMLLoader(
                NutriFitClientApplication.class.getResource("/com/nutrifit/client/diario-view.fxml")
        );

        Scene scene = new Scene(fxmlLoader.load(), 1000, 620);

        stage.setTitle("NutriFit - Resumen diario");
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
