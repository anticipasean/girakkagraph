package io.github.anticipasean.girakkagraph.protocol.base.exception;

import akka.actor.ActorInitializationException;
import akka.actor.typed.ActorRef;
import akka.actor.typed.internal.adapter.ActorRefAdapter;
import java.util.function.BiFunction;

public class ProtocolInitializationException extends ActorInitializationException {

  public <T> ProtocolInitializationException(
      ActorRef<T> actorRef, String message, Throwable cause) {
    super(ActorRefAdapter.toClassic(actorRef), message, cause);
  }

  public <T> ProtocolInitializationException(ActorRef<T> actorRef) {
    this(
        actorRef,
        String.format("unable to initialize actor [ %s ]", actorRef),
        new IllegalStateException());
  }

  public <T> ProtocolInitializationException(ActorRef<T> actorRef, Throwable cause) {
    this(
        actorRef,
        String.format(
            "unable to initialize actor [ %s ] due to a [ %s ]",
            actorRef, cause.getCause().getClass().getSimpleName()),
        cause);
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public <T> ActorRef<T> getTypedActorRef() {
    return ActorRefAdapter.apply(super.getActor());
  }

  public static class Builder<T> {
    private ActorRef<T> actorRef;
    private Throwable cause = new IllegalStateException("unable to initialize actor");
    private BiFunction<ActorRef<T>, Throwable, String> messageSupplier =
        (ref, throwable) ->
            String.format(
                "unable to initialize actor [ %s ] due to a [ %s ]",
                ref, throwable.getCause().getClass().getSimpleName());
    private String message;

    public Builder() {}

    public Throwable build() {
      if (message == null) {
        message = messageSupplier.apply(actorRef, cause);
      }
      return new ProtocolInitializationException(actorRef, message, cause);
    }

    public Builder<T> actorRef(ActorRef<T> actorRef) {
      this.actorRef = actorRef;
      return this;
    }

    public Builder<T> message(String message) {
      this.message = message;
      return this;
    }

    public Builder<T> cause(Throwable cause) {
      this.cause = cause;
      return this;
    }
  }
}
