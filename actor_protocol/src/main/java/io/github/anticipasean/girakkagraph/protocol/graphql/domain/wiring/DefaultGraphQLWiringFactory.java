package io.github.anticipasean.girakkagraph.protocol.graphql.domain.wiring;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.receptionist.Receptionist;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.typed.javadsl.ActorFlow;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.ProvideDataFetcherFactory;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.ProvideDataFetcherFactoryImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.DataFetcherFactoryProvided;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.scalar.GraphQLScalarsSupport;
import graphql.execution.DataFetcherResult;
import graphql.language.TypeName;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetcherFactory;
import graphql.schema.GraphQLScalarType;
import graphql.schema.TypeResolver;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.InterfaceWiringEnvironment;
import graphql.schema.idl.ScalarWiringEnvironment;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import graphql.schema.idl.UnionWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultGraphQLWiringFactory implements WiringFactory {
  private final ActorSystem<Void> actorSystem;
  private final Registrable<ProvideDataFetcherFactory> dataFetcherFactoryRegistrable;
  private final AtomicReference<ActorRef<ProvideDataFetcherFactory>>
      provideDataFetcherFactoryRefHolder;
  private GraphQLSchemaDirectiveWiringFactory graphQLSchemaDirectiveWiringFactory;
  private Logger logger = LoggerFactory.getLogger(DefaultGraphQLWiringFactory.class);
  private Map<String, GraphQLScalarType> scalarNameToScalarTypeMap;

  private DefaultGraphQLWiringFactory(
      ActorSystem<Void> actorSystem,
      GraphQLSchemaDirectiveWiringFactory graphQLSchemaDirectiveWiringFactory) {
    this.actorSystem = actorSystem;
    this.dataFetcherFactoryRegistrable = RegistrableImpl.of(ProvideDataFetcherFactory.class);
    this.graphQLSchemaDirectiveWiringFactory = graphQLSchemaDirectiveWiringFactory;
    this.provideDataFetcherFactoryRefHolder = new AtomicReference<>();
    this.scalarNameToScalarTypeMap = GraphQLScalarsSupport.allScalarsMap();
  }

  public static WiringFactory getInstanceUsingActorSystemAndGraphQlSchemaDirectiveWiringFactory(
      ActorSystem<Void> actorSystem,
      GraphQLSchemaDirectiveWiringFactory graphQLSchemaDirectiveWiringFactory) {
    return new DefaultGraphQLWiringFactory(actorSystem, graphQLSchemaDirectiveWiringFactory);
  }

  /**
   * This is called to ask if this factory can provide a custom scalar
   *
   * @param environment the wiring environment
   * @return true if the factory can give out a type resolver
   */
  @Override
  public boolean providesScalar(ScalarWiringEnvironment environment) {
    boolean result =
        scalarNameToScalarTypeMap.containsKey(environment.getScalarTypeDefinition().getName())
            && environment
                .getRegistry()
                .hasType(
                    TypeName.newTypeName(environment.getScalarTypeDefinition().getName()).build())
            && GraphQLScalarsSupport.extendedScalarsMap()
                .containsKey(environment.getScalarTypeDefinition().getName());
//    logger.info("provides_scalar: environment: " + environment.getScalarTypeDefinition());
//    logger.info(
//        "provides_scalar: type_registry_entry: current_entry: "
//            + environment
//                .getRegistry()
//                .getType(
//                    environment.getScalarTypeDefinition().getName(), ScalarTypeDefinition.class));
//    logger.info("meets_condition: " + result);
    return result;
  }

  /**
   * Returns a {@link GraphQLScalarType} given scalar defined in IDL
   *
   * @param environment the wiring environment
   * @return a {@link GraphQLScalarType}
   */
  @Override
  public GraphQLScalarType getScalar(ScalarWiringEnvironment environment) {
    logger.info(
        "get_scalar: \nenvironment: \n\ttype_registry.scalars:[\n\t\t"
            + environment.getRegistry().scalars().entrySet().stream()
                .map(entry -> String.join(": ", entry.getKey(), entry.getValue().toString()))
                .collect(Collectors.joining(",\n\t\t"))
            + " ]\n\n\tscalar_definition: "
            + environment.getScalarTypeDefinition().toString()
            + "\n\tproviding_type_with_hashcode: "
            + scalarNameToScalarTypeMap
                .get(environment.getScalarTypeDefinition().getName())
                .hashCode());
    return scalarNameToScalarTypeMap.get(environment.getScalarTypeDefinition().getName());
//    return null;
  }

  /**
   * This is called to ask if this factory can provide a type resolver for the interface
   *
   * @param environment the wiring environment
   * @return true if the factory can give out a type resolver
   */
  @Override
  public boolean providesTypeResolver(InterfaceWiringEnvironment environment) {
    return false;
  }

  /**
   * Returns a {@link TypeResolver} given the type interface
   *
   * @param environment the wiring environment
   * @return a {@link TypeResolver}
   */
  @Override
  public TypeResolver getTypeResolver(InterfaceWiringEnvironment environment) {
    return null;
  }

  /**
   * This is called to ask if this factory can provide a type resolver for the union
   *
   * @param environment the wiring environment
   * @return true if the factory can give out a type resolver
   */
  @Override
  public boolean providesTypeResolver(UnionWiringEnvironment environment) {
    return false;
  }

  /**
   * Returns a {@link TypeResolver} given the type union
   *
   * @param environment the union wiring environment
   * @return a {@link TypeResolver}
   */
  @Override
  public TypeResolver getTypeResolver(UnionWiringEnvironment environment) {
    return null;
  }

  /**
   * This is called to ask if this factory can provide a {@link DataFetcherFactory} for the
   * definition
   *
   * @param environment the wiring environment
   * @return true if the factory can give out a data fetcher factory
   */
  @Override
  public boolean providesDataFetcherFactory(FieldWiringEnvironment environment) {
    //    logger.info(
    //        String.format(
    //            "providesDataFetcherFactory called with fieldwiringenv: [%s]",
    //            Optional.of(environment)
    //                .map(
    //                    fieldWiringEnvironment ->
    //                        Stream.of(
    //                                Pair.create("FieldType: ",
    // environment.getFieldType().getName()),
    //                                Pair.create("ParentType: ", environment.getParentType()),
    //                                Pair.create("Directives: ", environment.getDirectives()),
    //                                Pair.create("FieldDefinition:Name: ",
    // environment.getFieldDefinition().getName()))
    //                            .map(
    //                                stringPair ->
    //                                    String.join(
    //                                        ": ",
    //                                        stringPair.first(),
    //                                        String.valueOf(stringPair.second())))
    //                            .collect(Collectors.joining("\n")))
    //                .orElse("")));
    //    return true;
    return true;
  }

  /**
   * Returns a {@link DataFetcherFactory} given the type definition
   *
   * @param environment the wiring environment
   * @return a {@link DataFetcherFactory}
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> DataFetcherFactory<T> getDataFetcherFactory(FieldWiringEnvironment environment) {
    if (!isProvideDataFetcherFactoryRefAvailable()) {
      Optional<ActorRef<ProvideDataFetcherFactory>> provideDataFetcherFactoryActorRefMaybe =
          AskPattern.<Receptionist.Command, Receptionist.Listing>ask(
                  actorSystem.receptionist(),
                  ref -> Receptionist.find(dataFetcherFactoryRegistrable.serviceKey(), ref),
                  Duration.ofSeconds(10),
                  actorSystem.scheduler())
              .thenApply(
                  listing ->
                      listing.getServiceInstances(dataFetcherFactoryRegistrable.serviceKey()))
              .thenApply(actorRefs -> actorRefs.stream().findAny())
              .toCompletableFuture()
              .join();
      if (provideDataFetcherFactoryActorRefMaybe.isPresent()) {
        provideDataFetcherFactoryRefHolder.set(provideDataFetcherFactoryActorRefMaybe.get());
      } else {
        IllegalStateException illegalStateException =
            new IllegalStateException(
                "no listing received or found for graphql query service: "
                    + dataFetcherFactoryRegistrable.serviceKey());
        logger.error(
            "error occurred when retrieving graphql query service ref", illegalStateException);
        throw illegalStateException;
      }
    }

    //    if (Optional.ofNullable(
    //            environment
    //                .getFieldDefinition()
    //                .getType()
    //                .getNamedChildren()
    //                .getChildOrNull("TypeName"))
    //        .flatMap(o -> Optional.ofNullable(((TypeName) o).getName()))
    //        .map(s -> s.equals("Account") || s.equals("AppProperty"))
    //        .orElse(Boolean.FALSE)) {
    //      logger.info(
    //          String.format(
    //              "getDataFetcherFactory called with fieldwiringenv: [%s]",
    //              Optional.of(environment)
    //                  .map(
    //                      fieldWiringEnvironment ->
    //                          Stream.of(
    //                                  Pair.create("FieldType: ", environment.getFieldType()),
    //                                  Pair.create("ParentType: ", environment.getParentType()),
    //                                  Pair.create("Directives: ", environment.getDirectives()),
    //                                  Pair.create(
    //                                      "FieldDefinition: ", environment.getFieldDefinition()))
    //                              .map(
    //                                  stringPair ->
    //                                      String.join(
    //                                          ": ",
    //                                          stringPair.first(),
    //                                          String.valueOf(stringPair.second())))
    //                              .collect(Collectors.joining("\n")))
    //                  .orElse("")));
    //    }
    return (DataFetcherFactory<T>) getDataFetcherFactoryUsingAkkaStreams(environment);
  }

  public boolean isProvideDataFetcherFactoryRefAvailable() {
    return provideDataFetcherFactoryRefHolder.get() != null;
  }

  private DataFetcherFactory<CompletionStage<DataFetcherResult<?>>>
      getDataFetcherFactoryUsingAkkaStreams(FieldWiringEnvironment environment) {
    try {
      return Source.single(
              ProvideDataFetcherFactoryImpl.builder()
                  .commandId(UUID.randomUUID())
                  .fieldWiringEnvironment(environment))
          .via(
              ActorFlow
                  .<ProvideDataFetcherFactoryImpl.Builder, ProvideDataFetcherFactory,
                      DataFetcherFactoryProvided>
                      ask(
                          provideDataFetcherFactoryRefHolder.get(),
                          Duration.ofSeconds(10),
                          (v1, v2) -> v1.replyTo(v2).build()))
          .limit(1)
          .map(DataFetcherFactoryProvided::dataFetcherFactory)
          .runWith(Sink.head(), Materializer.matFromSystem(actorSystem))
          .thenApply(
              completionStageDataFetcherFactory ->
                  completionStageDataFetcherFactory.orElseThrow(
                      () ->
                          new IllegalStateException(
                              "data fetcher factory not provided for querying")))
          .toCompletableFuture()
          .join();
    } catch (Exception e) {
      logger.error(
          "an error occurred when obtaining the data fetcher factory for: "
              + environment.getFieldDefinition(),
          e);
    }
    throw new IllegalStateException("data fetcher factory not provided for querying");
  }

  /**
   * This is called to ask if this factory can provide a schema directive wiring.
   *
   * <p>{@link SchemaDirectiveWiringEnvironment#getDirectives()} contains all the directives
   * available which may in fact be an empty list.
   *
   * @param environment the calling environment
   * @return true if the factory can give out a schema directive wiring.
   */
  @Override
  public boolean providesSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment environment) {
    //            Pair.of("ElementParentTree: ",
    // Optional.ofNullable(environment.getElementParentTree())),
    //            Pair.of("Registry: ", Optional.ofNullable(environment.getRegistry())),
    //            Pair.of("NodeParentTree: ",
    // Optional.ofNullable(environment.getNodeParentTree())))
    //            Pair.of("BuildContext: ",
    // Optional.ofNullable(environment.getBuildContext())),
    //            Pair.of("CodeRegistry: ",
    // Optional.ofNullable(environment.getCodeRegistry())),
    //            Pair.of("FieldDataFetcher: ",
    // Optional.ofNullable(environment.getFieldDataFetcher())),

    //    String schemaEnvLogging =
    //        Stream.of(
    //                Pair.of("Element: ", Optional.ofNullable(environment.getElement())),
    //                Pair.of("Directive: ", Optional.ofNullable(environment.getDirective())),
    //                Pair.of("FieldDefinition: ",
    // Optional.ofNullable(environment.getFieldDefinition())))
    //            .map(pair -> pair.getFirst() + ": " + pair.getSecond().orElse(null))
    //            .collect(Collectors.joining(", "));
    //    logger.info("schemaEnvLogging: " + schemaEnvLogging);
    return graphQLSchemaDirectiveWiringFactory.providesSchemaDirectiveWiring(environment);
  }

  /**
   * Returns a {@link SchemaDirectiveWiring} given the environment
   *
   * @param environment the calling environment
   * @return a {@link SchemaDirectiveWiring}
   */
  @Override
  public SchemaDirectiveWiring getSchemaDirectiveWiring(
      SchemaDirectiveWiringEnvironment environment) {
//    if (environment.getElement() instanceof GraphQLFieldDefinition
//        && ((GraphQLFieldDefinition) environment.getElement()).getType()
//            instanceof GraphQLScalarType
//        && GraphQLScalarsSupport.extendedScalarsMap()
//            .containsKey(
//                ((GraphQLScalarType) ((GraphQLFieldDefinition) environment.getElement()).getType())
//                    .getName())) {
//      logger.info(
//          "get_schema_directive_wiring: extended scalar processing: element: "
//              + environment.getElement().toString());
//      logger.info(
//          "get_schema_directive_wiring: extended scalar processing: element.type.hashCode: "
//              + ((GraphQLFieldDefinition) environment.getElement()).getType().hashCode());
//    }
    return graphQLSchemaDirectiveWiringFactory.getSchemaDirectiveWiring(environment);
  }

  /**
   * This is called to ask if this factory can provide a data fetcher for the definition
   *
   * @param environment the wiring environment
   * @return true if the factory can give out a data fetcher
   */
  @Override
  public boolean providesDataFetcher(FieldWiringEnvironment environment) {
    return false;
  }

  /**
   * Returns a {@link DataFetcher} given the type definition
   *
   * @param environment the wiring environment
   * @return a {@link DataFetcher}
   */
  @Override
  public DataFetcher getDataFetcher(FieldWiringEnvironment environment) {
    return null;
  }

  /**
   * All fields need a data fetcher of some sort and this method is called to provide the data
   * fetcher that will be used if no specific one has been provided
   *
   * @param environment the wiring environment
   * @return a {@link DataFetcher}
   */
  @Override
  public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
    return null;
  }
}

