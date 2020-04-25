package io.github.anticipasean.girakkagraph.protocol.model.domain.metamodel;

import io.github.anticipasean.girakkagraph.protocol.model.domain.index.attribute.PersistableGraphQLAttributeRepository;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.type.PersistableGraphQLTypeRepository;
import org.immutables.criteria.backend.Backend;
import org.immutables.criteria.inmemory.InMemoryBackend;

public class MetaModelDatabase {
  private final PersistableGraphQLTypeRepository typeRepository;
  private final PersistableGraphQLAttributeRepository attributeRepository;

  public MetaModelDatabase() {
    Backend backendDatabase = new InMemoryBackend();
    this.typeRepository = new PersistableGraphQLTypeRepository(backendDatabase);
    this.attributeRepository = new PersistableGraphQLAttributeRepository(backendDatabase);
  }

  public PersistableGraphQLAttributeRepository getAttributeRepository() {
    return attributeRepository;
  }

  public PersistableGraphQLTypeRepository getTypeRepository() {
    return typeRepository;
  }
}
