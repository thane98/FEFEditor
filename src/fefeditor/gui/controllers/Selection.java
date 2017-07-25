package fefeditor.gui.controllers;

import fefeditor.Main;
import fefeditor.common.FileDialogs;
import fefeditor.common.io.CompressionUtils;
import fefeditor.common.io.IOUtils;
import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import fefeditor.data.PrefsSingleton;
import fefeditor.gui.controllers.fates.Dialogue;
import fefeditor.gui.controllers.fates.DialogueDLC;
import fefeditor.gui.controllers.fates.SupportDLC;
import feflib.fates.TranslationManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Selection implements Initializable {
    @FXML private ListView<String> typeList;
    @FXML private AnchorPane contentPane;

    private AnchorPane content;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        typeList.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2)
                openFile();
        });

        for (String s : GuiData.getInstance().getBaseTypes())
            typeList.getItems().add(s);

        contentPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (content != null)
                content.setPrefWidth(newValue.doubleValue());
        });
        contentPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (content != null)
                content.setPrefHeight(newValue.doubleValue());
        });

        initAutoComplete();
    }

    @FXML
    private void activateBaseList() {
        typeList.getItems().clear();
        for (String s : GuiData.getInstance().getBaseTypes())
            typeList.getItems().add(s);
    }

    @FXML
    private void activateDlcList() {
        typeList.getItems().clear();
        for (String s : GuiData.getInstance().getDlcTypes())
            typeList.getItems().add(s);
    }

    @SuppressWarnings("unused")
    @FXML
    private void close() {
        Platform.exit();
    }

    @FXML
    private void openAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About FEFEditor");
        alert.setContentText("A general purpose editor for Fire Emblem Fates by thane98. Please consult the main thread " +
                "or readme if you need help with using any of the provided tools. Special thanks to SecretiveCactus, RainThunder, DeathChaos25, " +
                "SciresM, and Hextator for their works which helped make this editor a reality.");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));
        alert.showAndWait();
    }

    @FXML
    private void openOptions() {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(Main.class.getResource("gui/fxml/options.fxml"));
            Scene scene = new Scene(root, 395, 150);
            scene.getStylesheets().add(Main.class.getResource("gui/jmetro/JMetroLightTheme.css").toExternalForm());
            stage.setResizable(false);
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();

            stage.setOnCloseRequest(e -> initAutoComplete());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openFile() {
        switch (typeList.getSelectionModel().getSelectedItem()) {
            case "FEFNightmare":
                openExternal("FEFNightmare4.jar");
                break;
            case "Dispo Editor":
                openEditor("Dispo.fxml");
                break;
            case "Person Editor":
                openEditor("Person.fxml");
                break;
            case "Castle Join Editor":
                openEditor("Join.fxml");
                break;
            case "Fates Script":
                openExternal("Fates-Script.jar");
                break;
            case "Fates Script 3":
                openExternal("Fates-Script3.jar");
                break;
            case "IndirectSound Editor":
                openEditor("IndirectSound.fxml");
                break;
            case "Dialogue Editor":
                openDialogue();
                break;
            case "Terrain Editor":
                openEditor("Terrain.fxml");
                break;
            case "Support Editor":
                openEditor("Support.fxml");
                break;
            case "Dialogue Editor (DLC)":
                openDialogueDLC();
                break;
            case "Support Editor (DLC)":
                openSupportDLC();
                break;
            default:
                break;
        }
    }

    private void openEditor(String name) {
        if (FileDialogs.openBin(GuiData.getInstance().getStage())) {
            try {
                Stage stage = new Stage();
                GuiData.getInstance().setWorkingStage(stage);
                content = FXMLLoader.load(Main.class.getResource("gui/fxml/" + name));
                changeContent();
                FileData.getInstance().getWorkingFile().deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openDialogue() {
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(GuiData.getInstance().getWorkingStage());
        if (file != null) {
            try {
                File workingFile = Files.createTempDirectory(Paths.get(FileData.getInstance().getTemp().getAbsolutePath()),
                        "Text").toFile();
                IOUtils.copyFolder(file, workingFile);
                FileData.getInstance().setWorkingFile(workingFile);

                Stage stage = new Stage();
                GuiData.getInstance().setWorkingStage(stage);
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("gui/fxml/Dialogue.fxml"));
                content = loader.load();
                changeContent();
                Dialogue controller = loader.getController();
                controller.addAccelerators();
                workingFile.deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openDialogueDLC() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(GuiData.getInstance().getStage());
        if (file != null) {
            try {
                File workingFile = File.createTempFile("FEFWORKING", null, FileData.getInstance().getTemp());
                FileData.getInstance().setWorkingFile(workingFile);
                byte[] out = Files.readAllBytes(Paths.get(file.getCanonicalPath()));
                Files.write(Paths.get(workingFile.getCanonicalPath()), out);

                Stage stage = new Stage();
                GuiData.getInstance().setWorkingStage(stage);
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("gui/fxml/DialogueDLC.fxml"));
                content = loader.load();
                changeContent();
                DialogueDLC controller = loader.getController();
                controller.addAccelerators();

                FileData.getInstance().getWorkingFile().deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openSupportDLC() {
        if (FileDialogs.openBin(GuiData.getInstance().getStage())) {
            try {
                Stage stage = new Stage();
                GuiData.getInstance().setWorkingStage(stage);
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("gui/fxml/SupportDLC.fxml"));
                content = loader.load();
                changeContent();
                FileData.getInstance().getWorkingFile().deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openExternal(String name) {
        try {
            @SuppressWarnings("unused")
            Process proc = Runtime.getRuntime().exec("java -jar external/" + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeContent() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(content);
        content.setPrefWidth(contentPane.getWidth());
        content.setPrefHeight(contentPane.getHeight());
    }

    private void initAutoComplete() {
        if(!PrefsSingleton.getInstance().isAutoComplete()) {
            TranslationManager.getInstance().clearSuggestionLists();
            return;
        }
        String text = PrefsSingleton.getInstance().getTextPath();
        File file = new File(text);
        if(text.equals("") || !file.exists()) {
            try {
                TranslationManager.getInstance().loadSuggestionList(Files.readAllLines(Paths.get(
                        Main.class.getResource("data/text/GameData.txt").toURI())));
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        else if(text.endsWith(".lz")) {
            String[] lines = CompressionUtils.extractMessageArchive(CompressionUtils.decompress(file));
            List<String> list = new ArrayList<>();
            list.addAll(Arrays.asList(lines));
            TranslationManager.getInstance().loadSuggestionList(list);
        }
        else if(text.endsWith(".txt")) {
            try {
                TranslationManager.getInstance().loadSuggestionList(Files.readAllLines(file.toPath()));
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}