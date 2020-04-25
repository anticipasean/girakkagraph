package io.github.anticipasean.girakkagraph.protocol.graphql.worker.schema;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import graphql.AssertException;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.WiringFactory;
import graphql.schema.validation.InvalidSchemaException;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.AkkaSpringDependencyInjectorExtensionId;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.worker.WorkerActor;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.CreateSchema;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.SchemaCreatedImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.schema.JpaMetaModelGraphQLSchemaExtractor;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.schema.impl.JpaMetaModelGraphQLSchemaExtractorImpl;
import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConvention;
import javax.persistence.EntityManager;

public class GraphQLSchemaSetupWorker extends WorkerActor<GraphQlSchemaProtocol> {

  protected GraphQLSchemaSetupWorker(
      SpawnedContext<Command, GraphQlSchemaProtocol> spawnedContext) {
    super(spawnedContext);
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(
        parentActorRef, GraphQlSchemaProtocol.class, GraphQLSchemaSetupWorker::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(CreateSchema.class, this::onCreateSchema)
        .build();
  }

  private GraphQLSchema generateSchemaFromJpaEntityManager(WiringFactory wiringFactory) {
    logger()
        .info(
            "obtaining entity manager from dependency injector extension: "
                + AkkaSpringDependencyInjectorExtensionId.class.getSimpleName());
    EntityManager entityManager =
        AkkaSpringDependencyInjectorExtensionId.getInstance()
            .createExtension(context.getSystem())
            .getEntityManager();
    EntityNamingConvention<?> entityNamingConvention =
        AkkaSpringDependencyInjectorExtensionId.getInstance()
            .createExtension(context.getSystem())
            .getEntityNamingConvention();
    logger().info("retrieved entity manager: " + (entityManager != null));
    JpaMetaModelGraphQLSchemaExtractor jpaMetaModelGraphQLSchemaExtractor =
        new JpaMetaModelGraphQLSchemaExtractorImpl(entityManager, wiringFactory,
            entityNamingConvention);
    GraphQLSchema graphQLSchemaFromJpaMetaModel =
        jpaMetaModelGraphQLSchemaExtractor.extractGraphQLSchemaFromJpaMetaModel();
    logger()
        .info("schema extracted from jpa metamodel: " + (graphQLSchemaFromJpaMetaModel != null));
    return graphQLSchemaFromJpaMetaModel;
  }

  private Behavior<Command> onCreateSchema(CreateSchema command) {
    context.getLog().info("create_schema received: " + command);
    GraphQLSchema graphQLSchema;
    try {
      graphQLSchema = generateSchemaFromJpaEntityManager(command.wiringFactory());
    } catch (AssertException e) {
      String message =
          "A null value was encountered, likely one of the additional types or additional directives added";
      context.getLog().error(message);
      sendErrorReply(e, command);
      return Behaviors.stopped();
    } catch (InvalidSchemaException e) {
      String message = "GraphQL marked this schema as invalid";
      context.getLog().error(message);
      sendErrorReply(e, command);
      return Behaviors.stopped();
    } catch (Exception e) {
      String message =
          "an error occurred that has not been handled when generating the graphql schema";
      logger().error(message, e);
      sendErrorReply(e, command);
      return Behaviors.stopped();
    }
    replyToIfPresent(
        command,
        SchemaCreatedImpl.builder()
            .commandId(command.commandId())
            .graphQLSchemaResult(graphQLSchema)
            .build());
    return Behaviors.same();
  }

  private void sendErrorReply(Throwable e, CreateSchema command) {
    replyToIfPresent(
        command,
        SchemaCreatedImpl.builder().commandId(command.commandId()).errorOccurred(e).build());
  }
}
