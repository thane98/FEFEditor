package fefeditor.gui.controllers.fates;

import fefeditor.common.FileDialogs;
import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import feflib.controls.HexField;
import feflib.fates.TranslationManager;
import feflib.fates.gamedata.dispo.DispoBlock;
import feflib.fates.gamedata.dispo.DispoFaction;
import feflib.fates.gamedata.dispo.FatesDispo;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Dispo implements Initializable {
    @FXML private TreeView<String> factionTree;
    @FXML private VBox generalBox;
    @FXML private VBox skillBox;
    @FXML private VBox itemBox;
    @FXML private AnchorPane dispoPane;
    @FXML private CheckMenuItem coordCheck;
    @FXML private GridPane dispoGrid;
    @FXML private ScrollPane dispoScrollPane;

    private TranslationManager manager = TranslationManager.getInstance();
    private List<TextField> generalFields = new ArrayList<>();
    private List<HexField> generalHexFields = new ArrayList<>();
    private List<Spinner<Integer>> generalSpinners = new ArrayList<>();
    private List<TextField> itemFields = new ArrayList<>();
    private List<HexField> itemHexFields = new ArrayList<>();
    private List<TextField> skillFields = new ArrayList<>();
    private Region[][] dispoRegion;
    private FatesDispo file;
    private DispoFaction selectedFaction;
    private DispoBlock selectedBlock;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        GuiData.getInstance().getWorkingStage().setOnCloseRequest(we -> close());
        file = new FatesDispo(FileData.getInstance().getWorkingFile());
        setupDispoGrid();
        populateTree();

        factionTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                updateSelection(newValue));
        dispoScrollPane.widthProperty().addListener((observable, oldValue, newValue) ->
                dispoPane.setPrefWidth(newValue.doubleValue()));
        dispoScrollPane.heightProperty().addListener((observable, oldValue, newValue) ->
                dispoPane.setPrefHeight(newValue.doubleValue()));
        populateForm();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) factionTree.getScene().getWindow();
        stage.close();
    }

    @SuppressWarnings("unused")
    @FXML
    private void export() {
        FileDialogs.saveFile(GuiData.getInstance().getStage(), file.serialize());
    }

    @SuppressWarnings("unused")
    @FXML
    private void save() {

    }

    @FXML
    private void addBlock() {
        if (selectedFaction != null) {
            TextInputDialog dialog = new TextInputDialog("Placeholder");
            dialog.setTitle("Add Spawn");
            dialog.setHeaderText("Enter Spawn PID");
            dialog.setContentText("Please enter a PID for the spawn:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(str -> {
                for (DispoFaction f : file.getFactions()) {
                    if (f.getName().equals(str)) {
                        throwNameInUseDialog();
                        return;
                    }
                }
                selectedBlock = null;
                DispoBlock block = new DispoBlock(str);
                selectedFaction.addSpawn(block);
                populateTree();
                selectFaction(selectedFaction);
                clearFields();
            });
        }
    }

    @FXML
    private void duplicateBlock() {
        if (selectedBlock == null)
            return;
        DispoBlock block = new DispoBlock(selectedBlock);
        selectedBlock = null;
        selectedFaction.addSpawn(block);
        populateTree();
        selectFaction(selectedFaction);
        clearFields();
    }

    @FXML
    private void removeBlock() {
        if (selectedFaction != null && selectedBlock != null) {
            selectedFaction.getSpawns().remove(selectedBlock);
            selectedBlock = null;
            populateTree();
            for (TreeItem<String> item : factionTree.getRoot().getChildren()) {
                if (item.getValue().equals(selectedFaction.getName())) {
                    factionTree.getSelectionModel().select(item);
                    item.setExpanded(true);
                    clearFields();
                    break;
                }
            }
        }
    }

    @FXML
    private void addFaction() {
        TextInputDialog dialog = new TextInputDialog("Placeholder");
        dialog.setTitle("Add Faction");
        dialog.setHeaderText("Enter Faction Name");
        dialog.setContentText("Please enter an unused name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String str = result.get();
            for (DispoFaction f : file.getFactions()) {
                for (DispoBlock b : f.getSpawns()) {
                    if (b.getPid().equals(str)) {
                        throwNameInUseDialog();
                        return;
                    }
                }
                if (f.getName().equals(str)) {
                    throwNameInUseDialog();
                    return;
                }
            }
            selectedFaction = null;
            selectedBlock = null;
            DispoFaction faction = new DispoFaction(str);
            file.getFactions().add(faction);
            populateTree();
            clearFields();
        }
    }

    @FXML
    private void removeFaction() {
        if (selectedFaction != null) {
            file.getFactions().remove(selectedFaction);
            selectedFaction = null;
            selectedBlock = null;
            updateOccupiedCoords();
            populateTree();
            clearFields();
        }
    }

    @FXML
    private void toggleCoordTwo() {
        selectedBlock = null;
        selectedFaction = null;
        factionTree.getSelectionModel().select(factionTree.getRoot());
        clearFields();
        updateOccupiedCoords();
    }

    private void selectFaction(DispoFaction faction) {
        for (TreeItem<String> item : factionTree.getRoot().getChildren()) {
            if (item.getValue().equals(faction.getName())) {
                factionTree.getSelectionModel().select(item);
                item.setExpanded(true);
                break;
            }
        }
    }

    private void updateSelection(TreeItem<String> item) {
        if (item == null || factionTree.getRoot() == null)
            return;
        if (item.equals(factionTree.getRoot()))
            return;

        selectedFaction = null;
        selectedBlock = null;
        clearFields();

        boolean isFaction = false;
        for (DispoFaction f : file.getFactions()) {
            if (f.getName().equals(item.getValue()))
                isFaction = true;
        }
        if (isFaction) {
            updateOccupiedCoords(item.getValue());
            return;
        }

        // The selected item is a character block.
        TreeItem<String> factionItem = item.getParent();
        updateOccupiedCoords(factionItem.getValue());
        setSelectedSpawn(factionItem, item);
        updateFields();
    }

    private void populateForm() {
        generalBox.getChildren().add(new Label("PID:"));
        generalBox.getChildren().add(new TextField());
        generalFields.add((TextField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("Coordinate 1:"));
        generalBox.getChildren().add(new HexField(2, true));
        generalHexFields.add((HexField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("Coordinate 2:"));
        generalBox.getChildren().add(new HexField(2, true));
        generalHexFields.add((HexField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("Team:"));
        generalSpinners.add(new Spinner<>(new SpinnerValueFactory
                .IntegerSpinnerValueFactory(0, 255)));
        generalBox.getChildren().add(generalSpinners.get(generalSpinners.size() - 1));
        generalBox.getChildren().add(new Label("Level:"));
        generalSpinners.add(new Spinner<>(new SpinnerValueFactory
                .IntegerSpinnerValueFactory(0, 255)));
        generalBox.getChildren().add(generalSpinners.get(generalSpinners.size() - 1));
        generalBox.getChildren().add(new Label("Spawn Bitflags:"));
        generalBox.getChildren().add(new HexField(4, true));
        generalHexFields.add((HexField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("Skill Bitflags:"));
        generalBox.getChildren().add(new HexField(4, true));
        generalHexFields.add((HexField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("AI AC:"));
        generalBox.getChildren().add(new TextField());
        generalFields.add((TextField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("AC Parameter:"));
        generalBox.getChildren().add(new TextField());
        generalFields.add((TextField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("AI MI:"));
        generalBox.getChildren().add(new TextField());
        generalFields.add((TextField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("MI Parameter:"));
        generalBox.getChildren().add(new TextField());
        generalFields.add((TextField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("AI AT:"));
        generalBox.getChildren().add(new TextField());
        generalFields.add((TextField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("AT Parameter:"));
        generalBox.getChildren().add(new TextField());
        generalFields.add((TextField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("AI MV:"));
        generalBox.getChildren().add(new TextField());
        generalFields.add((TextField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("MV Parameter:"));
        generalBox.getChildren().add(new TextField());
        generalFields.add((TextField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        
        itemBox.getChildren().add(new Label("Item 1:"));
        itemBox.getChildren().add(new TextField());
        itemFields.add((TextField) itemBox.getChildren().get(itemBox.getChildren().size() - 1));
        itemBox.getChildren().add(new Label("Item 2:"));
        itemBox.getChildren().add(new TextField());
        itemFields.add((TextField) itemBox.getChildren().get(itemBox.getChildren().size() - 1));
        itemBox.getChildren().add(new Label("Item 3:"));
        itemBox.getChildren().add(new TextField());
        itemFields.add((TextField) itemBox.getChildren().get(itemBox.getChildren().size() - 1));
        itemBox.getChildren().add(new Label("Item 4:"));
        itemBox.getChildren().add(new TextField());
        itemFields.add((TextField) itemBox.getChildren().get(itemBox.getChildren().size() - 1));
        itemBox.getChildren().add(new Label("Item 5:"));
        itemBox.getChildren().add(new TextField());
        itemFields.add((TextField) itemBox.getChildren().get(itemBox.getChildren().size() - 1));
        itemBox.getChildren().add(new Label("Item 1 Bitflags:"));
        itemBox.getChildren().add(new HexField(4, true));
        itemHexFields.add((HexField) itemBox.getChildren().get(itemBox.getChildren().size() - 1));
        itemBox.getChildren().add(new Label("Item 2 Bitflags:"));
        itemBox.getChildren().add(new HexField(4, true));
        itemHexFields.add((HexField) itemBox.getChildren().get(itemBox.getChildren().size() - 1));
        itemBox.getChildren().add(new Label("Item 3 Bitflags:"));
        itemBox.getChildren().add(new HexField(4, true));
        itemHexFields.add((HexField) itemBox.getChildren().get(itemBox.getChildren().size() - 1));
        itemBox.getChildren().add(new Label("Item 4 Bitflags:"));
        itemBox.getChildren().add(new HexField(4, true));
        itemHexFields.add((HexField) itemBox.getChildren().get(itemBox.getChildren().size() - 1));
        itemBox.getChildren().add(new Label("Item 5 Bitflags:"));
        itemBox.getChildren().add(new HexField(4, true));
        itemHexFields.add((HexField) itemBox.getChildren().get(itemBox.getChildren().size() - 1));

        skillBox.getChildren().add(new Label("Skill 1:"));
        skillBox.getChildren().add(new TextField());
        skillFields.add((TextField) skillBox.getChildren().get(skillBox.getChildren().size() - 1));
        skillBox.getChildren().add(new Label("Skill 2:"));
        skillBox.getChildren().add(new TextField());
        skillFields.add((TextField) skillBox.getChildren().get(skillBox.getChildren().size() - 1));
        skillBox.getChildren().add(new Label("Skill 3:"));
        skillBox.getChildren().add(new TextField());
        skillFields.add((TextField) skillBox.getChildren().get(skillBox.getChildren().size() - 1));
        skillBox.getChildren().add(new Label("Skill 4:"));
        skillBox.getChildren().add(new TextField());
        skillFields.add((TextField) skillBox.getChildren().get(skillBox.getChildren().size() - 1));
        skillBox.getChildren().add(new Label("Skill 5:"));
        skillBox.getChildren().add(new TextField());
        skillFields.add((TextField) skillBox.getChildren().get(skillBox.getChildren().size() - 1));
        setupAutoComplete();
        addListeners();
    }

    private void setupAutoComplete() {
        List<AutoCompletionBinding<String>> bindings = new ArrayList<>();
        bindings.add(TextFields.bindAutoCompletion(generalFields.get(0),
                manager.getCharacters().keySet()));
        for(int x = 0; x < 5; x++) {
            bindings.add(TextFields.bindAutoCompletion(itemFields.get(x),
                    manager.getItems().keySet()));
            bindings.add(TextFields.bindAutoCompletion(skillFields.get(x),
                    manager.getSkills().keySet()));
        }

        bindings.get(0).setOnAutoCompleted(event -> generalFields.get(0).setText(
                manager.getRealEntry(generalFields.get(0).getText())));
        for(int x = 0; x < 5; x++) {
            int i = x;
            bindings.get((x * 2) + 1).setOnAutoCompleted(event -> itemFields.get(i).setText(
                    manager.getRealEntry(itemFields.get(i).getText())));
            bindings.get((x * 2) + 2).setOnAutoCompleted(event -> skillFields.get(i).setText(
                    manager.getRealEntry(skillFields.get(i).getText())));
        }
    }

    private void addListeners() {
        generalFields.get(0).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null) {
                selectedBlock.setPid(newValue);
                factionTree.getSelectionModel().getSelectedItem().setValue(newValue);
            }
        });
        generalFields.get(1).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setAc(newValue);
        });
        generalFields.get(2).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setAiPositionOne(newValue);
        });
        generalFields.get(3).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setMi(newValue);
        });
        generalFields.get(4).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setAiPositionTwo(newValue);
        });
        generalFields.get(5).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setAt(newValue);
        });
        generalFields.get(6).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setAiPositionThree(newValue);
        });
        generalFields.get(7).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setMv(newValue);
        });
        generalFields.get(8).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setAiPositionFour(newValue);
        });
        generalHexFields.get(0).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null) {
                selectedBlock.setFirstCoord(generalHexFields.get(0).getValue());
                updateSelection(factionTree.getSelectionModel().getSelectedItem());
            }
        });
        generalHexFields.get(1).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null) {
                selectedBlock.setSecondCoord(generalHexFields.get(1).getValue());
                updateSelection(factionTree.getSelectionModel().getSelectedItem());
            }
        });
        generalHexFields.get(2).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setSpawnBitflags(generalHexFields.get(2).getValue());
        });
        generalHexFields.get(3).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setSkillFlag(generalHexFields.get(3).getValue());
        });
        generalSpinners.get(0).valueProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setTeam(generalSpinners.get(0).getValue().byteValue());
        });
        generalSpinners.get(1).valueProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setLevel(generalSpinners.get(1).getValue().byteValue());
        });

        for(int x = 0; x < 5; x++) {
            int i = x;
            itemFields.get(x).textProperty().addListener((observable, oldValue, newValue) -> {
                if(selectedBlock != null)
                    selectedBlock.setItem(newValue, i);
            });
            itemHexFields.get(x).textProperty().addListener((observable, oldValue, newValue) -> {
                if(selectedBlock != null)
                    selectedBlock.getItemBitflags()[i] = itemHexFields.get(i).getValue();
            });
            skillFields.get(x).textProperty().addListener((observable, oldValue, newValue) -> {
                if(selectedBlock != null)
                    selectedBlock.setSkill(newValue, i);
            });
        }
    }

    private void updateFields() {
        generalFields.get(0).setText(selectedBlock.getPid());
        generalFields.get(1).setText(selectedBlock.getAc());
        generalFields.get(2).setText(selectedBlock.getAiPositionOne());
        generalFields.get(3).setText(selectedBlock.getMi());
        generalFields.get(4).setText(selectedBlock.getAiPositionTwo());
        generalFields.get(5).setText(selectedBlock.getAt());
        generalFields.get(6).setText(selectedBlock.getAiPositionThree());
        generalFields.get(7).setText(selectedBlock.getMv());
        generalFields.get(8).setText(selectedBlock.getAiPositionFour());
        generalHexFields.get(0).setValue(selectedBlock.getFirstCoord());
        generalHexFields.get(1).setValue(selectedBlock.getSecondCoord());
        generalHexFields.get(2).setValue(selectedBlock.getSpawnBitflags());
        generalHexFields.get(3).setValue(selectedBlock.getSkillFlag());
        generalSpinners.get(0).getValueFactory().setValue((int) selectedBlock.getTeam());
        generalSpinners.get(1).getValueFactory().setValue((int) selectedBlock.getLevel());

        itemFields.get(0).setText(selectedBlock.getItems()[0]);
        itemFields.get(1).setText(selectedBlock.getItems()[1]);
        itemFields.get(2).setText(selectedBlock.getItems()[2]);
        itemFields.get(3).setText(selectedBlock.getItems()[3]);
        itemFields.get(4).setText(selectedBlock.getItems()[4]);
        itemHexFields.get(0).setValue(selectedBlock.getItemBitflags()[0]);
        itemHexFields.get(1).setValue(selectedBlock.getItemBitflags()[1]);
        itemHexFields.get(2).setValue(selectedBlock.getItemBitflags()[2]);
        itemHexFields.get(3).setValue(selectedBlock.getItemBitflags()[3]);
        itemHexFields.get(4).setValue(selectedBlock.getItemBitflags()[4]);

        skillFields.get(0).setText(selectedBlock.getSkills()[0]);
        skillFields.get(1).setText(selectedBlock.getSkills()[1]);
        skillFields.get(2).setText(selectedBlock.getSkills()[2]);
        skillFields.get(3).setText(selectedBlock.getSkills()[3]);
        skillFields.get(4).setText(selectedBlock.getSkills()[4]);
    }

    private void populateTree() {
        TreeItem<String> rootItem = new TreeItem<>(FileData.getInstance().getOriginal().getName());
        rootItem.setExpanded(true);
        for (DispoFaction f : file.getFactions()) {
            TreeItem<String> faction = new TreeItem<>(f.getName());
            for (DispoBlock b : f.getSpawns()) {
                TreeItem<String> block = new TreeItem<>(b.getPid());
                faction.getChildren().add(block);
            }
            rootItem.getChildren().add(faction);
        }

        factionTree.setRoot(rootItem);
        updateOccupiedCoords();
    }

    private void updateOccupiedCoords() {
        for (Node n : dispoGrid.getChildren())
            n.setId("dispoGrid");
        for (DispoFaction f : file.getFactions()) {
            for (DispoBlock b : f.getSpawns()) {
                int x;
                int y;
                if (coordCheck.isSelected()) {
                    x = b.getSecondCoord()[0];
                    y = b.getSecondCoord()[1];
                } else {
                    x = b.getFirstCoord()[0];
                    y = b.getFirstCoord()[1];
                }
                if (x > 0 && x < 32 && y > 0 && y < 32)
                    dispoRegion[x][y].setId("occupiedCoord");
            }
        }
    }

    private void updateOccupiedCoords(String factionName) {
        for (Node n : dispoGrid.getChildren())
            n.setId("dispoGrid");
        for (DispoFaction f : file.getFactions()) {
            if (factionName.equals(f.getName()))
                selectedFaction = f;
            for (DispoBlock b : f.getSpawns()) {
                int x;
                int y;
                if (coordCheck.isSelected()) {
                    x = b.getSecondCoord()[0];
                    y = b.getSecondCoord()[1];
                } else {
                    x = b.getFirstCoord()[0];
                    y = b.getFirstCoord()[1];
                }
                if (factionName.equals(f.getName())) {
                    if (x > 0 && x < 32 && y > 0 && y < 32)
                        dispoRegion[x][y].setId("selectedFaction");
                } else {
                    if (x > 0 && x < 32 && y > 0 && y < 32)
                        dispoRegion[x][y].setId("occupiedCoord");
                }
            }
        }
    }

    private void setupDispoGrid() {
        dispoRegion = new Region[32][32];
        for (int y = 0; y < 32; y++) {
            dispoGrid.getColumnConstraints().get(y).setFillWidth(true);
            dispoGrid.getRowConstraints().get(y).setFillHeight(true);
            for (int x = 0; x < 32; x++) {
                Region region = new Region();
                region.setMinSize(5.0, 5.0);
                region.setId("dispoGrid");
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                dispoRegion[x][y] = region;
                dispoGrid.getChildren().add(region);
                GridPane.setConstraints(region, x, y);

                int yCoord = y;
                int xCoord = x;
                region.setOnMouseClicked(event -> moveBlock(xCoord, yCoord));
            }
        }
    }

    private void setSelectedSpawn(TreeItem<String> faction, TreeItem<String> spawn) {
        int factionIndex = -1;
        for (DispoFaction f : file.getFactions()) {
            if (f.getName().equals(faction.getValue()))
                factionIndex = file.getFactions().indexOf(f);
        }
        int blockIndex = faction.getChildren().indexOf(spawn);

        DispoBlock block = file.getFactions().get(factionIndex).getSpawns().get(blockIndex);

        if (coordCheck.isSelected()) {
            if (block.getSecondCoord()[0] > 0 && block.getSecondCoord()[1] < 32 && block.getSecondCoord()[1] > 0 
                    && block.getSecondCoord()[1] < 32)
                dispoRegion[block.getSecondCoord()[0]][block.getSecondCoord()[1]].setId("selectedBlock");
        } else {
            if (block.getFirstCoord()[0] > 0 && block.getFirstCoord()[1] < 32 && block.getFirstCoord()[1] > 0
                    && block.getFirstCoord()[1] < 32)
                dispoRegion[block.getFirstCoord()[0]][block.getFirstCoord()[1]].setId("selectedBlock");
        }

        selectedBlock = block;
    }

    private void moveBlock(int x, int y) {
        if (selectedBlock != null && selectedFaction != null) {
            if (coordCheck.isSelected())
                selectedBlock.setSecondCoord(new byte[]{(byte) x, (byte) y});
            else
                selectedBlock.setFirstCoord(new byte[]{(byte) x, (byte) y});

            // Prompt the tree to reload the selection at the new coordinate.
            updateSelection(factionTree.getSelectionModel().getSelectedItem());
        }
    }

    private void clearFields() {
        for (TextField t : generalFields)
            t.clear();
        for(TextField t : itemFields)
            t.clear();
        for(TextField t : skillFields)
            t.clear();
        for(Spinner<Integer> s : generalSpinners)
            s.getValueFactory().setValue(0);
    }

    private void throwNameInUseDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Encountered");
        alert.setHeaderText("Name already in use.");
        alert.setContentText("The name you chose is currently in use somewhere else. Please choose a different name.");
        alert.showAndWait();
    }
}
