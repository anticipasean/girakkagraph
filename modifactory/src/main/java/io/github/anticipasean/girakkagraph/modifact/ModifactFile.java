package io.github.anticipasean.girakkagraph.modifact;

import java.io.File;
import java.io.IOException;
import java.lang.Runtime.Version;
import java.util.jar.JarFile;

public class ModifactFile extends JarFile {

    public ModifactFile(String name) throws IOException {
        super(name);
    }

    public ModifactFile(String name, boolean verify) throws IOException {
        super(name, verify);
    }

    public ModifactFile(File file) throws IOException {
        super(file);
    }

    public ModifactFile(File file, boolean verify) throws IOException {
        super(file, verify);
    }

    public ModifactFile(File file, boolean verify, int mode) throws IOException {
        super(file, verify, mode);
    }

    public ModifactFile(File file, boolean verify, int mode, Version version) throws IOException {
        super(file, verify, mode, version);
    }
}
