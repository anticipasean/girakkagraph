package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge;

import com.google.common.base.Preconditions;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.ModelArgument;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(
    typeImmutable = "*Impl",
    overshadowImplementation = true,
    depluralize = true,
    depluralizeDictionary = {"unprocessedArgument:unprocessedArguments"})
public interface UnprocessedArgumentEdge extends ArgumentEdge {

  List<String> unprocessedArguments();

  @Override
  default ModelArgument<?, ?> modelArgument() {
    throw new UnsupportedOperationException(
        "unprocessed argument edges by definition do not have model arguments yet");
  }

  @Value.Check
  default void checkRawArgumentsNotContainInvalidCharacters() {
    Preconditions.checkArgument(
        unprocessedArguments().stream().noneMatch(s -> s.indexOf('=') >= 0 && s.indexOf('/') >= 0));
  }
}
