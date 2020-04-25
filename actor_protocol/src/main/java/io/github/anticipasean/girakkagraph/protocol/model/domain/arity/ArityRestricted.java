package io.github.anticipasean.girakkagraph.protocol.model.domain.arity;

import org.immutables.value.Value;

@Value.Style(
    overshadowImplementation = true,
    typeImmutable = "*Impl",
    depluralize = true,
    depluralizeDictionary = {"operand:operands"})
public interface ArityRestricted {
  Arity arity();
}
