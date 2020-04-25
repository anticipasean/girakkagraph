package io.github.anticipasean.girakkagraph.protocol.base.util.shareable;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.japi.function.Function4;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.BuildShareables;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.DependenciesRegistered;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareableResultsFound;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareables;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareablesImpl;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.RegisterDependencies;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.ShareablesBuilt;
import io.github.anticipasean.girakkagraph.protocol.base.util.container.DefaultValueContainer;
import io.github.anticipasean.girakkagraph.protocol.base.util.container.ValueContainer;
import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.Dependency;
import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.DependencyImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.lookup.ShareableQueryImpl;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public interface ShareableConfigurable<T extends C, C extends Command> {

  default Behavior<C> onDependencyLookUpResult(LookUpShareableResultsFound command) {
    return Behaviors.same();
  }

  default BiFunction<ActorContext<Command>, BuildShareables, Behavior<C>>
      onBuildDependenciesAction() {
    return (context, buildShareables) -> {
      context.getLog().info("build_dependencies: " + buildShareables);
      return Behaviors.same();
    };
  }

  default BiFunction<ActorContext<Command>, ShareablesBuilt, Behavior<C>>
      onDependenciesBuiltAction() {
    return (context, dependenciesBuilt) -> {
      context.getLog().info("dependencies_built: " + dependenciesBuilt);
      return Behaviors.same();
    };
  }

  default BiFunction<ActorContext<Command>, RegisterDependencies, Behavior<C>>
      onRegisterDependenciesAction() {
    return (context, registerDependencies) -> {
      context.getLog().info("register_dependencies_action: " + registerDependencies);
      return Behaviors.same();
    };
  }

  default BiFunction<ActorContext<Command>, DependenciesRegistered, Behavior<C>>
      onDependenciesRegisteredAction() {
    return (context, dependenciesRegistered) -> {
      context.getLog().info("dependencies_registered_action: " + dependenciesRegistered);
      return Behaviors.same();
    };
  }

  //  default BiFunction<ActorContext<Command>, RegisterDependencies, Behavior<C>>
  //      onRegisterDependenciesAction(ActorRef<RegisterDependencies> lookUpDependenciesServiceRef)
  // {
  //    return (context, registerDependencies) -> {
  //      context.getLog().info("register_dependencies: " + registerDependencies);
  //      SingleCommandResponseDelegate<DependenciesRegistered, RegisterDependencies, Command>
  //          delegate =
  //              SingleCommandResponseDelegateImpl
  //                  .<DependenciesRegistered, RegisterDependencies, Command>builder()
  //                  .contextInWhichDelegateIsToBeCreated(context)
  //                  .generateRequestWithReplyToDelegateRef(
  //                      reqRef ->
  //                          RegisterDependenciesImpl.builder()
  //                              .from(registerDependencies)
  //                              .replyTo(reqRef)
  //                              .build())
  //                  .timeoutOnResponse(Duration.ofSeconds(10))
  //                  .expectedResponseTypeForDelegate(DependenciesRegistered.class)
  //                  .requestRecipientCapableOfRespondingWithExpectedResponseType(
  //                      lookUpDependenciesServiceRef)
  //                  .generateReplyFromDelegateToContextInWhichCreated(
  //                      dependenciesRegistered -> dependenciesRegistered)
  //                  .replyToActorUsingResponseReceivedByDelegate(context.getSelf())
  //                  .build();
  //
  //      return Behaviors.same();
  //    };
  //  }

  default Function4<
          String,
          Class<? extends ValueContainer<?, ?, ?>>,
          Class<?>,
          ActorRef<LookUpShareableResultsFound>,
          LookUpShareables>
      generateLookUpDependencyCommand() {
    return (name, containerType, type, replyTo) -> {
      return LookUpShareablesImpl.builder()
          .addLookUpShareableQueries(ShareableQueryImpl.of(name, containerType, type))
          .replyTo(replyTo)
          .build();
    };
  }

  default Dependency buildDependency(String name, Class<?> type) {
    return buildDependency(name, type, DefaultValueContainer.class, Optional.empty());
  }

  default Dependency buildDependency(
      String name, Class<?> type, Class<? extends ValueContainer> containerType) {
    return buildDependency(name, type, containerType, Optional.empty());
  }

  default Dependency buildDependency(
      String name,
      Class<?> type,
      Class<? extends ValueContainer> containerType,
      Optional<String> fullJavaClassNameWithParamTypes) {
    return DependencyImpl.builder()
        .name(name)
        .type(type)
        .valueContainerType(containerType)
        .fullJavaClassNameWithParameterTypesIfKnown(fullJavaClassNameWithParamTypes)
        .build();
  }

  default <V> Shareable buildShareable(String name, Class<V> valueType, V value) {
    return ShareableImpl.builder()
        .name(name)
        .valueContainer(ValueContainer.buildContainer(value, valueType))
        .build();
  }

  default <E> Shareable buildShareable(String name, List<E> value, Class<E> elementType) {
    return ShareableImpl.builder()
        .name(name)
        .valueContainer(ValueContainer.buildContainer(value, elementType))
        .build();
  }

  default Shareable buildShareable(String name, Class<Object> classReference) {
    return ShareableImpl.builder()
        .name(name)
        .valueContainer(ValueContainer.buildContainer(classReference))
        .build();
  }

  default <R> Shareable buildShareable(String name, ActorRef<R> value, Class<R> replyType) {
    return ShareableImpl.builder()
        .name(name)
        .valueContainer(ValueContainer.buildContainer(value, replyType))
        .build();
  }
}

