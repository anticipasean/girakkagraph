package io.github.anticipasean.girakkagraph.protocol.base.util.subscription;

public interface ActorRefRegistrationConfigurable<T> {

  Registrable<T> registrable();

  ActorRefRegistrations<T> registrations();

  default ActorRefRegistrationsImpl.Builder<T> newRegistrationsBuilder() {
    return ActorRefRegistrationsImpl.<T>builder()
        .addCommandType(registrable().protocolMessageType());
  }

}
