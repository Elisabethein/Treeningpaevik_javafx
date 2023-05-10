package com.example.oop_v2;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class TrennidApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TrennidApplication.class.getResource("trennid.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 600, 600);
        stage.setTitle("Treeningpäevik");
        TrennidController.resize(stage, scene);
    }

    public static void main(String[] args) {
        launch();
    }
}