package fefeditor.gui.controllers;

import fefeditor.FEFEditor;
import fefeditor.common.FileDialogs;
import fefeditor.common.io.CompressionUtils;
import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import fefeditor.data.PrefsSingleton;
import fefeditor.gui.controllers.fates.Dialogue;
import fefeditor.gui.controllers.fates.DialogueDLC;
import feflib.fates.TranslationManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
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
import java.nio.file.StandardCopyOption;
import java.util.*;

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

    @FXML
    private void activateAwakeningList() {
        typeList.getItems().clear();
        for (String s : GuiData.getInstance().getAwakeningTypes())
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
        stage.getIcons().add(new Image(FEFEditor.class.getResourceAsStream("icon.png")));
        alert.showAndWait();
    }

    @FXML
    private void openOptions() {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(FEFEditor.class.getResource("gui/fxml/options.fxml"));
            Scene scene = new Scene(root, 395, 150);
            scene.getStylesheets().add(FEFEditor.class.getResource("gui/css/JMetroLightTheme.css").toExternalForm());
            stage.getIcons().add(new Image(FEFEditor.class.getResourceAsStream("icon.png")));
            stage.setTitle("Options");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();

            stage.setOnCloseRequest(e -> initAutoComplete());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void makeChapter() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select ROM Path");
        File rom = chooser.showDialog(GuiData.getInstance().getStage());
        chooser.setTitle("Select Output Folder");
        File dest = chooser.showDialog(GuiData.getInstance().getStage());
        if(rom != null && dest != null) {
            String sourceCid = null;
            String targetCid = null;

            TextInputDialog dialog = new TextInputDialog("A000");
            dialog.setTitle("Make Chapter");
            dialog.setHeaderText("Source Chapter CID");
            dialog.setContentText("Please the source chapter CID:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent())
                sourceCid = result.get();
            dialog = new TextInputDialog("A000");
            dialog.setTitle("Make Chapter");
            dialog.setHeaderText("Target Chapter CID");
            dialog.setContentText("Please the target chapter CID:");
            result = dialog.showAndWait();
            if (result.isPresent())
                targetCid = result.get();

            if(sourceCid != null && targetCid != null) {
                String[] regions = { "", "@E/", "@U/", "@F/", "@G/", "@I/", "@S/" };
                File dSrc = new File(rom.getAbsolutePath() + "/GameData/Dispos/" + sourceCid + ".bin.lz");
                File dTarget = new File(dest.getAbsolutePath() + "/GameData/Dispos/" + targetCid + ".bin.lz");
                File pSrc = new File(rom.getAbsolutePath() + "/GameData/Person/" + sourceCid + ".bin.lz");
                File pTarget = new File(dest.getAbsolutePath() + "/GameData/Person/" + targetCid + ".bin.lz");
                File tSrc = new File(rom.getAbsolutePath() + "/GameData/Terrain/" + sourceCid + ".bin.lz");
                File tTarget = new File(dest.getAbsolutePath() + "/GameData/Terrain/" + targetCid + ".bin.lz");
                File cSrc = new File(rom.getAbsolutePath() + "/map/Config/" + sourceCid + ".bin");
                File cTarget = new File(dest.getAbsolutePath() + "/map/Config/" + targetCid + ".bin");
                File sTxSrc = new File(rom.getAbsolutePath() + "/Scripts/" + sourceCid + "_Terrain.cmb");
                File sTxTarget = new File(dest.getAbsolutePath() + "/Scripts/" + targetCid + "_Terrain.cmb");
                File sSrc;
                File sTarget;
                if(new File(rom.getAbsolutePath() + "/Scripts/" + sourceCid + ".cmb").exists()) {
                    sSrc = new File(rom.getAbsolutePath() + "/Scripts/" + sourceCid + ".cmb");
                    sTarget = new File(dest.getAbsolutePath() + "/Scripts/" + targetCid + ".cmb");
                }
                else if(new File(rom.getAbsolutePath() + "/Scripts/A/" + sourceCid + ".cmb").exists()) {
                    sSrc = new File(rom.getAbsolutePath() + "/Scripts/A/" + sourceCid + ".cmb");
                    sTarget = new File(dest.getAbsolutePath() + "/Scripts/A/" + targetCid + ".cmb");
                }
                else if(new File(rom.getAbsolutePath() + "/Scripts/B/" + sourceCid + ".cmb").exists()) {
                    sSrc = new File(rom.getAbsolutePath() + "/Scripts/B/" + sourceCid + ".cmb");
                    sTarget = new File(dest.getAbsolutePath() + "/Scripts/B/" + targetCid + ".cmb");
                }
                else {
                    sSrc = new File(rom.getAbsolutePath() + "/Scripts/C/" + sourceCid + ".cmb");
                    sTarget = new File(dest.getAbsolutePath() + "/Scripts/C/" + targetCid + ".cmb");
                }
                File txSrc = null;
                File txTarget = null;
                for(String s : regions) {
                    txSrc = new File(rom.getAbsolutePath() + "/m/" + s + sourceCid + ".bin.lz");
                    if(txSrc.exists()) {
                        txTarget = new File(dest.getAbsolutePath() + "/m/" + s + targetCid + ".bin.lz");
                        break;
                    }
                    txSrc = new File(rom.getAbsolutePath() + "/m/A/" + s + sourceCid + ".bin.lz");
                    if(txSrc.exists()) {
                        txTarget = new File(dest.getAbsolutePath() + "/m/A/" + s + targetCid + ".bin.lz");
                        break;
                    }
                    txSrc = new File(rom.getAbsolutePath() + "/m/B/" + s + sourceCid + ".bin.lz");
                    if(txSrc.exists()) {
                        txTarget = new File(dest.getAbsolutePath() + "/m/B/" + s + targetCid + ".bin.lz");
                        break;
                    }
                    txSrc = new File(rom.getAbsolutePath() + "/m/C/" + s + sourceCid + ".bin.lz");
                    if(txSrc.exists()) {
                        txTarget = new File(dest.getAbsolutePath() + "/m/C/" + s + targetCid + ".bin.lz");
                        break;
                    }
                }
                if(txTarget == null)
                    return;

                dTarget.getParentFile().mkdirs();
                pTarget.getParentFile().mkdirs();
                tTarget.getParentFile().mkdirs();
                cTarget.getParentFile().mkdirs();
                txTarget.getParentFile().mkdirs();
                sTxTarget.getParentFile().mkdirs();
                sTarget.getParentFile().mkdirs();

                try {
                    Files.copy(dSrc.toPath(), dTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(pSrc.toPath(), pTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(tSrc.toPath(), tTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(cSrc.toPath(), cTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(txSrc.toPath(), txTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(sSrc.toPath(), sTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    if(sTxSrc.exists())
                        Files.copy(sTxSrc.toPath(), sTxTarget.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void openFile() {
        switch (typeList.getSelectionModel().getSelectedItem()) {
            case "FEFNightmare":
                openExternal("FEFNightmare4.jar");
                break;
            case "Dispo Editor":
                openEditor("fates/Dispo.fxml");
                break;
            case "Person Editor":
                openEditor("fates/Person.fxml");
                break;
            case "Castle Join Editor":
                openEditor("fates/Join.fxml");
                break;
            case "Fates Script":
                openExternal("Fates-Script.jar");
                break;
            case "Fates Script 3":
                openExternal("Fates-Script3.jar");
                break;
            case "IndirectSound Editor":
                openEditor("fates/IndirectSound.fxml");
                break;
            case "Dialogue Editor":
                openDialogue();
                break;
            case "Terrain Editor":
                openEditor("fates/Terrain.fxml");
                break;
            case "Support Editor":
                openEditor("fates/Support.fxml");
                break;
            case "GameData Text Editor":
                openEditor("fates/GameDataText.fxml");
                break;
            case "Dialogue Editor (DLC)":
                openDialogueDLC();
                break;
            case "Support Editor (DLC)":
                openSupportDLC();
                break;
            case "(A) Dispo Editor":
                openEditor("awakening/Dispo.fxml");
                break;
            default:
                break;
        }
    }

    private void openEditor(String name) {
        if (FileDialogs.openBin(GuiData.getInstance().getStage())) {
            try {
                content = FXMLLoader.load(FEFEditor.class.getResource("gui/fxml/" + name));
                changeContent();
                FileData.getInstance().getWorkingFile().deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openDialogue() {
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(GuiData.getInstance().getStage());
        if (file != null) {
            try {
                FileData.getInstance().setWorkingFile(file);
                FXMLLoader loader = new FXMLLoader(FEFEditor.class.getResource("gui/fxml/fates/Dialogue.fxml"));
                content = loader.load();
                changeContent();
                Dialogue controller = loader.getController();
                controller.addAccelerators();
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

                FXMLLoader loader = new FXMLLoader(FEFEditor.class.getResource("gui/fxml/fates/DialogueDLC.fxml"));
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
                FXMLLoader loader = new FXMLLoader(FEFEditor.class.getResource("gui/fxml/fates/SupportDLC.fxml"));
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
                        FEFEditor.class.getResource("data/text/FatesGameData.txt").toURI())));
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