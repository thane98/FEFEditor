package fefeditor;

import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import fefeditor.data.PrefsSingleton;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class FEFEditor extends Application {
    @Override
    public void start(Stage primaryStage) {
        GuiData.getInstance().setStage(primaryStage);
        FileData.getInstance();
        PrefsSingleton.getInstance();

        try {
            Parent root = FXMLLoader.load(this.getClass().getResource("gui/fxml/Selection.fxml"));
            Scene scene = new Scene(root, 1000, 800);
            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
            scene.getStylesheets().add(this.getClass().getResource("gui/css/JMetroLightTheme.css").toExternalForm());
            scene.getStylesheets().add(this.getClass().getResource("gui/css/dispo.css").toExternalForm());
            primaryStage.setTitle("FEFEditor");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}