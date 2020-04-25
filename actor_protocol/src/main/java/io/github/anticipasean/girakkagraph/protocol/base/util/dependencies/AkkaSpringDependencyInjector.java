package io.github.anticipasean.girakkagraph.protocol.base.util.dependencies;

import akka.actor.typed.Extension;
import io.github.anticipasean.girakkagraph.protocol.graphql.dependencies.GraphQLDependencies;
import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConvention;
import javax.persistence.EntityManager;

public interface AkkaSpringDependencyInjector extends Extension {

  EntityManager getEntityManager();

  EntityNamingConvention<?> getEntityNamingConvention();

  GraphQLDependencies getGraphQLDependencies();
}
