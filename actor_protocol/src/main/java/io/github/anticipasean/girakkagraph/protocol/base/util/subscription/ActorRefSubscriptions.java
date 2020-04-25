package io.github.anticipasean.girakkagraph.protocol.base.util.subscription;

import java.util.Set;
import java.util.stream.Collectors;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(
    typeImmutable = "*Impl",
    depluralize = true,
    depluralizeDictionary = {"subscription:subscriptions", "commandType:commandTypes"})
public interface ActorRefSubscriptions {

  Set<Class<?>> commandTypes();

  @Value.Derived
  default Set<Registrable<?>> registrables() {
    return commandTypes().stream().map(RegistrableImpl::of).collect(Collectors.toSet());
  }
}
