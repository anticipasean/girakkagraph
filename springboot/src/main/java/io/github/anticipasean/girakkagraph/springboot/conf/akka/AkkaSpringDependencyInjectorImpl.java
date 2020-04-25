package io.github.anticipasean.girakkagraph.springboot.conf.akka;

import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.AkkaSpringDependencyInjector;
import io.github.anticipasean.girakkagraph.protocol.graphql.dependencies.GraphQLDependencies;
import io.github.anticipasean.girakkagraph.protocol.graphql.dependencies.GraphQLDependenciesImpl;
import io.github.anticipasean.girakkagraph.springboot.conf.PersistenceConfigurationProperties;
import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConvention;
import java.io.File;
import java.io.IOException;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class AkkaSpringDependencyInjectorImpl implements AkkaSpringDependencyInjector {
  private EntityManager entityManager;
  private EntityNamingConvention<?> entityNamingConvention;

  @Autowired
  public AkkaSpringDependencyInjectorImpl(
      EntityManager entityManager,
      PersistenceConfigurationProperties persistenceConfigurationProperties) {
    this.entityManager = entityManager;
    this.entityNamingConvention =
        persistenceConfigurationProperties
            .entityNamingConventionFactory()
            .getEntityNamingConventionInstance();
  }

  public static Resource graphQLGeneratedSchemaResource() {
    return new ClassPathResource("graphql/girakkagraph.graphqls");
  }

  @Override
  public EntityManager getEntityManager() {
    return entityManager;
  }

  @Override
  public EntityNamingConvention<?> getEntityNamingConvention() {
    return entityNamingConvention;
  }

  @Override
  public GraphQLDependencies getGraphQLDependencies() {
    File schemaFileToBeGeneratedHandle;
    try {
      schemaFileToBeGeneratedHandle = graphQLGeneratedSchemaResource().getFile();
    } catch (IOException e) {
      throw new RuntimeException("unable to create file at resource location", e);
    }
    return GraphQLDependenciesImpl.builder()
        .graphQLSchemaToBeGeneratedFileHandle(schemaFileToBeGeneratedHandle)
        .build();
  }
}
