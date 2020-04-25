package io.github.anticipasean.girakkagraph.protocol.base.handler;

import akka.actor.typed.BackoffSupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.handler.event.Event;
import java.util.Optional;

public abstract class EventSourcedProtocolHandler<C extends Command, E extends Event, S>
    extends EventSourcedBehavior<C, E, S> implements ProtocolHandler {

  protected ActorContext<C> context;

  protected EventSourcedProtocolHandler(
      ActorContext<C> context,
      PersistenceId persistenceId,
      Optional<BackoffSupervisorStrategy> onPersistFailure) {
    super(persistenceId, onPersistFailure);
    this.context = context;
    this.context
        .getLog()
        .info("initializing protocol handler: " + context.getSelf().path().toSerializationFormat());
  }

  protected EventSourcedProtocolHandler(ActorContext<C> context, PersistenceId persistenceId) {
    super(persistenceId);
    this.context = context;
    this.context
        .getLog()
        .info("initializing protocol handler: " + context.getSelf().path().toSerializationFormat());
  }

  protected EventSourcedProtocolHandler(
      ActorContext<C> context,
      PersistenceId persistenceId,
      BackoffSupervisorStrategy onPersistFailure) {
    super(persistenceId, onPersistFailure);
    this.context = context;
    this.context
        .getLog()
        .info("initializing protocol handler: " + context.getSelf().path().toSerializationFormat());
  }
}
