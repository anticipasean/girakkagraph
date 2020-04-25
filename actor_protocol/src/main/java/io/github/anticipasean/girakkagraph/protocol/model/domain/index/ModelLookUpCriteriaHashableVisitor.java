package io.github.anticipasean.girakkagraph.protocol.model.domain.index;

import akka.japi.Pair;
import graphql.language.Argument;
import graphql.language.Directive;
import graphql.language.Field;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelLookUpCriteriaHashableVisitor extends NodeVisitorStub {

  private final Logger logger;

  public ModelLookUpCriteriaHashableVisitor() {
    this.logger = LoggerFactory.getLogger(ModelLookUpCriteriaHashableVisitor.class);
  }

  @Override
  public TraversalControl visitArgument(Argument node, TraverserContext<Node> context) {
    if (node == null) {
      return TraversalControl.QUIT;
    }
    logger.info("argument: " + node);
    JsonArrayBuilder accumulate = context.getNewAccumulate();
    context.setAccumulate(accumulate.add(node.getName()));
    return TraversalControl.QUIT;
  }

  @Override
  public TraversalControl visitDirective(Directive node, TraverserContext<Node> context) {
    if (node == null) {
      return TraversalControl.QUIT;
    }
    logger.info("directive: " + node);
    JsonArrayBuilder accumulate = context.getNewAccumulate();
    context.setAccumulate(accumulate.add(node.getName()));
    return TraversalControl.QUIT;
  }

  @Override
  public TraversalControl visitField(Field node, TraverserContext<Node> context) {
    if (node == null) {
      return TraversalControl.QUIT;
    }
    logger.info("field: " + node);
    JsonArrayBuilder arguments = null;
    if (!node.getArguments().isEmpty()) {
      context.setAccumulate(Json.createArrayBuilder());
      for (Argument argument : node.getArguments()) {
        visitArgument(argument, context);
      }
      arguments = context.getNewAccumulate();
      logger.info("arguments accumulate: " + arguments);
    }
    JsonArrayBuilder directives = null;
    if (!node.getDirectives().isEmpty()) {
      context.setAccumulate(Json.createArrayBuilder());
      for (Directive directive : node.getDirectives()) {
        visitDirective(directive, context);
      }
      directives = context.getNewAccumulate();
      logger.info("directives accumulate: " + directives);
    }
    context.setAccumulate(Stream.<Pair<String, JsonObjectBuilder>>builder());
    visitSelectionSet(node.getSelectionSet(), context);
    Stream.Builder<Pair<String, JsonObjectBuilder>> selections = context.getNewAccumulate();
    logger.info("selection set accumulate: " + selections);
    Stream<Pair<String, JsonObjectBuilder>> pairStream = selections.build();
    JsonObjectBuilder objectBuilder =
        pairStream.reduce(
            Json.createObjectBuilder(),
            (jsonObjectBuilder, stringJsonObjectBuilderPair) ->
                jsonObjectBuilder.add(
                    stringJsonObjectBuilderPair.first(), stringJsonObjectBuilderPair.second()),
            (jsonObjectBuilder, jsonObjectBuilder2) -> jsonObjectBuilder2);
    JsonObjectBuilder fieldObjectWithProperties = objectBuilder;
    if (arguments != null) {
      fieldObjectWithProperties =
          fieldObjectWithProperties.add(
              ModelLookUpCriteriaHashable.ComponentType.ARGUMENT.key, arguments.build());
    }
    if (directives != null) {
      fieldObjectWithProperties =
          fieldObjectWithProperties.add(
              ModelLookUpCriteriaHashable.ComponentType.DIRECTIVE.key, directives.build());
    }
    logger.info("field accumulate: " + fieldObjectWithProperties);
    context.setAccumulate(fieldObjectWithProperties);
    return TraversalControl.QUIT;
  }

  @Override
  public TraversalControl visitSelectionSet(SelectionSet node, TraverserContext<Node> context) {
    if (node == null) {
      return TraversalControl.QUIT;
    }
    for (Selection selection : node.getSelections()) {
      if (selection instanceof Field) {
        logger.info("selection: " + selection);
        Stream.Builder<Pair<String, JsonObjectBuilder>> fieldBuilder = context.getNewAccumulate();
        visitField((Field) selection, context);
        fieldBuilder =
            fieldBuilder.add(
                Pair.create(((Field) selection).getName(), context.getNewAccumulate()));
        context.setAccumulate(fieldBuilder);
      }
    }
    return TraversalControl.QUIT;
  }
}
