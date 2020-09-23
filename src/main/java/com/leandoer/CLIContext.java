package com.leandoer;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.logging.LogManager;

public class CLIContext {

    static {
        InputStream stream = ClassLoader.getSystemClassLoader().
                getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private BiConsumer<String, String[]> function;
    private String archiveName;
    private String[] filenames;

    public BiConsumer<String, String[]> getFunction() {
        return function;
    }

    public void setFunction(BiConsumer<String, String[]> function) {
        this.function = function;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public String[] getFilenames() {
        return filenames;
    }

    public void setFilenames(String[] filenames) {
        this.filenames = filenames;
    }
}
