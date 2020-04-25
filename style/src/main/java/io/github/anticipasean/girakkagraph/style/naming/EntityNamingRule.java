package io.github.anticipasean.girakkagraph.style.naming;

public interface EntityNamingRule<T> {

  default boolean entityClassTrioFollowsRule(
      Class<? extends T> entityInterface,
      Class<? extends T> jpaEntityClass,
      Class<? extends T> immutableEntityClass) {
    return true;
  }

  default boolean entityInterfaceFollowsRule(Class<? extends T> entityInterface) {
    return true;
  }

  default boolean jpaEntityClassFollowsRule(Class<? extends T> jpaEntityClass) {
    return true;
  }

  default boolean immutableEntityClassFollowsRule(Class<? extends T> immutableEntityClass) {
    return true;
  }

  default boolean entityInterfaceNameFollowsRule(String entityInterface) {
    return true;
  }

  default boolean jpaEntityClassNameFollowsRule(String jpaEntityClass) {
    return true;
  }

  default boolean immutableEntityClassNameFollowsRule(String immutableEntityClass) {
    return true;
  }
}
