package ru;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;

public class App extends Application {

    @Getter
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app.fxml"));
        Parent root = loader.load();

        FxmlControllerTlkToXml controller = loader.getController();
        controller.setApp(this);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        this.primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        Application.launch();
    }

}
