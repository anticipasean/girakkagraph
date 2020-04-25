package io.github.anticipasean.girakkagraph.protocol.graphql.dependencies;

import java.io.File;
import org.immutables.value.Value;

@Value.Immutable
public interface GraphQLDependencies {

  File graphQLSchemaToBeGeneratedFileHandle();
}
