package io.github.anticipasean.girakkagraph.springboot.conf;

import static org.springframework.core.ReactiveTypeDescriptor.multiValue;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.stream.Materializer;
import akka.stream.javadsl.AsPublisher;
import graphql.spring.web.reactive.GraphQLInvocation;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.Initialized;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.StartUp;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.StartUpImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.AkkaSpringDependencyInjector;
import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.AkkaSpringDependencyInjectorExtensionId;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlQueryProtocol;
import io.github.anticipasean.girakkagraph.protocol.supervisor.SupervisorProtocolActor;
import io.github.anticipasean.girakkagraph.springboot.web.controller.AkkaGraphQLInvocation;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.util.Assert;

@Configuration
@Profile({"default", "test"})
@ConditionalOnClass({
  akka.actor.typed.ActorSystem.class,
  akka.stream.javadsl.Source.class,
  graphql.spring.web.reactive.components.GraphQLController.class
})
public class SpringAkkaConfiguration {

  private static final String DEFAULT_ACTOR_SYSTEM_NAME = "girakkagraph";
  private final ActorSystem<Command> actorSystem;
  private final Materializer materializer;
  private final AkkaSpringDependencyInjectorExtensionId akkaSpringDependencyInjectorExtensionId;
  private final Logger logger = LoggerFactory.getLogger(SpringAkkaConfiguration.class);
  private final GraphQLInvocation graphQLInvocation;

  @Autowired
  public SpringAkkaConfiguration(AkkaSpringDependencyInjector akkaSpringDependencyInjector) {
    this.akkaSpringDependencyInjectorExtensionId =
        new AkkaSpringDependencyInjectorExtensionId(akkaSpringDependencyInjector);
    AkkaSpringDependencyInjectorExtensionId.setInstance(
        this.akkaSpringDependencyInjectorExtensionId);
    actorSystem =
        ActorSystem.<Command>create(SupervisorProtocolActor.create(), DEFAULT_ACTOR_SYSTEM_NAME);
    materializer = Materializer.createMaterializer(actorSystem);
    final ReactiveAdapterRegistry registry = ReactiveAdapterRegistry.getSharedInstance();
    registerAdapters(registry);
    startUpRemainingActors(actorSystem);
    graphQLInvocation =
        new AkkaGraphQLInvocation(actorSystem, RegistrableImpl.of(GraphQlQueryProtocol.class));
  }

  @Bean
  @ConditionalOnMissingBean
  public Registrable<GraphQlQueryProtocol> graphQLQueryCommandRegistrable() {
    return RegistrableImpl.of(GraphQlQueryProtocol.class);
  }

  @Bean
  @ConditionalOnMissingBean(name = "actorSystem")
  public ActorSystem<Command> actorSystem() {
    return actorSystem;
  }

  @Bean
  @ConditionalOnMissingBean(name = "materializer")
  public Materializer materializer() {
    return materializer;
  }

  @Bean(name = "graphQLInvocation")
  @ConditionalOnClass(GraphQLInvocation.class)
  public GraphQLInvocation graphQLInvocation() {
    return graphQLInvocation;
  }

  public void startUpRemainingActors(ActorSystem<Command> actorSystem) {
    CompletionStage<Initialized> startUpStage =
        AskPattern.<StartUp, Initialized>ask(
            actorSystem.narrow(),
            actorRef ->
                StartUpImpl.builder().commandId(UUID.randomUUID()).replyTo(actorRef).build(),
            Duration.ofSeconds(30),
            actorSystem.scheduler());
    observeSystemStartUp(startUpStage);
  }

  private void observeSystemStartUp(CompletionStage<Initialized> systemStartedUpStage) {
    CompletableFuture<Object> objectWhenComplete =
        systemStartedUpStage
            .handleAsync(
                (initialized, throwable) -> {
                  if (throwable != null) {
                    return new BeanInitializationException(
                        "Unable to complete the start up of all supervisor actors under the supervisor protocol due to unhandled exception",
                        throwable);
                  } else if (initialized != null) {
                    if (initialized.errorOccurred().isPresent()) {
                      return new IllegalStateException(
                          "Unable to complete the start up of all supervisor actors under the supervisor protocol due to a handled exception",
                          initialized.errorOccurred().get());
                    } else {
                      return initialized;
                    }
                  } else {
                    return new IllegalStateException(
                        "Not sure why both values: throwable and systemInitialized are null");
                  }
                })
            .toCompletableFuture();
    Object completion = objectWhenComplete.join();
    if (completion instanceof Initialized) {
      logger.info("initialization completed successfully with dependencies injected");
      return;
    } else if (completion instanceof Throwable) {
      logger.error("initialization did not complete successfully: " + completion);
      throw new RuntimeException((Throwable) completion);
    }
  }

  private void registerAdapters(ReactiveAdapterRegistry registry) {
    Assert.notNull(registry, "registry must not be null");
    registry.registerReactiveType(
        multiValue(akka.stream.javadsl.Source.class, akka.stream.javadsl.Source::empty),
        source ->
            ((akka.stream.javadsl.Source<?, ?>) source)
                .runWith(
                    akka.stream.javadsl.Sink.asPublisher(AsPublisher.WITH_FANOUT), materializer),
        akka.stream.javadsl.Source::fromPublisher);

    registry.registerReactiveType(
        multiValue(akka.stream.scaladsl.Source.class, akka.stream.scaladsl.Source::empty),
        source ->
            ((akka.stream.scaladsl.Source<?, ?>) source)
                .runWith(akka.stream.scaladsl.Sink.asPublisher(true), materializer),
        akka.stream.scaladsl.Source::fromPublisher);
  }

  private boolean isBlank(String str) {
    return (str == null || str.isEmpty());
  }
}
