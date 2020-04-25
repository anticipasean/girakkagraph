package io.github.anticipasean.girakkagraph.modifact.generation.configuration;

import cyclops.reactive.IO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import org.hibernate.tool.api.reveng.RevengStrategy.SchemaSelection;
import org.immutables.value.Value;

@Value.Immutable
public interface GeneratorConfiguration {

  @Value.Default
  default String graphQlSchemaFileExtension() {
    return ".graphqls";
  }

  @Value.Default
  default String modelName() {
    return "defaultModelName";
  }

  @Value.Default
  default String version() {
    return "1.0-SNAPSHOT";
  }

  Optional<String> existingModifactPath();

  Optional<String> modelGraphQlSchemaPath();

  Optional<String> persistenceVendorPropertiesPath();

  Optional<Properties> persistenceVendorProperties();

  List<SchemaSelection> schemaSelections();

  @Value.Default
  default IO<Path> outputDirectory(){
      return IO.withCatch(() -> Files.createTempDirectory("javaSrc"));
  }
}
