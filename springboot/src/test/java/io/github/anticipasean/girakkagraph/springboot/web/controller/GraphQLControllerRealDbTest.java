package io.github.anticipasean.girakkagraph.springboot.web.controller;

import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.jpa.database=default",
      "spring.jpa.show-sql=true",
      "spring.datasource.continue-on-error=false",
    })
@RunWith(SpringRunner.class)
@Transactional
// @AutoConfigureDataJpa
@AutoConfigureCache
@AutoConfigureWebFlux
@AutoConfigureWebTestClient
@ActiveProfiles({"default"})
// @Transactional
// @AutoConfigureTestEntityManager
// @AutoConfigureDataJpa
// @AutoConfigureCache
// @AutoConfigureWebFlux
// @AutoConfigureWebTestClient
// @Import({TestDatabaseConfiguration.class})
// @ActiveProfiles({"test"})
public class GraphQLControllerRealDbTest {

  @Autowired private EntityManager entityManager;
  @Autowired private WebTestClient webTestClient;
  //  @Autowired private DataSource dataSource;

  @Test
  /** Requires a ton of configuration settings to be just right */
  public void runAccountGraphQLQuery() {
    actualGraphQLQuery(webTestClient);
  }

  @Transactional
  public void actualGraphQLQuery(WebTestClient webTestClient) {
    webTestClient
        .post()
        .uri("/graphql")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Mono.just(
                "{\"query\":\"{account(accountId: 95555){ accountId achHoldings{ achHoldingId } }}\"}"),
            String.class)
        .exchange()
        .expectStatus()
        .isOk();
  }
}
