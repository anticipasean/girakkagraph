package io.github.anticipasean.girakkagraph.springboot.supervisor;

import akka.Done;
import akka.actor.typed.ActorSystem;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.springboot.conf.TestDatabaseConfigurationImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//@SpringBootTest(
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        properties = {
//                "spring.jpa.database=default",
//                "spring.jpa.show-sql=true",
//                "spring.datasource.continue-on-error=false",
//        })
//@RunWith(SpringRunner.class)
//@Transactional
//@AutoConfigureDataJpa
//@AutoConfigureCache
//@AutoConfigureWebFlux
//@AutoConfigureWebTestClient
//@ActiveProfiles({"test"})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.jpa.generate-ddl=true",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database=default",
                "spring.jpa.show-sql=true",
                "spring.jpa.hibernate.naming.strategy=org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl",
                "spring.datasource.continue-on-error=false",
                //      "spring.datasource.generate-unique-name=true",
                //      "spring.datasource.username=sa"
        })
@RunWith(SpringRunner.class)
@Transactional
@AutoConfigureTestEntityManager
@AutoConfigureDataJpa
@AutoConfigureCache
@AutoConfigureWebFlux
@AutoConfigureWebTestClient
@Import({TestDatabaseConfigurationImpl.class})
@ActiveProfiles({"test"})
public class SupervisorProtocolTest {

  @Autowired private ActorSystem<Command> actorSystem;

  @Test
  public void startUpSystem() throws InterruptedException, ExecutionException, TimeoutException {
    CompletableFuture<Done> doneCompletableFuture = actorSystem.getWhenTerminated().toCompletableFuture();
    doneCompletableFuture.get(15,TimeUnit.SECONDS);

    //    Behavior<SystemInitialized> receiveSystemInitialized =
    //        Behaviors.<SystemInitialized>receiveMessage(
    //            command -> {
    //              System.out.println("received system initialized");
    //              System.out.println(
    //                  String.format("error occurred: %s", command.errorOccurred().isPresent()));
    //              command
    //                  .errorOccurred()
    //                  .ifPresent(throwable -> System.out.println("throwable: " + throwable));
    //              return Behaviors.stopped();
    //            });
    //    StartUpSystem startUpSystem =
    //        StartUpSystemImpl.builder()
    //            .commandId(UUID.randomUUID())
    //            .build();
    //    ActorSystem<SupervisorCommand> protocol =
    //        ActorSystem.create(SupervisorProtocol.create(), "protocol");
    //    CompletionStage<Void> whenDone =
    //        protocol
    //            .getWhenTerminated()
    //            .handleAsync(
    //                (done, throwable) -> {
    //                  if (throwable != null) {
    //                    System.out.println("supervisor protocol test: error occurred: %s" +
    // throwable);
    //                  } else if (done != null) {
    //                    System.out.println(
    //                        "supervisor protocol test: protocol terminated successfully");
    //                  }
    //                  return Optional.ofNullable(done);
    //                })
    //            .thenRun(() -> System.out.println("done"));
    //    protocol
    //        .scheduler()
    //        .scheduleOnce(
    //            Duration.ofSeconds(10), () -> protocol.terminate(), protocol.executionContext());
  }
}
