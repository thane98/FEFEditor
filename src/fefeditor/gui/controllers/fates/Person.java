package fefeditor.gui.controllers.fates;

import fefeditor.common.FileDialogs;
import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import feflib.fates.TranslationManager;
import feflib.fates.gamedata.person.FatesPerson;
import feflib.fates.gamedata.person.PersonBlock;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Person implements Initializable {
    @FXML private ListView<String> personList;
    @FXML private TextField pidText;
    @FXML private TextField aidText;
    @FXML private TextField fidText;
    @FXML private TextField mPidText;
    @FXML private TextField mPidHText;
    @FXML private TextField combatText;
    @FXML private TextField voiceText;

    private TranslationManager manager = TranslationManager.getInstance();
    private MenuItem addBlock;
    private MenuItem removeBlock;
    private FatesPerson person;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        try {
            person = new FatesPerson(FileData.getInstance().getWorkingFile());
            updateList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ContextMenu contextMenu = new ContextMenu();
        addBlock = new MenuItem("Add Block");
        removeBlock = new MenuItem("Remove Block");
        contextMenu.getItems().add(addBlock);
        contextMenu.getItems().add(removeBlock);
        personList.setContextMenu(contextMenu);

        addListeners();
    }

    @SuppressWarnings("unused")
    @FXML
    private void save() {
        if (personList.getSelectionModel().getSelectedIndex() != -1) {
            PersonBlock c = person.getCharacters().get(personList.getSelectionModel().getSelectedIndex());
            c.setPid(pidText.getText());
            c.setFid(fidText.getText());
            c.setAid(aidText.getText());
            c.setMPid(mPidText.getText());
            c.setMPidH(mPidHText.getText());
            c.setCombatMusic(combatText.getText());
            c.setEnemyVoice(voiceText.getText());
            updateList();
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void export() {
        FileDialogs.saveFile(GuiData.getInstance().getStage(), person.serialize());
    }

    @FXML
    private void updateFields() {
        if (personList.getSelectionModel().getSelectedIndex() != -1) {
            PersonBlock c = person.getCharacters().get(personList.getSelectionModel().getSelectedIndex());
            pidText.setText(c.getPid());
            aidText.setText(c.getAid());
            fidText.setText(c.getFid());
            mPidText.setText(c.getMPid());
            mPidHText.setText(c.getMPidH());
            combatText.setText(c.getCombatMusic());
            voiceText.setText(c.getEnemyVoice());
        }
    }

    private void clearFields() {
        pidText.clear();
        aidText.clear();
        fidText.clear();
        mPidText.clear();
        mPidHText.clear();
        combatText.clear();
        voiceText.clear();
    }

    private void updateList() {
        clearFields();
        personList.getItems().clear();
        for (PersonBlock c : person.getCharacters()) {
            personList.getItems().add(c.getPid());
        }
    }

    private void addListeners() {
        addBlock.setOnAction(event -> {
            PersonBlock c = new PersonBlock();
            if (person.getCharacters().size() > 0)
                c = new PersonBlock();
            c.setPid("Placeholder PID");
            person.getCharacters().add(c);
            updateList();
        });

        removeBlock.setOnAction(event -> {
            if (personList.getSelectionModel().getSelectedIndex() != -1)
                person.getCharacters().remove(personList.getSelectionModel().getSelectedIndex());
            updateList();
        });

        List<AutoCompletionBinding<String>> bindings = new ArrayList<>();
        bindings.add(TextFields.bindAutoCompletion(pidText,
                manager.getCharacters().keySet()));
        bindings.add(TextFields.bindAutoCompletion(fidText,
                manager.getCharacters().keySet()));
        bindings.add(TextFields.bindAutoCompletion(aidText,
                manager.getCharacters().keySet()));
        bindings.add(TextFields.bindAutoCompletion(mPidText,
                manager.getCharacters().keySet()));
        bindings.add(TextFields.bindAutoCompletion(mPidHText,
                manager.getCharacters().keySet()));
        bindings.get(0).setOnAutoCompleted(event -> pidText.setText(manager.getRealEntry(pidText.getText())));
        bindings.get(1).setOnAutoCompleted(event -> fidText.setText(manager.getRealEntry(fidText.getText())));
        bindings.get(2).setOnAutoCompleted(event -> aidText.setText(manager.getRealEntry(aidText.getText())));
        bindings.get(3).setOnAutoCompleted(event -> mPidText.setText(manager.getRealEntry(mPidText.getText())));
        bindings.get(4).setOnAutoCompleted(event -> mPidHText.setText(manager.getRealEntry(mPidHText.getText())));
    }
}
