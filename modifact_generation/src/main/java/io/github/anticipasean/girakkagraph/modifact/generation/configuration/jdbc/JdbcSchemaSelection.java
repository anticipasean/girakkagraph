package io.github.anticipasean.girakkagraph.modifact.generation.configuration.jdbc;

import org.hibernate.tool.api.reveng.RevengStrategy.SchemaSelection;
import org.immutables.value.Value;

@Value.Immutable
public interface JdbcSchemaSelection extends SchemaSelection {}
