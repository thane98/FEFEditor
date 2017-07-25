package fefeditor.gui.controllers.fates;

import fefeditor.Main;
import fefeditor.common.io.CompressionUtils;
import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

public class DialogueDLC implements Initializable {
    @FXML
    private TextArea valueBox;
    @FXML
    private TextField pidField;
    @FXML
    private ListView<String> dialogueList;
    @FXML
    private AnchorPane pane;
    @FXML
    private Label pidLabel;
    @FXML
    private ProgressIndicator progress;

    private List<String> prefixes = new ArrayList<>();
    private List<String> suffixes = new ArrayList<>();
    private HashMap<String, List<String>> fileMap = new HashMap<>();

    private File working = FileData.getInstance().getWorkingFile();

    private String currentPid;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addEventHandlers();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(Main.class.getResourceAsStream("data/xml/Dialogue.xml"));
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getDocumentElement().getElementsByTagName("Types").item(0).getChildNodes();
            for (int x = 0; x < nList.getLength(); x++) {
                if (nList.item(x).getNodeType() == Node.ELEMENT_NODE) {
                    Node node = nList.item(x);
                    dialogueList.getItems().add(node.getAttributes().getNamedItem("name").getNodeValue());
                    prefixes.add(node.getAttributes().getNamedItem("prefix").getNodeValue());
                    suffixes.add(node.getAttributes().getNamedItem("suffix").getNodeValue());
                }
            }

            byte[] bytes = CompressionUtils.decompress(FileData.getInstance().getWorkingFile());
            String[] lines = CompressionUtils.extractMessageArchive(bytes);
            List<String> list = new ArrayList<>();
            list.addAll(Arrays.asList(lines));
            fileMap.put(FileData.getInstance().getWorkingFile().getAbsolutePath(), list);
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Decompression Error");
            alert.setHeaderText("Unable to decompress file.");
            alert.setContentText("Dialogue Editor was unable to properly decompress one of your files.");
            alert.showAndWait();
        }
    }

    @FXML
    private void export() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showSaveDialog(GuiData.getInstance().getWorkingStage());
        if (file != null) {
            try {
                Thread t = new Thread(() -> {
                    try {
                        progress.setVisible(true);
                        String[] lines = new String[fileMap.get(working.getAbsolutePath()).size()];
                        byte[] out = CompressionUtils.makeMessageArchive(fileMap.get(working.getAbsolutePath()).toArray(lines));
                        Files.write(Paths.get(file.getAbsolutePath()), CompressionUtils.compress(out));
                        progress.setVisible(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                t.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void save() {
        if (currentPid == null || currentPid.equals("") || dialogueList.getSelectionModel().getSelectedIndex() == -1)
            return;
        int selected = dialogueList.getSelectionModel().getSelectedIndex();
        int dialogueLine = getDialogueLine(selected);
        String line = prefixes.get(selected) + currentPid + suffixes.get(selected) + ": " + valueBox.getText();
        fileMap.get(working.getAbsolutePath()).set(dialogueLine, line);
    }

    @FXML
    private void updateValues() {
        valueBox.clear();
        dialogueList.getSelectionModel().clearSelection();
        currentPid = pidField.getText();
        pidLabel.setText("Current PID: " + currentPid);
        pidField.clear();
    }

    private void addEventHandlers() {
        dialogueList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            int dialogueLine = getDialogueLine(newValue.intValue());
            if (dialogueLine == -1)
                return;
            if (dialogueLine == fileMap.get(working.getAbsolutePath()).size()) {
                String line = prefixes.get(newValue.intValue()) + currentPid + suffixes.get(newValue.intValue())
                        + ": Placeholder";
                fileMap.get(working.getAbsolutePath()).add(line);
            }

            String line = fileMap.get(working.getAbsolutePath()).get(dialogueLine);
            String[] split = line.split(": ");
            StringBuilder value = new StringBuilder();
            for (int x = 1; x < split.length; x++)
                value.append(split[x]);
            valueBox.setText(value.toString());
        });
    }

    private int getDialogueLine(int index) {
        if (currentPid == null || currentPid.equals("") || index == -1)
            return -1;
        List<String> lines = fileMap.get(working.getAbsolutePath());

        String search = prefixes.get(index) + currentPid + suffixes.get(index);
        for (int x = 0; x < lines.size(); x++) {
            if (lines.get(x).startsWith(search))
                return x;
        }

        // Indicate that a new line should be added to the file.
        return lines.size();
    }

    public void addAccelerators() {
        pane.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.S,
                KeyCombination.SHORTCUT_DOWN), this::save);
    }
}
