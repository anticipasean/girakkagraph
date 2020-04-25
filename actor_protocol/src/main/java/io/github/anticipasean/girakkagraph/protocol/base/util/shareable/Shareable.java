package io.github.anticipasean.girakkagraph.protocol.base.util.shareable;

import akka.actor.typed.receptionist.ServiceKey;
import io.github.anticipasean.girakkagraph.protocol.base.util.container.ValueContainer;
import java.util.Optional;
import org.immutables.criteria.Criteria;
import org.immutables.criteria.repository.sync.SyncReadable;
import org.immutables.criteria.repository.sync.SyncWritable;
import org.immutables.value.Value;

@Value.Immutable
@Value.Enclosing
@Criteria
@Criteria.Repository(facets = {SyncReadable.class, SyncWritable.class})
@Value.Style(
    get = {"is*", "get*"}, // Detect 'get' and 'is' prefixes in accessor methods
    //        init = "set*", // Builder initialization methods will have 'set' prefix
    typeAbstract = {"*"}, // 'Base' prefix will be detected and trimmed
    typeImmutable = "*Impl")
public interface Shareable {

  @Value.Default
  @Criteria.Id
  default String name() {
    return valueContainer().name();
  }

  @Value.Default
  default String fullJavaClassNameWithParameterizedType() {
    return valueContainer().name();
  }

  @Value.Default
  default Class<?> javaClass() {
    return valueContainer().type();
  }

  Optional<AssociatedActorInfo<?>> associatedActorInfo();

  @Value.Default
  default Class<?> valueContainerType() {
    return valueContainer().containerType();
  }

  ValueContainer<?, ?, ?> valueContainer();

  @Value.Immutable
  interface AssociatedActorInfo<C> {
    ServiceKey<C> serviceKey();
  }
}
//    @Value.Default
//    default Class<T> dependencyType() {
//      return valueContainer().keySet().stream().findFirst().get();
//    }
//
//    Map<Class<T>, T> valueContainer();
//
//    @Value.Default
//    default T value() {
//      Optional<Class<T>> tClass = valueContainer().keySet().stream().findFirst();
//      if (tClass.isPresent()) {
//        T val = valueContainer().get(tClass.get());
//        return val;
//      }
//      throw new NoSuchElementException("no element was specified as the value in value map");
//    }