/*
  default DependencyContainerNode getContainerNode(ListValueContainer container) {
    return new DefaultListContainerNode(container);
  }

  default DependencyContainerNode getContainerNode(ClassValueContainer container) {
    return new DefaultClassContainerNode(container);
  }

  default DependencyContainerNode getContainerNode(ActorRefValueContainer container) {
    return new DefaultActorRefContainerNode(container);
  }

  default DependencyContainerNode getContainerNode(OptionalValueContainer container) {
    return new DefaultSimpleContainerNode(container);
  }

  static interface DependencyContainerNode {

    Class<? extends ValueContainer> containerType();

    void accept(DependencyContainerVisitor visitor);
  }

  static interface DependencyContainerVisitor {

    default void visit(SimpleContainerNode containerNode) {}

    default void visit(ListContainerNode containerNode) {}

    default void visit(ClassContainerNode containerNode) {}

    default void visit(ActorRefContainerNode containerNode) {}
  }

  static interface ListContainerNode extends DependencyContainerNode {
    static <E> ValueContainer buildShareable(List<E> value, Class<E> cls) {
      return ListValueContainerImpl.<E>builder().value(value).type(cls).build();
    }

    default Class<? extends ValueContainer> containerType() {
      return ListValueContainer.class;
    }

    @Override
    default void accept(DependencyContainerVisitor visitor) {
      visitor.visit(this);
    }

    ListValueContainer getListContainer();
  }

  static interface ClassContainerNode extends DependencyContainerNode {
    static ValueContainer buildShareable(Class<Object> classReference) {
      return ClassValueContainerImpl.builder().type(classReference).build();
    }

    default Class<? extends ValueContainer> containerType() {
      return ClassValueContainer.class;
    }

    @Override
    default void accept(DependencyContainerVisitor visitor) {
      visitor.visit(this);
    }

    ClassValueContainer getClassContainer();
  }

  static interface ActorRefContainerNode extends DependencyContainerNode {
    static <R> ValueContainer buildShareable(ActorRef<R> value, Class<R> cls) {
      return ActorRefValueContainerImpl.<R>builder().value(value).type(cls).build();
    }

    default Class<? extends ValueContainer> containerType() {
      return ActorRefValueContainer.class;
    }

    @Override
    default void accept(DependencyContainerVisitor visitor) {
      visitor.visit(this);
    }

    ActorRefValueContainer getActorRefContainer();
  }

  static interface SimpleContainerNode extends DependencyContainerNode {
    static <V> ValueContainer buildShareable(V value, Class<V> cls) {
      return OptionalValueContainerImpl.<V>builder().value(value).type(cls).build();
    }

    default Class<? extends ValueContainer> containerType() {
      return ActorRefValueContainer.class;
    }

    @Override
    default void accept(DependencyContainerVisitor visitor) {
      visitor.visit(this);
    }

    OptionalValueContainer getSimpleValueContainer();
  }

  static class DefaultDependencyContainerVisitor implements DependencyContainerVisitor {

    public DefaultDependencyContainerVisitor() {}
  }

  static class DefaultSimpleContainerNode implements SimpleContainerNode {
    private OptionalValueContainer container;

    public DefaultSimpleContainerNode(OptionalValueContainer container) {
      this.container = container;
    }

    @Override
    public OptionalValueContainer getSimpleValueContainer() {
      return container;
    }
  }

  static class DefaultListContainerNode implements ListContainerNode {
    private ListValueContainer container;

    public DefaultListContainerNode(ListValueContainer container) {
      this.container = container;
    }

    @Override
    public ListValueContainer getListContainer() {
      return container;
    }
  }

  static class DefaultClassContainerNode implements ClassContainerNode {
    private ClassValueContainer container;

    public DefaultClassContainerNode(ClassValueContainer container) {
      this.container = container;
    }

    @Override
    public ClassValueContainer getClassContainer() {
      return container;
    }
  }

  static class DefaultActorRefContainerNode implements ActorRefContainerNode {
    private ActorRefValueContainer container;

    public DefaultActorRefContainerNode(ActorRefValueContainer container) {
      this.container = container;
    }

    @Override
    public ActorRefValueContainer getActorRefContainer() {
      return container;
    }
  }
* */
