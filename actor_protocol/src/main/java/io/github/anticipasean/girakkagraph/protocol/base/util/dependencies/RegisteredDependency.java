package io.github.anticipasean.girakkagraph.protocol.base.util.dependencies;

import akka.actor.typed.ActorRef;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.DependenciesRegistered;
import java.util.Set;
import org.immutables.criteria.Criteria;
import org.immutables.criteria.repository.sync.SyncReadable;
import org.immutables.criteria.repository.sync.SyncWritable;
import org.immutables.value.Value;

@Value.Immutable
@Value.Enclosing
@Criteria
@Criteria.Repository(facets = {SyncReadable.class, SyncWritable.class})
public interface RegisteredDependency {

  Set<ActorRef<DependenciesRegistered>> dependentActors();

  Dependency dependency();
}
