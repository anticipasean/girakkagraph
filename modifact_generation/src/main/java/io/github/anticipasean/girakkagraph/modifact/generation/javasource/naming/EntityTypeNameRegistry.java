package io.github.anticipasean.girakkagraph.modifact.generation.javasource.naming;

import cyclops.data.ImmutableMap;

public interface EntityTypeNameRegistry {

  ImmutableMap<String, EntityTypeName> mappedNamedTypes();
}
