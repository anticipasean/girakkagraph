package io.github.anticipasean.girakkagraph.springboot.web.controller;

import akka.Done;
import akka.actor.typed.ActorSystem;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.springboot.conf.TestDatabaseConfigurationImpl;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.persistence.EntityManager;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.PropertyResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

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
@AutoConfigureWebFlux
@AutoConfigureWebTestClient
@Import({TestDatabaseConfigurationImpl.class})
@ActiveProfiles({"test"})
public class GraphQLControllerTestDbTest {

  @Autowired private EntityManager entityManager;
  //  @Autowired private AppPropertyJpaRepository repository;
  @Autowired private PropertyResolver propertyResolver;
  @Autowired private WebTestClient webTestClient;
  @Autowired private ActorSystem<Command> actorSystem;
  private String appPropertyGraphQLQuery =
      "{\"query\":\"{appProperty(propertyKey: \\\"blah.blah\\\"){ propertyKey propertyValue }}\"}";
  //  @Autowired private DataSource dataSource;

  @BeforeEach
  public void setUp() {}

  @AfterEach
  public void tearDown() {}

  @Test
  /** Requires a ton of configuration settings to be just right */
  public void runActualGraphQLQuery()
      throws InterruptedException, ExecutionException, TimeoutException {
    //    insertTestAppPropertiesEntry();
    CompletableFuture<Done> doneCompletableFuture =
        actorSystem.getWhenTerminated().toCompletableFuture();

    actualGraphQLQuery(webTestClient);
    doneCompletableFuture.get(25, TimeUnit.SECONDS);
  }

  @Transactional
  public void actualGraphQLQuery(WebTestClient webTestClient) {
    String queryWithIdPredicateOnEntity =
        "{\"query\": \"{  account(accountId: 1829123){    accountId  billingHistories { feeBillingHistories { feeCode feeId(eq: 1234124) } }  achHoldings {      achHoldingId      rejectionReason    } accrualSegments { accrualSegmentId accrualRate fee { feeId } } } }\"}";
    webTestClient
        .mutateWith(
            (builder, httpHandlerBuilder, connector) ->
                builder.responseTimeout(Duration.ofSeconds(25)))
        .post()
        .uri("/graphql")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Mono.just(
                "{\"query\": \"{  account{    accountId(eq: 1829123)  billingHistories { feeBillingHistories { feeCode feeId(eq: 1234124) } }  achHoldings {      achHoldingId(group: 1)      rejectionReason    } accrualSegments { accrualSegmentId accrualRate fee { feeId } } } }\"}"),
            String.class)
        .exchange()
        .expectStatus()
        .isOk();
  }

  //  @Transactional
  //  public void insertTestAppPropertiesEntry() {
  //    Long fakePropId = 1928472984L;
  //    String propKeyPrimaryKey = "blah.blah";
  //    this.entityManager.persist(
  //        AppPropertyJpa.create()
  //            .setPropertyId(fakePropId)
  //            .setPropertyKey(propKeyPrimaryKey)
  //            .setPropertyValue("blalue")
  //            .setUserIdentifier("Bob"));
  //    AppPropertyJpa appPropertiesJpa = this.repository.getOne(propKeyPrimaryKey);
  //    Assert.assertEquals(fakePropId, appPropertiesJpa.getPropertyId());
  //    this.repository.saveAndFlush(appPropertiesJpa);
  //  }

  @Test
  public void runGraphQLQueryForEntityNotPresent() {
    webTestClient
        .post()
        .uri("/graphql")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Mono.just(
                "{\"query\":\"{appProperties(propertyKey: \\\"blah.blah\\\"){ propertyKey propertyValue }}\"}"),
            String.class)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.errors[0].message")
        .value(
            s -> Assert.assertThat(s, Matchers.containsString("no result was found for")),
            String.class);
  }
}
