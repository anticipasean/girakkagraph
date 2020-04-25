package io.github.anticipasean.girakkagraph.modifact.generation.generator;

import cyclops.control.Option;
import io.github.anticipasean.girakkagraph.modifact.generation.DevelopmentStage;
import io.github.anticipasean.girakkagraph.modifact.generation.Modifact;
import io.github.anticipasean.girakkagraph.modifact.generation.ModifactGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultModifactGenerator implements ModifactGenerator {
  private final DevelopmentStage<Modifact> startingStage;
  private final Logger logger;

  public DefaultModifactGenerator(DevelopmentStage<Modifact> startingStage) {
    this.startingStage = startingStage;
    this.logger = LoggerFactory.getLogger(DefaultModifactGenerator.class);
  }

  @Override
  public Option<Modifact> generateModifact() {
    logger.info("calling startingStage.get");
    Option<Modifact> modifactOption = startingStage.get();
    logger.info("result: " + modifactOption);
    return modifactOption;
  }
}
