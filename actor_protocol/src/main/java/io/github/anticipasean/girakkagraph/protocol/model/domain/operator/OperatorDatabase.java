package io.github.anticipasean.girakkagraph.protocol.model.domain.operator;

import org.immutables.criteria.backend.Backend;
import org.immutables.criteria.inmemory.InMemoryBackend;

public class OperatorDatabase {
  private final ModelOperatorRepository modelOperatorRepository;

  public OperatorDatabase() {
    Backend backend = new InMemoryBackend();
    this.modelOperatorRepository = new ModelOperatorRepository(backend);
  }

  public ModelOperatorRepository getModelOperatorRepository() {
    return modelOperatorRepository;
  }

  public OperatorDatabase copyOf() {
    OperatorDatabase operatorDatabase = new OperatorDatabase();
    operatorDatabase
        .getModelOperatorRepository()
        .insertAll(modelOperatorRepository.findAll().fetch());
    return operatorDatabase;
  }
}
