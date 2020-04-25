package io.github.anticipasean.girakkagraph.protocol.model.domain.arity;

public interface Unary extends ArityRestricted {

  default Arity arity() {
    return Arity.UNARY;
  }
}
