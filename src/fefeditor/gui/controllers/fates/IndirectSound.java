package fefeditor.gui.controllers.fates;

import fefeditor.common.FileDialogs;
import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import feflib.controls.HexField;
import feflib.fates.sound.SoundBin;
import feflib.fates.sound.SoundEntry;
import feflib.fates.sound.VoiceGroup;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class IndirectSound implements Initializable {
    @FXML
    private ListView<String> groupList;
    @FXML
    private ListView<String> entryList;
    @FXML
    private VBox form;

    private SoundBin bin;
    private HexField tag = new HexField(4, true);
    private VoiceGroup selectedGroup;
    private SoundEntry selectedEntry;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bin = null;
        groupList.getItems().clear();
        bin = new SoundBin(FileData.getInstance().getWorkingFile());
        for (VoiceGroup g : bin.getEntries()) {
            groupList.getItems().add(g.getMainLabel());
        }
        form.getChildren().add(tag);

        groupList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != -1) {
                entryList.getItems().clear();
                selectedGroup = bin.getEntries().get(newValue.intValue());
                for (SoundEntry e : bin.getEntries().get(newValue.intValue()).getEntries()) {
                    entryList.getItems().add(e.getLabel());
                }
            }
        });
        entryList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() != -1) {
                selectedEntry = selectedGroup.getEntries().get(newValue.intValue());
                tag.setValue(selectedEntry.getTag());
            }
        });
        tag.textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedEntry != null)
                bin.changeTag(groupList.getSelectionModel().getSelectedIndex(), selectedEntry, tag.getValue());
        });
    }

    @SuppressWarnings("unused")
    @FXML
    private void export() {
        FileDialogs.saveFile(GuiData.getInstance().getStage(), bin.getInjectableFile().toBin());
    }

    @FXML
    private void addCharacter() {
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(GuiData.getInstance().getStage());
        if (file != null) {
            TextInputDialog dialog = new TextInputDialog("placeholder");
            dialog.setTitle("Name Input");
            dialog.setHeaderText("Name Input");
            dialog.setContentText("Please enter the voice set name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> bin.addEntries(name, file));

            entryList.getItems().clear();
            groupList.getItems().clear();
            selectedEntry = null;
            selectedGroup = null;
            for (VoiceGroup v : bin.getEntries()) {
                groupList.getItems().add(v.getMainLabel());
            }
        }
    }

    @FXML
    private void appendItem() {
        if (groupList.getSelectionModel().getSelectedIndex() == -1)
            return;
        TextInputDialog dialog = new TextInputDialog("placeholder");
        dialog.setTitle("Enter Sound Name");
        dialog.setHeaderText("Name Input");
        dialog.setContentText("Please enter the sound name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            bin.appendItem(groupList.getSelectionModel().getSelectedIndex(), name);
            entryList.getItems().clear();
            selectedEntry = null;
            for(SoundEntry e : selectedGroup.getEntries())
                entryList.getItems().add(e.getLabel());
        });
    }

    @FXML
    private void batchEditTags() {
        if(selectedGroup == null)
            return;

        // Create the custom dialog.
        Dialog<byte[]> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Look, a Custom Login Dialog");

        ButtonType editButtonType = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        HexField tag = new HexField(4, true);
        grid.add(new Label("Tag:"), 0, 0);
        grid.add(tag, 1, 0);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(tag::requestFocus);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == editButtonType) {
                return tag.getValue();
            }
            return null;
        });

        Optional<byte[]> result = dialog.showAndWait();

        result.ifPresent(usernamePassword -> {
            for(SoundEntry e : selectedGroup.getEntries()) {
                bin.changeTag(groupList.getSelectionModel().getSelectedIndex(), e, result.get());
            }
        });
    }
}
