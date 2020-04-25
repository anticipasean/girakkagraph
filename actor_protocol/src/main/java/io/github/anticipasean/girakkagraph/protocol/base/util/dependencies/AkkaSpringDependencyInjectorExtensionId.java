package io.github.anticipasean.girakkagraph.protocol.base.util.dependencies;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.ExtensionId;
import java.util.Optional;

public class AkkaSpringDependencyInjectorExtensionId
    extends ExtensionId<AkkaSpringDependencyInjector> {
  private static Optional<AkkaSpringDependencyInjectorExtensionId> instance = Optional.empty();
  private AkkaSpringDependencyInjector akkaSpringDependencyInjector;

  public AkkaSpringDependencyInjectorExtensionId(
      AkkaSpringDependencyInjector akkaSpringDependencyInjector) {
    this.akkaSpringDependencyInjector = akkaSpringDependencyInjector;
  }

  public static AkkaSpringDependencyInjectorExtensionId getInstance() {
    return instance.orElseThrow(
        () ->
            new IllegalStateException(
                "may not call get instance before setting this instance in configuration"));
  }

  public static void setInstance(AkkaSpringDependencyInjectorExtensionId instance) {
    AkkaSpringDependencyInjectorExtensionId.instance = Optional.of(instance);
  }

  @Override
  public ExtensionId<AkkaSpringDependencyInjector> id() {
    return super.id();
  }

  @Override
  public AkkaSpringDependencyInjector createExtension(ActorSystem<?> system) {
    return akkaSpringDependencyInjector;
  }
}
