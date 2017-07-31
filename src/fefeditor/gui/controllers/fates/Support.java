package fefeditor.gui.controllers.fates;

import fefeditor.bin.ArrayConvert;
import fefeditor.bin.CharSupport;
import fefeditor.bin.SupportCharacter;
import fefeditor.bin.SupportBin;
import fefeditor.common.io.CompressionUtils;
import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import feflib.fates.TranslationManager;
import feflib.fates.gamedata.CharacterBlock;
import feflib.fates.gamedata.FatesGameData;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Support implements Initializable {
    @FXML
    private ListView<String> supportCList;
    @FXML
    private ListView<String> supportList;
    @FXML
    private VBox supportForm;

    private ComboBox<String> supportCharacters;
    private ComboBox<String> supportTypes;
    private ComboBox<String> typeEditor;

    private List<SupportCharacter> characters = new ArrayList<>();
    private List<CharacterBlock> unusedCharacters = new ArrayList<>();

    private SupportBin bin;

    private TranslationManager manager = TranslationManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bin = new SupportBin(FileData.getInstance().getWorkingFile());
        FatesGameData data = new FatesGameData(FileData.getInstance().getWorkingFile());
        for (CharacterBlock c : data.getCharacters()) {
            if (c.getSupportId() != -1) {
                SupportCharacter ch = new SupportCharacter();
                ch.setCharId(c.getId());
                ch.setSupportId(c.getSupportId());
                ch.setName(findName(c));
                characters.add(ch);
            } else {
                unusedCharacters.add(c);
            }
        }
        for (SupportCharacter c : characters)
            supportCList.getItems().add(c.getName());

        supportCharacters = new ComboBox<>();
        supportCharacters.setItems(supportCList.getItems());
        supportTypes = new ComboBox<>();
        supportTypes.getItems().addAll(Arrays.asList("Romantic", "Fast Romantic", "Platonic", "Fast Platonic"));

        String[] labels = {"Support Character:", "Support Type:"};
        for (int x = 0; x < labels.length; x++) {
            Label label = new Label(labels[x]);
            label.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 14));
            supportForm.getChildren().add(label);

            if (x == 0)
                supportForm.getChildren().add(supportCharacters);
            else
                supportForm.getChildren().add(supportTypes);
        }
        setSupportHandlers();
    }

    @SuppressWarnings("unused")
    @FXML
    private void export() {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter lzFilter = new FileChooser.ExtensionFilter("Compressed Bin File (*.bin.lz)", "*.bin.lz");
        FileChooser.ExtensionFilter binFilter = new FileChooser.ExtensionFilter("Bin File (*.bin)", "*.bin");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All Files", "*");
        chooser.getExtensionFilters().addAll(lzFilter, binFilter, allFilter);
        File file = chooser.showSaveDialog(GuiData.getInstance().getStage());
        if (file != null) {
            try {
                if (file.getName().endsWith("lz")) {
                    byte[] compressed = CompressionUtils.compress(bin.toBin());
                    Path path = Paths.get(file.getCanonicalPath());
                    Files.write(path, compressed);
                } else
                    Files.write(Paths.get(file.getCanonicalPath()), bin.toBin());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void openAddCharacterDialog() {
        List<String> choices = new ArrayList<>();
        unusedCharacters.forEach(c -> choices.add(findName(c)));
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Add Character");
        dialog.setHeaderText("Add a character to the support table.");
        dialog.setContentText("Choose character:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(s -> addCharacter(unusedCharacters.get(choices.indexOf(s))));
    }

    private void setSupportHandlers() {
        Button addSupport = new Button("Add");
        supportForm.getChildren().add(addSupport);

        supportForm.getChildren().add(new Label());
        typeEditor = new ComboBox<>();
        typeEditor.setItems(supportTypes.getItems());
        supportForm.getChildren().add(typeEditor);

        supportCList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            supportList.getItems().clear();
            if (newValue.intValue() == -1)
                return;
            supportTypes.getSelectionModel().clearSelection();
            supportCharacters.getSelectionModel().clearSelection();
            CharSupport[] supports = bin.getSupports(characters.get(newValue.intValue()));
            for (CharSupport c : supports) {
                supportList.getItems().add(getById(c.getCharId()).getName());
            }
        });
        supportList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == -1)
                return;
            SupportCharacter current = characters.get(supportCList.getSelectionModel().getSelectedIndex());
            CharSupport support = bin.getSupports(current)[newValue.intValue()];
            typeEditor.getSelectionModel().select(support.getType());
        });
        addSupport.setOnAction(event -> {
            if (supportCharacters.getSelectionModel().getSelectedIndex() == -1 || supportTypes.getSelectionModel().getSelectedIndex() == -1)
                return;
            SupportCharacter character = characters.get(supportCList.getSelectionModel().getSelectedIndex());
            SupportCharacter target = getByName(supportCharacters.getSelectionModel().getSelectedItem());
            CharSupport[] supports = bin.getSupports(character);
            CharSupport[] targetSupports = bin.getSupports(target);

            for (CharSupport c : supports) {
                if (target != null && c.getCharId() == target.getCharId()) return;
            }

            CharSupport newSupport = new CharSupport();
            if (target != null) {
                newSupport.setCharId(target.getCharId());
            }
            newSupport.setType(supportTypes.getSelectionModel().getSelectedIndex());
            CharSupport newSupportTwo = new CharSupport();
            newSupportTwo.setCharId(character.getCharId());
            newSupportTwo.setType(supportTypes.getSelectionModel().getSelectedIndex());
            addSupport(character, newSupport, supports.length);
            addSupport(target, newSupportTwo, targetSupports.length);

            supportCharacters.getSelectionModel().clearSelection();
            supportTypes.getSelectionModel().clearSelection();
            int selection = supportCList.getSelectionModel().getSelectedIndex();
            supportCList.getSelectionModel().clearSelection();
            supportCList.getSelectionModel().select(selection);
        });
        typeEditor.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (typeEditor.getSelectionModel().getSelectedIndex() != -1 && supportList.getSelectionModel().getSelectedIndex() != -1) {
                SupportCharacter character = characters.get(supportCList.getSelectionModel().getSelectedIndex());
                bin.changeSupportType(character, supportList.getSelectionModel().getSelectedIndex(), newValue.intValue());
            }
        });
    }

    private void addCharacter(CharacterBlock block) {
        // Create new character from forms.
        SupportCharacter character = new SupportCharacter();
        character.setName(findName(block));
        character.setCharId(block.getId());
        character.setSupportId(characters.size());
        characters.add(character);

        // Add character to GameData
        byte[] bytes = ArrayConvert.toByteArray(character.getSupportId());
        bin.addSupportTable(bytes, (short) character.getSupportId(), block.getBlockStart() - 0x20);

        // Update lists.
        supportCList.getItems().add(character.getName());
        unusedCharacters.remove(block);
    }

    private void addSupport(SupportCharacter character, CharSupport support, int index) {
        byte[] bytes = new byte[0xC];
        System.arraycopy(ArrayConvert.toByteArray((short) support.getCharId()), 0, bytes, 0, 2);
        System.arraycopy(ArrayConvert.toByteArray((short) index), 0, bytes, 2, 2);
        System.arraycopy(support.getBytes(), 0, bytes, 4, 4);
        bytes[0x8] = 0x1; // Fix for supports not leveling.
        bin.addSupport(character, bytes);
    }

    private SupportCharacter getById(int id) {
        for (SupportCharacter c : characters) {
            if (c.getCharId() == id)
                return c;
        }
        return null;
    }

    private SupportCharacter getByName(String name) {
        for (SupportCharacter c : characters) {
            if (c.getName().equals(name))
                return c;
        }
        return null;
    }

    private String findName(CharacterBlock c) {
        if (manager.getRaw().containsKey(c.getMPid()))
            return manager.getRaw().get(c.getMPid());
        else if (c.getId() == 0x1)
            return "Corrin (M)";
        else if (c.getId() == 0x2)
            return "Corrin (F)";
        else
            return "0x" + Long.toHexString(c.getId());
    }
}
