package fefeditor.gui.controllers.awakening;

import fefeditor.common.FileDialogs;
import fefeditor.data.FileData;
import fefeditor.data.GuiData;
import feflib.awakening.data.dispo.ADispoBlock;
import feflib.awakening.data.dispo.ADispoFaction;
import feflib.awakening.data.dispo.AwakeningDispo;
import feflib.controls.HexField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Dispo implements Initializable {
    @FXML private TreeView<String> factionTree;
    @FXML private VBox generalBox;
    @FXML private VBox itemBox;
    @FXML private AnchorPane dispoPane;
    @FXML private CheckMenuItem coordCheck;
    @FXML private GridPane dispoGrid;
    @FXML private ScrollPane dispoScrollPane;

    private List<TextField> generalFields = new ArrayList<>();
    private List<HexField> generalHexFields = new ArrayList<>();
    private List<TextField> itemFields = new ArrayList<>();
    private List<HexField> itemHexFields = new ArrayList<>();
    private Region[][] dispoRegion;
    private AwakeningDispo file;
    private ADispoFaction selectedFaction;
    private ADispoBlock selectedBlock;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        GuiData.getInstance().getStage().setOnCloseRequest(we -> close());
        file = new AwakeningDispo(FileData.getInstance().getWorkingFile());
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
                for (ADispoFaction f : file.getFactions()) {
                    if (f.getName().equals(str)) {
                        throwNameInUseDialog();
                        return;
                    }
                }
                selectedBlock = null;
                ADispoBlock block = new ADispoBlock(str);
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
        ADispoBlock block = new ADispoBlock(selectedBlock);
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
            for (ADispoFaction f : file.getFactions()) {
                for (ADispoBlock b : f.getSpawns()) {
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
            ADispoFaction faction = new ADispoFaction(str);
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

    private void selectFaction(ADispoFaction faction) {
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
        for (ADispoFaction f : file.getFactions()) {
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
        generalBox.getChildren().add(new Label("Coordinate 1:"));
        generalBox.getChildren().add(new HexField(2, true));
        generalHexFields.add((HexField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("Coordinate 2:"));
        generalBox.getChildren().add(new HexField(2, true));
        generalHexFields.add((HexField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("Unknown:"));
        generalBox.getChildren().add(new HexField(4, true));
        generalHexFields.add((HexField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        generalBox.getChildren().add(new Label("Team and Level:"));
        generalBox.getChildren().add(new HexField(2, true));
        generalHexFields.add((HexField) generalBox.getChildren().get(generalBox.getChildren().size() - 1));
        
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
        addListeners();
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
                selectedBlock.setAcParam(newValue);
        });
        generalFields.get(3).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setMi(newValue);
        });
        generalFields.get(4).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setMiParam(newValue);
        });
        generalFields.get(5).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setAt(newValue);
        });
        generalFields.get(6).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setAtParam(newValue);
        });
        generalFields.get(7).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setMv(newValue);
        });
        generalFields.get(8).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setMvParam(newValue);
        });
        generalHexFields.get(0).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null) {
                selectedBlock.setCoordOne(generalHexFields.get(0).getValue());
                updateSelection(factionTree.getSelectionModel().getSelectedItem());
            }
        });
        generalHexFields.get(1).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null) {
                selectedBlock.setCoordTwo(generalHexFields.get(1).getValue());
                updateSelection(factionTree.getSelectionModel().getSelectedItem());
            }
        });
        generalHexFields.get(2).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setUnknown(generalHexFields.get(2).getValue());
        });
        generalHexFields.get(3).textProperty().addListener((observable, oldValue, newValue) -> {
            if(selectedBlock != null)
                selectedBlock.setUnknownTwo(generalHexFields.get(3).getValue());
        });

        for(int x = 0; x < 5; x++) {
            int i = x;
            itemFields.get(x).textProperty().addListener((observable, oldValue, newValue) -> {
                if(selectedBlock != null)
                    selectedBlock.setItem(i, newValue);
            });
            itemHexFields.get(x).textProperty().addListener((observable, oldValue, newValue) -> {
                if(selectedBlock != null)
                    selectedBlock.getItemBitflags()[i] = itemHexFields.get(i).getValue();
            });
        }
    }

    private void updateFields() {
        generalFields.get(0).setText(selectedBlock.getPid());
        generalFields.get(1).setText(selectedBlock.getAc());
        generalFields.get(2).setText(selectedBlock.getAcParam());
        generalFields.get(3).setText(selectedBlock.getMi());
        generalFields.get(4).setText(selectedBlock.getMiParam());
        generalFields.get(5).setText(selectedBlock.getAt());
        generalFields.get(6).setText(selectedBlock.getAtParam());
        generalFields.get(7).setText(selectedBlock.getMv());
        generalFields.get(8).setText(selectedBlock.getMvParam());
        generalHexFields.get(0).setValue(selectedBlock.getCoordOne());
        generalHexFields.get(1).setValue(selectedBlock.getCoordTwo());
        generalHexFields.get(2).setValue(selectedBlock.getUnknown());
        generalHexFields.get(3).setValue(selectedBlock.getUnknownTwo());

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
    }

    private void populateTree() {
        TreeItem<String> rootItem = new TreeItem<>(FileData.getInstance().getOriginal().getName());
        rootItem.setExpanded(true);
        for (ADispoFaction f : file.getFactions()) {
            TreeItem<String> faction = new TreeItem<>(f.getName());
            for (ADispoBlock b : f.getSpawns()) {
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
        for (ADispoFaction f : file.getFactions()) {
            for (ADispoBlock b : f.getSpawns()) {
                int x;
                int y;
                if (coordCheck.isSelected()) {
                    x = b.getCoordTwo()[0];
                    y = b.getCoordTwo()[1];
                } else {
                    x = b.getCoordOne()[0];
                    y = b.getCoordOne()[1];
                }
                if (x > 0 && x < 32 && y > 0 && y < 32)
                    dispoRegion[x][y].setId("occupiedCoord");
            }
        }
    }

    private void updateOccupiedCoords(String factionName) {
        for (Node n : dispoGrid.getChildren())
            n.setId("dispoGrid");
        for (ADispoFaction f : file.getFactions()) {
            if (factionName.equals(f.getName()))
                selectedFaction = f;
            for (ADispoBlock b : f.getSpawns()) {
                int x;
                int y;
                if (coordCheck.isSelected()) {
                    x = b.getCoordTwo()[0];
                    y = b.getCoordTwo()[1];
                } else {
                    x = b.getCoordOne()[0];
                    y = b.getCoordOne()[1];
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
        for (ADispoFaction f : file.getFactions()) {
            if (f.getName().equals(faction.getValue()))
                factionIndex = file.getFactions().indexOf(f);
        }
        int blockIndex = faction.getChildren().indexOf(spawn);

        ADispoBlock block = file.getFactions().get(factionIndex).getSpawns().get(blockIndex);

        if (coordCheck.isSelected()) {
            if (block.getCoordTwo()[0] > 0 && block.getCoordTwo()[1] < 32 && block.getCoordTwo()[1] > 0 
                    && block.getCoordTwo()[1] < 32)
                dispoRegion[block.getCoordTwo()[0]][block.getCoordTwo()[1]].setId("selectedBlock");
        } else {
            if (block.getCoordOne()[0] > 0 && block.getCoordOne()[1] < 32 && block.getCoordOne()[1] > 0
                    && block.getCoordOne()[1] < 32)
                dispoRegion[block.getCoordOne()[0]][block.getCoordOne()[1]].setId("selectedBlock");
        }

        selectedBlock = block;
    }

    private void moveBlock(int x, int y) {
        if (selectedBlock != null && selectedFaction != null) {
            if (coordCheck.isSelected())
                selectedBlock.setCoordTwo(new byte[]{(byte) x, (byte) y});
            else
                selectedBlock.setCoordOne(new byte[]{(byte) x, (byte) y});

            // Prompt the tree to reload the selection at the new coordinate.
            updateSelection(factionTree.getSelectionModel().getSelectedItem());
        }
    }

    private void clearFields() {
        for (TextField t : generalFields)
            t.clear();
        for(TextField t : itemFields)
            t.clear();
    }

    private void throwNameInUseDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Encountered");
        alert.setHeaderText("Name already in use.");
        alert.setContentText("The name you chose is currently in use somewhere else. Please choose a different name.");
        alert.showAndWait();
    }
}
