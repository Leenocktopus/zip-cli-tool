package com.leandoer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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

    private BiConsumer<Path, Path[]> function;
    private Path archiveName;
    private Path[] filenames;

    public BiConsumer<Path, Path[]> getFunction() {
        return function;
    }

    public void setFunction(BiConsumer<Path, Path[]> function) {
        this.function = function;
    }

    public Path getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(Path archiveName) {
        this.archiveName = archiveName;
    }

    public Path[] getFilenames() {
        return filenames;
    }

    public void setFilenames(Path[] filenames) {
        this.filenames = filenames;
    }
}
