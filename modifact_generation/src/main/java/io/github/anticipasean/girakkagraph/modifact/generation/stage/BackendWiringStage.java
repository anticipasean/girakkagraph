package io.github.anticipasean.girakkagraph.modifact.generation.stage;

import graphql.language.TypeDefinition;
import io.github.anticipasean.girakkagraph.modifact.generation.DevelopmentStage;
import io.github.anticipasean.girakkagraph.modifact.generation.Modifact;
import io.github.anticipasean.girakkagraph.modifact.generation.backend.jdbc.wiring.SqlDatabaseBackendWiring;
import io.github.anticipasean.girakkagraph.modifact.generation.configuration.GeneratorConfiguration;
import java.io.File;
import java.util.Map;
import org.immutables.value.Value;
import org.slf4j.LoggerFactory;

@Value.Immutable
public interface BackendWiringStage extends ModifactStage {

  GeneratorConfiguration configuration();

  File graphQlSchemaFile();

  Map<String, TypeDefinition> typeNameToSdlTypeDefinition();

  @Override
  default DevelopmentStage<Modifact> nextDevelopmentStage() {
    LoggerFactory.getLogger(BackendWiringStage.class).info("backend_wiring_stage: " + this);
    if (configuration().persistenceVendorProperties().isPresent()
        || configuration().persistenceVendorPropertiesPath().isPresent()) {
      return SqlDatabaseBackendWiring.getInstance().apply(this);
    } else {
      return DevelopmentStage.stop();
    }
  }
}
