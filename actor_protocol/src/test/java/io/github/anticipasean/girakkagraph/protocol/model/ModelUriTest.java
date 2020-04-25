package io.github.anticipasean.girakkagraph.protocol.model;

import akka.japi.Pair;
import com.google.common.collect.Lists;


import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ModelUriTest {


  @Test
  void createModelUri() {
    URI propKeyUri = URI.create("model://appProperty/propertyKey");
    URI spPropUri = URI.create("model://appProperty");
    URI achHoldIdUri = URI.create("model://account/achHoldings/achHoldingId");
    URI acctUri = URI.create("model://account");
    URI acctAchUri = URI.create("model://account/achHoldings");
    URI propValUri = URI.create("model://appProperty/propertyValue");
    List<URI> uris =
        Lists.newArrayList(propKeyUri, spPropUri, acctAchUri, achHoldIdUri, acctUri, propValUri);
    Set<Pair<URI, URI>> pairs =
        uris.stream()
            .flatMap(uri -> uris.stream().map(uri2 -> Pair.create(uri, uri2)))
            .collect(Collectors.toSet());
    System.out.println("first,second,resolve,relativize,relativePath");
    pairs.stream()
        .map(
            uriuriPair ->
                String.join(
                    ",",
                    uriuriPair.first().toString(),
                    uriuriPair.second().toString(),
                    uriuriPair.first().resolve(uriuriPair.second()).toString(),
                    uriuriPair.first().relativize(uriuriPair.second()).toString(),
                    uriuriPair.first().relativize(uriuriPair.second()).getPath()))
        .forEach(System.out::println);
  }
}
