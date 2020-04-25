package io.github.anticipasean.girakkagraph.protocol.base.util.subscription;

import akka.actor.typed.receptionist.ServiceKey;
import com.google.common.base.CaseFormat;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(overshadowImplementation = true, typeImmutable = "*Impl")
public interface Registrable<T> {

  @Value.Parameter
  Class<T> protocolMessageType();

  @Value.Derived
  default String id() {
    return CaseFormat.UPPER_CAMEL.to(
        CaseFormat.LOWER_UNDERSCORE, protocolMessageType().getSimpleName());
  }

  @Value.Derived
  default ServiceKey<T> serviceKey() {
    return ServiceKey.create(protocolMessageType(), id());
  }
}
