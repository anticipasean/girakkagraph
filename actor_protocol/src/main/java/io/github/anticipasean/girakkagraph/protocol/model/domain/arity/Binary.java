package io.github.anticipasean.girakkagraph.protocol.model.domain.arity;

public interface Binary extends ArityRestricted {

  default Arity arity() {
    return Arity.BINARY;
  }
}
