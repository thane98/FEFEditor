package fefeditor.gui.controllers.fates;

import fefeditor.bin.blocks.CharSupport;
import fefeditor.bin.blocks.SupportCharacter;
import fefeditor.common.inject.ArrayConvert;
import fefeditor.common.io.CompressionUtils;
import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import fefeditor.common.inject.SupportBinDLC;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class SupportDLC implements Initializable {
    @FXML
    private ListView<String> supportCList;
    @FXML
    private ListView<String> supportList;
    @FXML
    private ListView<String> characterCList;
    @FXML
    private AnchorPane ap;
    @FXML
    private VBox supportForm;
    @FXML
    private VBox characterForm;

    private ComboBox<String> supportCharacters;
    private ComboBox<String> supportTypes;
    private ComboBox<String> typeEditor;

    private List<TextField> characterFields = new ArrayList<>();
    private List<SupportCharacter> characters = new ArrayList<>();

    private SupportBinDLC bin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            bin = new SupportBinDLC(FileData.getInstance().getWorkingFile());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(new FileInputStream(new File(System.getProperty("user.dir") + "/external/SupportsDLC.xml")));
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getDocumentElement().getElementsByTagName("character");
            for (int x = 0; x < nList.getLength(); x++) {
                if (nList.item(x).getNodeType() == Node.ELEMENT_NODE) {
                    Node node = nList.item(x);
                    SupportCharacter character = new SupportCharacter();
                    character.setName(node.getAttributes().getNamedItem("name").getNodeValue());
                    character.setCharId(Integer.parseInt(node.getAttributes().getNamedItem("charId").getNodeValue()));
                    character.setSupportId(Integer.parseInt(node.getAttributes().getNamedItem("supportId").getNodeValue()));
                    characters.add(character);
                }
            }
            for (SupportCharacter c : characters)
                supportCList.getItems().add(c.getName());
            characterCList.setItems(supportCList.getItems());

            supportCharacters = new ComboBox<>();
            supportCharacters.setItems(supportCList.getItems());
            supportTypes = new ComboBox<>();
            supportTypes.getItems().addAll(Arrays.asList(
                    "Romantic", "Fast Romantic", "Platonic", "Fast Platonic"
            ));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

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

        StringConverter<Short> hexFormatter = new StringConverter<Short>() {
            @Override
            public Short fromString(String string) {
                return Short.parseShort(string, 16);
            }

            @Override
            public String toString(Short object) {
                if (object == null)
                    return "";
                return Integer.toHexString(object).toUpperCase();
            }
        };

        String[] charLabels = {"Name:", "Char Id:", "Support Id:"};
        for (int x = 0; x < charLabels.length; x++) {
            String l = charLabels[x];
            Label label = new Label(l);
            label.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 14));
            characterForm.getChildren().add(label);

            TextField field = new TextField();
            if (x > 0)
                field.setTextFormatter(new TextFormatter<>(hexFormatter));
            characterFields.add(field);
            characterForm.getChildren().add(field);
        }

        for (SupportCharacter c : characters) {
            if (!bin.containsCharacter(c)) {
                byte[] bytes = ArrayConvert.toByteArray(c.getSupportId());
                bin.addSupportTable(bytes);
            }
        }

        setSupportHandlers();
        setCharacterHandlers();
    }

    @FXML
    private void export() {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter lzFilter = new FileChooser.ExtensionFilter("Compressed Bin File (*.bin.lz)", "*.bin.lz");
        FileChooser.ExtensionFilter binFilter = new FileChooser.ExtensionFilter("Bin File (*.bin)", "*.bin");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All Files", "*");
        chooser.getExtensionFilters().addAll(lzFilter, binFilter, allFilter);
        File file = chooser.showSaveDialog(GuiData.getInstance().getWorkingStage());
        if (file != null) {
            try {
                String fileName;
                if (file.getName().endsWith(".bin"))
                    fileName = file.getName().substring(0, file.getName().length() - 4);
                else if (file.getName().endsWith(".bin.lz"))
                    fileName = file.getName().substring(0, file.getName().length() - 7);
                else
                    fileName = file.getName();
                bin.replaceLabels(fileName);
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
    private void close() {
        Stage stage = (Stage) ap.getScene().getWindow();
        stage.close();
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
                if (c.getCharId() == target.getCharId())
                    return;
            }

            CharSupport newSupport = new CharSupport();
            newSupport.setCharId(target.getCharId());
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

    private void setCharacterHandlers() {
        Button addChar = new Button("Add");
        characterForm.getChildren().add(addChar);
        addChar.setOnAction(event -> addCharacter());
    }

    private void addCharacter() {
        for (TextField t : characterFields) {
            if (t.getText().equals(""))
                return;
        }
        // Create new character from forms.
        SupportCharacter character = new SupportCharacter();
        character.setName(characterFields.get(0).getText());
        character.setCharId(Integer.parseInt(characterFields.get(1).getText(), 16));
        character.setSupportId(Integer.parseInt(characterFields.get(2).getText(), 16));
        characters.add(character);

        // Add character to GameData
        byte[] bytes = ArrayConvert.toByteArray(character.getSupportId());
        bin.addSupportTable(bytes);

        // Add character to SupportsDLC.xml
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new FileInputStream(new File(System.getProperty("user.dir")
                    + "/external/SupportsDLC.xml")));
            Element root = document.getDocumentElement();
            Element newElement = document.createElement("character");
            newElement.setAttribute("name", character.getName());
            newElement.setAttribute("charId", character.getCharId() + "");
            newElement.setAttribute("supportId", character.getSupportId() + "");
            root.appendChild(newElement);

            DOMSource source = new DOMSource(document);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StreamResult result = new StreamResult(System.getProperty("user.dir") + "/external/SupportsDLC.xml");
            transformer.transform(source, result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Update lists.
        characterCList.getItems().add(character.getName());
        supportCList.setItems(characterCList.getItems());

        // Clear original fields.
        characterFields.get(0).setText("");
        characterFields.get(1).setText("0");
        characterFields.get(2).setText("0");
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
}