//  private DataFetcherFactory<CompletionStage<DataFetcherResult<?>>>
//      getDataFetcherFactoryUsingAskPattern(FieldWiringEnvironment environment) {
//    Creator<CompletionStage<DataFetcherFactoryProvided>> provideDataFetcherFactoryCommandCreator
// =
//        () ->
//            AskPattern.<GraphQLQueryCommand, DataFetcherFactoryProvided>ask(
//                provideDataFetcherFactoryRefHolder.get(),
//                ref ->
//                    ProvideDataFetcherFactoryImpl.builder()
//                        .commandId(UUID.randomUUID())
//                        .fieldWiringEnvironment(environment)
//                        .replyTo(ref.narrow())
//                        .build(),
//                Duration.ofSeconds(5),
//                actorSystem.scheduler());
//    BiFunction<
//            DataFetcherFactoryProvided,
//            Throwable,
//            DataFetcherFactory<CompletionStage<DataFetcherResult<?>>>>
//        handleAsyncOutputDataFetcherFactoryMaybe =
//            (dataFetcherFactoryProvided, throwable) -> {
//              return Optional.ofNullable(dataFetcherFactoryProvided.dataFetcherFactory())
//                  .map(Optional::get)
//                  .orElseThrow(() -> new RuntimeException(throwable));
//            };
//    CompletionStage<DataFetcherFactory<CompletionStage<DataFetcherResult<?>>>>
//        dataFetcherFactoryFuture =
//            Source.lazyCompletionStage(
//                    () ->
//                        provideDataFetcherFactoryCommandCreator
//                            .create()
//                            .handleAsync(
//                                handleAsyncOutputDataFetcherFactoryMaybe,
//                                actorSystem.executionContext()))
//                .runWith(Sink.head(), actorSystem);
//    return dataFetcherFactoryFuture.toCompletableFuture().join();
//  }

