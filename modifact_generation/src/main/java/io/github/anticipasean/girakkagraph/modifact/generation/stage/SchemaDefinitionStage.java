package io.github.anticipasean.girakkagraph.modifact.generation.stage;

import cyclops.control.Option;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.SchemaPrinter;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;
import io.github.anticipasean.girakkagraph.modifact.generation.DevelopmentStage;
import io.github.anticipasean.girakkagraph.modifact.generation.Modifact;
import io.github.anticipasean.girakkagraph.modifact.generation.configuration.GeneratorConfiguration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;
import org.immutables.value.Value;
import org.slf4j.LoggerFactory;

@Value.Immutable
public interface SchemaDefinitionStage extends ModifactStage {

  GeneratorConfiguration configuration();

  @Value.Derived
  default String graphQlSchemaFilePath() {
    return graphQlSchemaFile().getAbsolutePath();
  }

  @Value.Derived
  default File graphQlSchemaFile() {
    if (configuration().modelGraphQlSchemaPath().isPresent()) {
      File graphQLSchemaFileFromConfigPath =
          new File(configuration().modelGraphQlSchemaPath().get());
      if (graphQLSchemaFileFromConfigPath.exists()) {
        return graphQLSchemaFileFromConfigPath;
      } else {
        Supplier<String> messageSupplier =
            () ->
                String.format(
                    "the path supplied for the graphql schema [ %s ] does not exist",
                    configuration().modelGraphQlSchemaPath().get());
        throw new IllegalArgumentException(messageSupplier.get(), new FileNotFoundException());
      }
    }
    try {
      return File.createTempFile(
          configuration().modelName(), configuration().graphQlSchemaFileExtension());
    } catch (IOException e) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unable to create temporary graphql file [ %s%s ] for modifact file generation",
                  configuration().modelName(), configuration().graphQlSchemaFileExtension());
      throw new RuntimeException(messageSupplier.get(), e);
    }
  }

  Optional<GraphQLSchema> graphQlSchema();

  @Override
  default DevelopmentStage<Modifact> nextDevelopmentStage() {
    LoggerFactory.getLogger(SchemaDefinitionStage.class).info("schema_definition_stage: " + this);
    if ((graphQlSchemaFile().isFile() && graphQlSchemaFile().length() > 0L)
        || graphQlSchema().isPresent()) {
      SchemaParser schemaParser = new SchemaParser();
      try {
        TypeDefinitionRegistry typeDefinitionRegistry =
            graphQlSchema()
                .map(new SchemaPrinter()::print)
                .map(schemaParser::parse)
                .orElseGet(() -> schemaParser.parse(graphQlSchemaFile()));
        return BackendWiringStageImpl.builder()
            .graphQlSchemaFile(graphQlSchemaFile())
            .configuration(configuration())
            .putAllTypeNameToSdlTypeDefinition(typeDefinitionRegistry.types())
            .build()
            .nextDevelopmentStage();
      } catch (SchemaProblem schemaProblem) {
        Supplier<String> messageSupplier =
            () ->
                String.format(
                    "graphql schema parser was unable to deduce "
                        + "the type definition registry from file: [ %s ]",
                    graphQlSchemaFile().getAbsolutePath());
        throw new IllegalStateException(messageSupplier.get(), schemaProblem);
      }
    } else {
      return BackendWiringStageImpl.builder()
          .graphQlSchemaFile(graphQlSchemaFile())
          .configuration(configuration())
          .build()
          .nextDevelopmentStage();
    }
  }
}
