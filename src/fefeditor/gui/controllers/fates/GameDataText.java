package fefeditor.gui.controllers.fates;

import fefeditor.common.FileDialogs;
import fefeditor.common.io.CompressionUtils;
import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import feflib.utils.Pair;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class GameDataText implements Initializable {
    @FXML private TextArea key;
    @FXML private TextArea value;
    @FXML private ListView<String> list;

    private List<String> lines = new ArrayList<>();
    private List<Pair<String, String>> pairs = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            String[] temp = CompressionUtils.extractMessageArchive(Files.readAllBytes(FileData.getInstance()
                    .getWorkingFile().toPath()));
            lines.addAll(Arrays.asList(temp));
            for(int x = 6; x < lines.size(); x++) {
                String[] split = lines.get(x).split(": ", 2);
                pairs.add(new Pair<>(split[0], split[1]));
                list.getItems().add(pairs.get(x - 6).getFirst());
            }

            list.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                key.setText(pairs.get(newValue.intValue()).getFirst());
                value.setText(pairs.get(newValue.intValue()).getSecond());
            });

            key.textProperty().addListener((observable, oldValue, newValue) -> {
                if(list.getSelectionModel().getSelectedIndex() != -1 && !value.getText().equals("")) {
                    int index = list.getSelectionModel().getSelectedIndex();
                    pairs.get(index).setFirst(newValue);
                    list.getItems().set(index, key.getText());
                }
            });

            value.textProperty().addListener((observable, oldValue, newValue) -> {
                if(list.getSelectionModel().getSelectedIndex() != -1) {
                    int index = list.getSelectionModel().getSelectedIndex();
                    pairs.get(index).setSecond(newValue);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void export() {
        for(int x = 0; x < pairs.size(); x++) {
            lines.set(x + 6, pairs.get(x).getFirst() + ": " + pairs.get(x).getSecond());
        }
        String[] arr = new String[lines.size()];
        lines.toArray(arr);
        FileDialogs.saveFile(GuiData.getInstance().getStage(), CompressionUtils.makeMessageArchive(arr));
    }

    @FXML
    private void addField() {
        lines.add("Placeholder_Key: Placeholder_Value");
        pairs.add(new Pair<>("Placeholder_Key", "Placeholder_Value"));
        list.getItems().add("Placeholder_Key");
    }
}
