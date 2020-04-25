package io.github.anticipasean.girakkagraph.modifact.generation;

import cyclops.control.Option;

public interface ModifactGenerator {

  Option<Modifact> generateModifact();
}
