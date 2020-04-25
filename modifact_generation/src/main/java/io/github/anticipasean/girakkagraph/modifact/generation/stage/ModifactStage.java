package io.github.anticipasean.girakkagraph.modifact.generation.stage;

import io.github.anticipasean.girakkagraph.modifact.generation.DevelopmentStage;
import io.github.anticipasean.girakkagraph.modifact.generation.Modifact;

public interface ModifactStage {

  DevelopmentStage<Modifact> nextDevelopmentStage();
}
