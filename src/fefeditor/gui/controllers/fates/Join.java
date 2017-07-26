package fefeditor.gui.controllers.fates;

import fefeditor.common.FileDialogs;
import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import feflib.fates.TranslationManager;
import feflib.fates.castle.join.FatesJoin;
import feflib.fates.castle.join.JoinBlock;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.util.ResourceBundle;

public class Join implements Initializable {
    @FXML private ListView<String> joinList;
    @FXML private TextField characterText;
    @FXML private TextField birthrightText;
    @FXML private TextField conquestText;
    @FXML private TextField revelationText;

    private TranslationManager manager = TranslationManager.getInstance();
    private MenuItem addBlock;
    private MenuItem removeBlock;
    private FatesJoin file;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        file = new FatesJoin(FileData.getInstance().getWorkingFile());
        for (JoinBlock j : file.getBlocks())
            joinList.getItems().add(j.getCharacter());
        ContextMenu contextMenu = new ContextMenu();
        addBlock = new MenuItem("Add Block");
        removeBlock = new MenuItem("Remove Block");
        contextMenu.getItems().add(addBlock);
        contextMenu.getItems().add(removeBlock);
        joinList.setContextMenu(contextMenu);
        addListeners();
    }
    
    @FXML
    private void save() {
        if (joinList.getSelectionModel().getSelectedIndex() != -1) {
            int index = joinList.getSelectionModel().getSelectedIndex();
            JoinBlock block = file.getBlocks().get(index);
            block.setCharacter(characterText.getText());
            block.setBirthrightJoin(birthrightText.getText());
            block.setConquestJoin(conquestText.getText());
            block.setRevelationJoin(revelationText.getText());
            updateList();
        }
    }

    @FXML
    private void export() {
        FileDialogs.saveFile(GuiData.getInstance().getStage(), file.serialize());
    }

    private void addListeners() {
        joinList.getSelectionModel().selectedIndexProperty().addListener((arg0, oldValue, newValue) -> {
            if (joinList.getSelectionModel().getSelectedIndex() != -1) {
                int index = newValue.intValue();
                characterText.setText(file.getBlocks().get(index).getCharacter());
                birthrightText.setText(file.getBlocks().get(index).getBirthrightJoin());
                conquestText.setText(file.getBlocks().get(index).getConquestJoin());
                revelationText.setText(file.getBlocks().get(index).getRevelationJoin());
            }
        });

        addBlock.setOnAction(event -> {
            JoinBlock j = new JoinBlock();
            if (file.getBlocks().size() > 0)
                j = new JoinBlock(file.getBlocks().get(0));
            file.getBlocks().add(j);
            updateList();
        });
        removeBlock.setOnAction(event -> {
            if (joinList.getSelectionModel().getSelectedIndex() != -1)
                file.getBlocks().remove(joinList.getSelectionModel().getSelectedIndex());
            updateList();
        });

        characterText.textProperty().addListener((observable, oldValue, newValue) -> {
            int index = joinList.getSelectionModel().getSelectedIndex();
            if(index != -1) {
                file.getBlocks().get(index).setCharacter(newValue);
                joinList.getItems().set(index, newValue);
            }
        });
        birthrightText.textProperty().addListener((observable, oldValue, newValue) -> {
            int index = joinList.getSelectionModel().getSelectedIndex();
            if(index != -1) {
                file.getBlocks().get(index).setBirthrightJoin(newValue);
            }
        });
        conquestText.textProperty().addListener((observable, oldValue, newValue) -> {
            int index = joinList.getSelectionModel().getSelectedIndex();
            if(index != -1) {
                file.getBlocks().get(index).setConquestJoin(newValue);
            }
        });
        revelationText.textProperty().addListener((observable, oldValue, newValue) -> {
            int index = joinList.getSelectionModel().getSelectedIndex();
            if(index != -1) {
                file.getBlocks().get(index).setRevelationJoin(newValue);
            }
        });

        AutoCompletionBinding<String> binding = TextFields.bindAutoCompletion(characterText,
                manager.getCharacters().keySet());
        binding.setOnAutoCompleted(event -> characterText.setText(manager.getRealEntry(characterText.getText())));
    }

    private void updateList() {
        joinList.getItems().clear();
        for (JoinBlock j : file.getBlocks())
            joinList.getItems().add(j.getCharacter());
    }
}
