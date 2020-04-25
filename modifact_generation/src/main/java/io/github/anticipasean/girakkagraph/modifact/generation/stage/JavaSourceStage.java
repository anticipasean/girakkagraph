package io.github.anticipasean.girakkagraph.modifact.generation.stage;

import graphql.language.TypeDefinition;
import io.github.anticipasean.girakkagraph.modifact.generation.DevelopmentStage;
import io.github.anticipasean.girakkagraph.modifact.generation.Modifact;
import io.github.anticipasean.girakkagraph.modifact.generation.configuration.GeneratorConfiguration;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.jdbc.HibernateMetadataJavaSourceWiring;
import java.io.File;
import java.util.Map;
import org.hibernate.boot.Metadata;
import org.immutables.value.Value;

@Value.Immutable
public interface JavaSourceStage extends ModifactStage {

  GeneratorConfiguration configuration();

  File graphQlSchemaFile();

  Map<String, TypeDefinition> typeNameToSdlTypeDefinition();

  Metadata hibernateMetadata();

  @Override
  default DevelopmentStage<Modifact> nextDevelopmentStage() {
    if (configuration().persistenceVendorPropertiesPath().isPresent()) {
      return HibernateMetadataJavaSourceWiring.getInstance().apply(this);
    } else {
      return DevelopmentStage.stop();
    }
  }
}
