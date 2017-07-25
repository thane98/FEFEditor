package fefeditor.data;

import fefeditor.Main;

import java.util.prefs.Preferences;

public class PrefsSingleton {
    private static PrefsSingleton instance;

    private static final String AUTO_COMPLETE_ID = "USE_AUTO_COMPLETE";
    private static final String GAMEDATA_TEXT_PATH = "GAMEDATA_TEXT_PATH";

    private Preferences prefs;
    private boolean autoComplete;
    private String textPath;

    private PrefsSingleton() {
        prefs = Preferences.userRoot().node(Main.class.getName());

        autoComplete = prefs.getBoolean(AUTO_COMPLETE_ID, true);
        textPath = prefs.get(GAMEDATA_TEXT_PATH, "");
    }

    public static PrefsSingleton getInstance() {
        if(instance == null)
            instance = new PrefsSingleton();
        return instance;
    }

    public boolean isAutoComplete() {
        return autoComplete;
    }

    public void setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
        prefs.putBoolean(AUTO_COMPLETE_ID, autoComplete);
    }

    public String getTextPath() {
        return textPath;
    }

    public void setTextPath(String textPath) {
        this.textPath = textPath;
        prefs.put(GAMEDATA_TEXT_PATH, textPath);
    }
}
