package io.github.anticipasean.girakkagraph.modifact.generation;

import java.io.File;

public interface Modifact {

  File graphQLSchemaFile();

  File sourceClassesDirectory();

  File compiledClassesDirectory();
}
