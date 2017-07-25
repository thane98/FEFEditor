package fefeditor.common;

import fefeditor.common.io.BinFile;
import fefeditor.common.io.CompressionUtils;
import fefeditor.data.FileData;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDialogs {
    /**
     * Open a standard FEF bin file and set the working file to a
     * temporary copy of it.
     *
     * @param stage The stage to be passed to the dialog.
     * @return Whether or not the user selected a valid file.
     */
    public static boolean openBin(Stage stage) {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter bothFilter = new FileChooser.ExtensionFilter("Data Files (*.bin.lz, *.bin)",
                "*.bin.lz", "*.bin");
        FileChooser.ExtensionFilter lzFilter = new FileChooser.ExtensionFilter("Compressed Bin File (*.bin.lz)",
                "*.bin.lz");
        FileChooser.ExtensionFilter binFilter = new FileChooser.ExtensionFilter("Bin File (*.bin)",
                "*.bin");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All Files",
                "*");
        chooser.getExtensionFilters().addAll(bothFilter, lzFilter, binFilter, allFilter);
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            FileData.getInstance().setOriginal(file);
            try {
                File workingFile = File.createTempFile("FEFWORKING", null, FileData.getInstance().getTemp());
                FileData.getInstance().setWorkingFile(workingFile);
                if (file.getName().endsWith("lz")) {
                    byte[] decompressed = CompressionUtils.decompress(file);
                    Files.write(Paths.get(workingFile.getCanonicalPath()), decompressed);
                } else {
                    Files.write(Paths.get(workingFile.getCanonicalPath()), Files.readAllBytes(Paths.get(file.getCanonicalPath())));
                }
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public static void saveStandardBin(Stage stage, BinFile bin) {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter lzFilter = new FileChooser.ExtensionFilter("Compressed Bin File (*.bin.lz)",
                "*.bin.lz");
        FileChooser.ExtensionFilter binFilter = new FileChooser.ExtensionFilter("Bin File (*.bin)",
                "*.bin");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All Files",
                "*");
        chooser.getExtensionFilters().addAll(lzFilter, binFilter, allFilter);
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                if (file.getName().endsWith("lz")) {
                    byte[] compressed = CompressionUtils.compress(bin.toByteArray());
                    Path path = Paths.get(file.getCanonicalPath());
                    Files.write(path, compressed);
                } else
                    Files.write(Paths.get(file.getCanonicalPath()), bin.toByteArray());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void saveFile(Stage stage, byte[] bytes) {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter lzFilter = new FileChooser.ExtensionFilter("Compressed Bin File (*.bin.lz)",
                "*.bin.lz");
        FileChooser.ExtensionFilter binFilter = new FileChooser.ExtensionFilter("Bin File (*.bin)",
                "*.bin");
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All Files",
                "*");
        chooser.getExtensionFilters().addAll(lzFilter, binFilter, allFilter);
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                if (file.getName().endsWith("lz")) {
                    byte[] compressed = CompressionUtils.compress(bytes);
                    Path path = Paths.get(file.getCanonicalPath());
                    Files.write(path, compressed);
                } else
                    Files.write(Paths.get(file.getCanonicalPath()), bytes);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