//    Behavior<Receptionist.Listing> listingSubscriptionProtocol =
//        Behaviors.setup(
//            context -> {
//              context.getLog().info("in listing subscription protocol");
//              context.spawn(listingSubscription(), "listingSubscription");
//              return Behaviors.same();
//            });

//    Behavior<SpawnProtocol.Command> listingSubscriptionProtocol =
//        Behaviors.setup(
//            context -> {
//              context.spawn(listingSubscription(), "listingSubscription");
//              return SpawnProtocol.create();
//            });

//    Function<ActorRef<ActorRef<SpawnProtocol.Command>>, SpawnProtocol.Command>
//        listingSubscriptionSpawnFunction =
//            (ActorRef<ActorRef<SpawnProtocol.Command>> ref) -> {
//              return new SpawnProtocol.Spawn<SpawnProtocol.Command>(
//                  listingSubscriptionProtocol,
//                  "wiringFactoryListingSubscription",
//                  Props.empty(),
//                  ref);
//            };

//    Behavior<String> strListener =
//        Behaviors.receive(String.class).onMessage(String.class, this::onTest).build();
//    Behavior<String> testStr =
//        Behaviors.setup(
//            context -> {
//              context
//                  .getLog()
//                  .info("test str started up: " +
// context.getSelf().path().toSerializationFormat());
//              context.spawn(strListener, "strListener");
//              return Behaviors.same();
//            }).unsafeCast();
//    Behavior<SpawnProtocol.Command> test =
//        Behaviors.setup(
//            context -> {
//              context
//                  .getLog()
//                  .info(
//                      "will this get logged: " +
// context.getSelf().path().toSerializationFormat());
//              ActorRef<String> stringActorRef = context.spawn(testStr, "testStrBehavior");
//              return SpawnProtocol.create();
//            });
//
//
//    Function<ActorRef<SpawnProtocol.Command>, SpawnProtocol.Spawn<SpawnProtocol.Command>>
// actorRefSpawnFunction = (ActorRef<SpawnProtocol.Command> ref) ->
//            new SpawnProtocol.Spawn<SpawnProtocol.Command>(
//                    test, "testspawn", Props.empty(), actorSystem.narrow());
//    actorSystem.tell(new SpawnProtocol.Spawn<SpawnProtocol.Command>(test,"test",
// Props.empty(), actorSystem.narrow()));
//    actorSystem.log().info("sent spawn protocol request");
//
//    Function<ActorRef<Receptionist.Listing>, SpawnProtocol.Spawn<Receptionist.Listing>>
//        listingSubscriptionSpawnVoidFunction =
//            (ActorRef<Receptionist.Listing> ref) -> {
//              return new SpawnProtocol.Spawn<Receptionist.Listing>(
//                  listingSubscription(), "listingSubscription", Props.empty(),
// ref.unsafeUpcast());
//            };
//    CompletionStage<ActorRef<Receptionist.Listing>> subscriptionSpawnProtocol =
//        AskPattern.ask(
//            actorSystem,
//            param -> listingSubscriptionSpawnVoidFunction.apply(param.unsafeUpcast()),
//            Duration.ofSeconds(4),
//            actorSystem.scheduler());
//    ActorRef<Receptionist.Listing> ref =
// subscriptionSpawnProtocol.toCompletableFuture().join();
//    actorSystem.log().info("successfully subscribed to receptionist: " + ref);
//
//  private Behavior<Receptionist.Listing> listingSubscription() {
//    return Behaviors.setup(
//            (ActorContext<Receptionist.Listing> context) -> {
//              //          context
//              //              .getSystem()
//              //              .receptionist()
//              //              .tell(Receptionist.find(queryGraphQLServiceKey, context.getSelf()));
//              context.getLog().info("submitted find request for graphql query service listing");
//              Function<ActorRef<Receptionist.Listing>, Receptionist.Command>
// actorRefCommandFunction =
//                      (ref) -> Receptionist.find(queryGraphQLServiceKey, ref);
//              context.<Receptionist.Command, Receptionist.Listing>ask(
//                      Receptionist.Listing.class,
//                      context.getSystem().receptionist(),
//                      Duration.ofSeconds(4),
//                      actorRefCommandFunction,
//                      (listing, throwable) ->
//                              Optional.ofNullable(listing).orElseThrow(() -> new
// RuntimeException(throwable)));
//              //          context
//              //              .getSystem()
//              //              .receptionist()
//              //              .tell(Receptionist.subscribe(queryGraphQLServiceKey,
//              // context.getSelf().narrow()));
//              //          context.getLog().info("subscribed to receptionist.listings for graphql
// query
//              // service");
//              return Behaviors.receive(Receptionist.Listing.class)
//                      .onMessage(
//                              Receptionist.Listing.class,
//                              listing -> {
//                                context.getSelf().tell(listing);
//                                return Behaviors.stopped();
//                              })
//                      .build();
//            });
//  }
//
//  private Behavior<Receptionist.Listing> onListingReceived(
//          ActorContext<Receptionist.Listing> context, Receptionist.Listing listing) {
//    Set<ActorRef<QueryGraphQL>> serviceInstances =
//            listing.getServiceInstances(queryGraphQLServiceKey);
//    context.getLog().info("received listings: " + serviceInstances);
//    if (graphQLQueryServiceRef.get() == null) {
//      Optional<ActorRef<QueryGraphQL>> queryServiceActorRefOpt =
//              serviceInstances.stream().findFirst();
//      if (queryServiceActorRefOpt.isPresent()) {
//        graphQLQueryServiceRef.set(queryServiceActorRefOpt.get());
//        return listingSubscriberBehavior;
//      } else {
//        throw new IllegalStateException(
//                "Unable to get query graphql service actor ref to query graphql.");
//      }
//    }
//    if (serviceInstances.contains(graphQLQueryServiceRef.get())) {
//      return listingSubscriberBehavior;
//    }
//    Optional<ActorRef<QueryGraphQL>> graphQLActorRefOpt = serviceInstances.stream().findFirst();
//    if (graphQLActorRefOpt.isPresent()) {
//      graphQLQueryServiceRef.set(graphQLActorRefOpt.get());
//      return listingSubscriberBehavior;
//    }
//    throw new IllegalStateException(
//            "graphql query service ref no longer active or registered. cannot query graphql");
//  }
