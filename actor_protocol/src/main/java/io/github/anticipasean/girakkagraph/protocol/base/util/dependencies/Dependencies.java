package io.github.anticipasean.girakkagraph.protocol.base.util.dependencies;

import java.util.Set;
import org.immutables.value.Value;

@Value.Style(
    add = "addDependency",
    addAll = "addDependencies",
    depluralize = true,
    depluralizeDictionary = {"dependency:dependencies"},
    typeAbstract = "*",
    typeImmutable = "*Impl",
    overshadowImplementation = true)
@Value.Immutable
public interface Dependencies {
  Set<Dependency> dependenciesSet();
}
