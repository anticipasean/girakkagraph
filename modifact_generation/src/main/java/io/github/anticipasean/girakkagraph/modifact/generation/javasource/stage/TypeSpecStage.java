package io.github.anticipasean.girakkagraph.modifact.generation.javasource.stage;

import com.squareup.javapoet.TypeSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.DevelopmentStage;

public interface TypeSpecStage {

  DevelopmentStage<TypeSpec> nextDevelopmentStage();
}
