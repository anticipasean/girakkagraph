package io.github.anticipasean.girakkagraph.modifact.generation.backend;

import cyclops.control.Option;
import io.github.anticipasean.girakkagraph.modifact.generation.DevelopmentStage;
import io.github.anticipasean.girakkagraph.modifact.generation.Modifact;
import io.github.anticipasean.girakkagraph.modifact.generation.stage.BackendWiringStage;
import java.util.function.Function;

public interface BackendWiring
    extends Function<BackendWiringStage, DevelopmentStage<Modifact>> {}
