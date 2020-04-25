package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.base;

import akka.japi.Pair;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelGraphComponent;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import java.util.Map;
import javax.json.JsonValue;
import org.immutables.value.Value;

@Value.Immutable
interface UnprocessedModelGraphComponent extends ModelGraphComponent {
  @Value.Default
  default ModelPath path() {
    return segmentEntry().first();
  }

  @Value.Default
  default String segmentName() {
    return segmentEntry().second().getKey();
  }

  @Value.Default
  default JsonValue jsonValue() {
    return segmentEntry().second().getValue();
  }

  @Value.Parameter
  Pair<ModelPath, Map.Entry<String, JsonValue>> segmentEntry();
}
