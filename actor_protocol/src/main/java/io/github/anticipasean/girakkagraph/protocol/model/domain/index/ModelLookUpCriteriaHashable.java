package io.github.anticipasean.girakkagraph.protocol.model.domain.index;

import akka.japi.Pair;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import graphql.language.Argument;
import graphql.language.Directive;
import graphql.language.Field;
import graphql.language.Node;
import graphql.language.NodeTraverser;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(overshadowImplementation = true, typeImmutable = "*Impl", allParameters = true)
@JsonSerialize(as = ModelLookUpCriteriaHashableImpl.class)
@JsonDeserialize(as = ModelLookUpCriteriaHashableImpl.class)
public interface ModelLookUpCriteriaHashable {
  static ModelLookUpCriteriaHashable
      generateHashableModelLookUpCriteriaObjectFromQueryRootGraphQlFieldInContext(Field rootField) {
    NodeTraverser nodeTraverser = new NodeTraverser();
    ModelLookUpCriteriaHashableVisitor modelLookUpCriteriaHashableVisitor =
        new ModelLookUpCriteriaHashableVisitor();
    JsonObjectBuilder traversalResult =
        (JsonObjectBuilder) nodeTraverser.preOrder(modelLookUpCriteriaHashableVisitor, rootField);
    JsonObject modelHashableJsonObject =
        Json.createObjectBuilder().add(rootField.getName(), traversalResult).build();
    ModelLookUpCriteriaHashable modelLookUpCriteriaHashable =
        ModelLookUpCriteriaHashableImpl.of(modelHashableJsonObject);
    return modelLookUpCriteriaHashable;
  }

  // TODO: Possibility of infinite recursion must be dealt with
  static List<ModelPath> pathsFromHashable(
      ModelLookUpCriteriaHashable modelLookUpCriteriaHashable) {
    JsonObject obj = modelLookUpCriteriaHashable.lookUpHashable();
    List<ModelPath> paths = new ArrayList<>();
    if (obj.keySet().size() == 0) {
      return paths;
    }
    LinkedList<Pair<ModelPath, Map.Entry<String, JsonValue>>> queue = new LinkedList<>();
    Map.Entry<String, JsonValue> rootEntry =
        obj.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof JsonObject)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "look up hashable is not in the correct format: "
                            + modelLookUpCriteriaHashable));
    String rootTypeName = rootEntry.getKey();
    ModelPath rootType = ModelPathImpl.builder().addSegments(rootTypeName).build();
    Pair<ModelPath, Map.Entry<String, JsonValue>> modelPathEntryPair =
        Pair.create(rootType, rootEntry);
    queue.push(modelPathEntryPair);
    //    System.out.println(
    //        "starting stack: "
    //            + queue.stream()
    //                .map(modelPath -> modelPath.toString())
    //                .collect(Collectors.joining(",")));
    while (!queue.isEmpty()) {
      Pair<ModelPath, Map.Entry<String, JsonValue>> segmentEntry = queue.pop();
      //      System.out.println("segment: " + segmentEntry.first());
      ModelPath modelPath = segmentEntry.first();
      paths.add(modelPath);
      if (segmentEntry.second().getValue() instanceof JsonObject) {
        JsonObject value = (JsonObject) segmentEntry.second().getValue();
        value.entrySet().stream()
            .filter(stringJsonValueEntry -> stringJsonValueEntry.getValue() instanceof JsonObject)
            .forEach(
                stringJsonValueEntry ->
                    queue.add(
                        Pair.create(
                            ModelPathImpl.builder()
                                .from(modelPath)
                                .addSegment(stringJsonValueEntry.getKey())
                                .build(),
                            stringJsonValueEntry)));
      }
      //      System.out.println(
      //          "stack state: [ "
      //              + queue.stream()
      //                  .map(
      //                      mPathEntry ->
      //                          String.join(
      //                                  ": [ ",
      //                                  mPathEntry.first().uri().toString(),
      //                                  mPathEntry.second().toString())
      //                              + " ]")
      //                  .collect(Collectors.joining(", "))
      //              + " ]");
    }
    return paths;
  }

  JsonObject lookUpHashable();

  /**
   * Add brackets around key strings so that they are not confused with a potential attribute or
   * type name
   */
  enum ComponentType {
    FIELD(Field.class, "__fields__", JsonValue.ValueType.OBJECT),
    ARGUMENT(Argument.class, "__arguments__", JsonValue.ValueType.ARRAY),
    DIRECTIVE(Directive.class, "__directives__", JsonValue.ValueType.ARRAY);
    public final JsonValue.ValueType jsonValueType;
    public final Class<? extends Node> graphQLClassType;
    public final String key;

    ComponentType(
        Class<? extends Node> graphQLClassType, String key, JsonValue.ValueType valueType) {
      this.graphQLClassType = graphQLClassType;
      this.key = key;
      this.jsonValueType = valueType;
    }
  }
}
