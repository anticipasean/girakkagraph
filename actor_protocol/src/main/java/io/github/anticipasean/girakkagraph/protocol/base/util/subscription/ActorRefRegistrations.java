package io.github.anticipasean.girakkagraph.protocol.base.util.subscription;

import org.immutables.value.Value;

import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
@Value.Style(
    overshadowImplementation = true,
    typeImmutable = "*Impl",
    depluralize = true,
    depluralizeDictionary = {"registrable:registrables", "commandType:commandTypes"})
public interface ActorRefRegistrations<T> {

  Set<Class<? extends T>> commandTypes();

  @Value.Derived
  default Set<Registrable<? extends T>> registrables() {
    return commandTypes().stream().map(RegistrableImpl::of).collect(Collectors.toSet());
  }
}
