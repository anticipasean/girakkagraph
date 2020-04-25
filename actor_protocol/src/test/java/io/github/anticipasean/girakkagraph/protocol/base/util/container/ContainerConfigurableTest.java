package io.github.anticipasean.girakkagraph.protocol.base.util.container;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorRefResolver;
import akka.actor.typed.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.google.common.collect.Maps;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.supervisor.SupervisorProtocolActor;
import java.util.Map;

class ContainerConfigurableTest {

  private ActorSystem<Command> actorSystem;
  private TestKit actorTestKit;
  private ActorRefResolver actorRefResolver;

  void setUp() {
    actorSystem = ActorSystem.create(SupervisorProtocolActor.create(), "supervisor_test");
    actorTestKit = new TestKit(actorSystem.classicSystem());
  }

  void tearDown() {}

  <T> ActorRef<T> convertToTypedActorRef(akka.actor.ActorRef actorRefUntyped) {
    if (actorRefResolver == null) {
      actorRefResolver = ActorRefResolver.createExtension(actorSystem);
    }
    return actorRefResolver.resolveActorRef(actorRefUntyped.path().toSerializationFormat());
  }

  <T> ActorRef<T> getTestRefAsTypedActorRef() {
    return convertToTypedActorRef(actorTestKit.getRef());
  }

  static class Key<T, U> {
    private String id;
    private Class<T> containerType;
    private Class<U> parameterType;

    Key(String id, Class<T> containerType, Class<U> parameterType) {
      this.id = id;
      this.containerType = containerType;
      this.parameterType = parameterType;
    }
  }

  static class ActorRefHolder {

    private Map<Key<?, ?>, Object> container;
    private Key<?, ?> key;

    ActorRefHolder() {
      this.container = Maps.newHashMap();
    }

    public <R> void put(Class<R> commandType, ActorRef<R> actorRef) {
      this.key = new Key<>("my_cmd", ActorRef.class, commandType);
      this.container.put(key, actorRef);
    }

    public <R> ActorRef<R> getActorRef(ActorSystem<?> actorSystem, Class<?> command) {
      if (command.isAssignableFrom(key.parameterType)
          && key.containerType.isAssignableFrom(ActorRef.class)) {
        ActorRef actorRef = (ActorRef) key.containerType.cast(container.get(key));
        return ActorRefResolver.get(actorSystem)
            .resolveActorRef(actorRef.path().toSerializationFormat());
      }
      return null;
    }
  }
}
