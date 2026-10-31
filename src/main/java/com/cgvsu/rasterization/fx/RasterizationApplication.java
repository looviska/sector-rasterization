package com.cgvsu.rasterization.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

/** Точка входа JavaFX-приложения: загружает mainwindow.fxml и показывает окно. */
public class RasterizationApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                RasterizationApplication.class.getResource("/mainwindow.fxml"));
        AnchorPane root = loader.load();

        stage.setTitle("Sector Rasterization App");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}