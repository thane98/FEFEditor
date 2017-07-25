package fefeditor.gui.controllers;

import fefeditor.data.PrefsSingleton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.controlsfx.control.ToggleSwitch;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Options implements Initializable {
    @FXML private ToggleSwitch autoComplete;
    @FXML private TextField pathBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        autoComplete.setSelected(PrefsSingleton.getInstance().isAutoComplete());
        pathBox.setText(PrefsSingleton.getInstance().getTextPath());

        autoComplete.selectedProperty().addListener((observable, oldValue, newValue) ->
                PrefsSingleton.getInstance().setAutoComplete(newValue));
        pathBox.textProperty().addListener((observable, oldValue, newValue) ->
                PrefsSingleton.getInstance().setTextPath(newValue));
    }

    @FXML
    private void openPathSelection() {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("GameData Files (*.bin.lz, *.txt)",
                "*.bin.lz", "*.txt");
        FileChooser.ExtensionFilter lzFilter = new FileChooser.ExtensionFilter("Compressed Bin File (*.bin.lz)",
                "*.bin.lz");
        FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text File (*.txt)",
                "*.txt");
        chooser.getExtensionFilters().addAll(allFilter, lzFilter, textFilter);
        File file = chooser.showOpenDialog(pathBox.getScene().getWindow());
        if (file != null) {
            pathBox.setText(file.getAbsolutePath());
        }
    }
}
