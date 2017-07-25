package fefeditor.data;

import java.io.File;
import java.io.IOException;

public class FileData {
    private static FileData instance;

    private File work;
    private File temp;
    private File templates;
    private File modules;

    private File original;
    private File workingFile;

    protected FileData() {
        work = new File(System.getProperty("user.dir"));
        temp = new File(work + "/temp/");
        temp.mkdir();
        try {
            temp.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        templates = new File(work + "/templates/");
        modules = new File(work + "/modules/");
    }

    public static FileData getInstance() {
        if (instance == null)
            instance = new FileData();
        return instance;
    }

    public File getWork() {
        return work;
    }

    public File getTemp() {
        return temp;
    }

    public File getWorkingFile() {
        return workingFile;
    }

    public void setWorkingFile(File workingFile) {
        this.workingFile = workingFile;
    }

    public File getTemplates() {
        return templates;
    }

    public void setTemplates(File templates) {
        this.templates = templates;
    }

    public File getOriginal() {
        return original;
    }

    public void setOriginal(File original) {
        this.original = original;
    }

    public File getModules() {
        return modules;
    }
}
