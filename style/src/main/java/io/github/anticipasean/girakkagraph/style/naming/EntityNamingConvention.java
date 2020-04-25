package io.github.anticipasean.girakkagraph.style.naming;

import java.time.YearMonth;
import java.util.List;

public interface EntityNamingConvention<T> {

  Class<T> parentEntityInterfaceType();

  default boolean appliesToClass(Class<?> cls) {
    return cls != null
        && parentEntityInterfaceType().isAssignableFrom(cls)
        && !cls.getName().equals(parentEntityInterfaceType().getName());
  }

  List<EntityNamingRule<T>> entityNamingRules();

  default boolean jpaEntityClassFollowsNamingRules(Class<? extends T> jpaEntityClass) {
    return entityNamingRules().stream()
        .allMatch(entityNamingRule -> entityNamingRule.jpaEntityClassFollowsRule(jpaEntityClass));
  }

  default boolean entityInterfaceClassFollowsNamingRules(Class<? extends T> entityInterfaceClass) {
    return entityNamingRules().stream()
        .allMatch(entityNamingRule -> entityNamingRule.entityInterfaceFollowsRule(entityInterfaceClass));
  }

  default boolean immutableEntityClassFollowsNamingRules(Class<? extends T> immutableEntityClass) {
    return entityNamingRules().stream()
        .allMatch(entityNamingRule -> entityNamingRule.immutableEntityClassFollowsRule(immutableEntityClass));
  }

  String entityInterfaceSimpleNameGivenJpaEntityClass(Class<? extends T> jpaEntityClass);

  String immutableEntitySimpleNameGivenJpaEntityClass(Class<? extends T> jpaEntityClass);

  String jpaEntitySimpleNameGivenEntityInterfaceClass(Class<? extends T> entityInterfaceClass);

  String jpaEntitySimpleNameGivenImmutableEntityClass(Class<? extends T> immutableEntityClass);
}
