package io.github.anticipasean.girakkagraph.protocol.model.domain.arity;

public interface Nullary extends ArityRestricted {

  default Arity arity() {
    return Arity.NULLARY;
  }
}
