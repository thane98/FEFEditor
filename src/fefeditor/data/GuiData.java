package fefeditor.data;

import fefeditor.Main;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Node;
import java.util.ArrayList;
import java.util.List;

public class GuiData {
    private static GuiData instance;

    private Stage stage;
    private Stage workingStage;
    private List<String> baseTypes = new ArrayList<>();
    private List<String> dlcTypes = new ArrayList<>();

    protected GuiData() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(Main.class.getResourceAsStream("data/xml/FileTypes.xml"));
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getDocumentElement().getElementsByTagName("FatesTypes").item(0).getChildNodes();
            for (int x = 0; x < nList.getLength(); x++) {
                if (nList.item(x).getNodeType() == Node.ELEMENT_NODE)
                    baseTypes.add(nList.item(x).getAttributes().getNamedItem("name").getNodeValue());
            }
            nList = doc.getDocumentElement().getElementsByTagName("DLC").item(0).getChildNodes();
            for (int x = 0; x < nList.getLength(); x++) {
                if (nList.item(x).getNodeType() == Node.ELEMENT_NODE)
                    dlcTypes.add(nList.item(x).getAttributes().getNamedItem("name").getNodeValue());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static GuiData getInstance() {
        if (instance == null) {
            instance = new GuiData();
        }
        return instance;
    }

    public List<String> getBaseTypes() {
        return baseTypes;
    }

    public List<String> getDlcTypes() {
        return dlcTypes;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getWorkingStage() {
        return workingStage;
    }

    public void setWorkingStage(Stage workingStage) {
        this.workingStage = workingStage;
    }
}
