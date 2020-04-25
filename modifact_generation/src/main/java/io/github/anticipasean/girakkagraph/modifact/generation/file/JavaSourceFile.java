package io.github.anticipasean.girakkagraph.modifact.generation.file;

import java.io.File;
import org.immutables.value.Value;

@Value.Immutable
public interface JavaSourceFile {

  default String className() {
    return fileHandle().getName();
  }

  default String fileExtension() {
    return ".java";
  }

  File fileHandle();
}
