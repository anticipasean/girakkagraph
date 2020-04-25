package io.github.anticipasean.girakkagraph.protocol.model.domain.arity;

public interface Ternary extends ArityRestricted {

  default Arity arity() {
    return Arity.TERNARY;
  }
}
