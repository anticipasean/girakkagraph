package io.github.anticipasean.girakkagraph.modifact.generation.backend.jdbc.wiring;

import cyclops.control.Try;
import io.github.anticipasean.girakkagraph.modifact.generation.DevelopmentStage;
import io.github.anticipasean.girakkagraph.modifact.generation.Modifact;
import io.github.anticipasean.girakkagraph.modifact.generation.backend.BackendWiring;
import io.github.anticipasean.girakkagraph.modifact.generation.stage.BackendWiringStage;
import io.github.anticipasean.girakkagraph.modifact.generation.stage.JavaSourceStageImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.hibernate.boot.Metadata;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.tool.api.reveng.RevengStrategy;
import org.hibernate.tool.internal.reveng.RevengMetadataBuilder;
import org.hibernate.tool.internal.reveng.strategy.DefaultStrategy;
import org.hibernate.tool.internal.reveng.strategy.OverrideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum SqlDatabaseBackendWiring implements BackendWiring {
  INSTANCE;
  private final Logger logger;

  SqlDatabaseBackendWiring() {
    this.logger = LoggerFactory.getLogger(SqlDatabaseBackendWiring.class);
  }

  public static SqlDatabaseBackendWiring getInstance() {
    return INSTANCE;
  }

  @Override
  public DevelopmentStage<Modifact> apply(BackendWiringStage backendWiringStage) {
    logger.info("sql_database_backend_wiring: " + backendWiringStage.configuration());
    RevengStrategy reverseEngineeringStrategy =
        createHibernateToolsReverseEngineeringStrategy(backendWiringStage);
    Properties configurationProperties =
        getHibernatePropertiesFromConfigurationInBackendWiringStage(backendWiringStage);
    if (configurationProperties.isEmpty()) {
      return DevelopmentStage.stop();
    }
    logger.info("persistence_vendor_properties loaded: " + configurationProperties.toString());
    RevengMetadataBuilder revengMetadataBuilder =
        RevengMetadataBuilder.create(configurationProperties, reverseEngineeringStrategy);
    Try<Metadata, NullPointerException> metadataMaybe =
        Try.withCatch(revengMetadataBuilder::build, NullPointerException.class);
    logMetadataBuiltIfSuccessful(metadataMaybe);
    if (metadataMaybe.isFailure()) {
      logMetadataRetrievalFailure(metadataMaybe);
      return DevelopmentStage.stop();
    }
    return DevelopmentStage.stage(
        () ->
            JavaSourceStageImpl.builder()
                .configuration(backendWiringStage.configuration())
                .putAllTypeNameToSdlTypeDefinition(backendWiringStage.typeNameToSdlTypeDefinition())
                .graphQlSchemaFile(backendWiringStage.graphQlSchemaFile())
                .hibernateMetadata(metadataMaybe.orElse(null))
                .build()
                .nextDevelopmentStage());
  }

  private Properties getHibernatePropertiesFromConfigurationInBackendWiringStage(
      BackendWiringStage backendWiringStage) {
    Properties configurationProperties = new Properties();
    if (backendWiringStage.configuration().persistenceVendorProperties().isPresent()) {
      Properties propertiesInConfiguration =
          backendWiringStage.configuration().persistenceVendorProperties().get();
      propertiesInConfiguration.forEach(configurationProperties::put);
    } else if (backendWiringStage.configuration().persistenceVendorPropertiesPath().isPresent()) {
      loadPersistenceVendorSpecificPropertiesFromFileIfConfigPresent(
          backendWiringStage, configurationProperties);
    }
    return configurationProperties;
  }

  private RevengStrategy createHibernateToolsReverseEngineeringStrategy(
      BackendWiringStage backendWiringStage) {
    OverrideRepository overrideRepository = new OverrideRepository();
    backendWiringStage
        .configuration()
        .schemaSelections()
        .forEach(overrideRepository::addSchemaSelection);
    return overrideRepository.getReverseEngineeringStrategy(new DefaultStrategy());
  }

  private void logMetadataBuiltIfSuccessful(Try<Metadata, NullPointerException> metadataMaybe) {
    logger.info(
        "metadata built: [\n\t"
            + metadataMaybe.stream()
                .map(Metadata::getEntityBindings)
                .flatMap(Collection::parallelStream)
                .map(PersistentClass::getClassName)
                .collect(Collectors.joining(",\n\t"))
            + "\n]");
  }

  private void logMetadataRetrievalFailure(Try<Metadata, NullPointerException> metadataMaybe) {
    logger.error(
        "an null value was used when processing the "
            + "metadata in the upstream hibernate-tools code: "
            + metadataMaybe
                .toFailedStream()
                .map(Throwable::getMessage)
                .collect(Collectors.joining(",")));
  }

  private Try<Boolean, IOException> loadPersistenceVendorSpecificPropertiesFromFileIfConfigPresent(
      BackendWiringStage backendWiringStage, Properties configurationProperties) {
    if (backendWiringStage.configuration().persistenceVendorPropertiesPath().isPresent()) {
      File persistenceVendorPropertiesFile =
          new File(backendWiringStage.configuration().persistenceVendorPropertiesPath().get());
      if (persistenceVendorPropertiesFile.exists()) {
        return Try.withResources(
                () -> new FileInputStream(persistenceVendorPropertiesFile),
                fileInputStream -> {
                  configurationProperties.load(fileInputStream);
                  return Boolean.TRUE;
                },
                IOException.class,
                FileNotFoundException.class)
            .onFail(logIOExceptionInBackendWiringStage(backendWiringStage));
      }
    }
    return Try.success(Boolean.FALSE);
  }

  private Consumer<IOException> logIOExceptionInBackendWiringStage(
      BackendWiringStage backendWiringStage) {
    return e ->
        logger.error(
            String.format(
                "unable to load properties from file: [ %s ]",
                backendWiringStage.configuration().persistenceVendorPropertiesPath()),
            e);
  }
}
