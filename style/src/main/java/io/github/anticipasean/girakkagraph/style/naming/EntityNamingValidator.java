package io.github.anticipasean.girakkagraph.style.naming;

import javax.naming.InvalidNameException;
import java.util.Arrays;
import java.util.stream.Collectors;

public interface EntityNamingValidator {
  static <T> void checkEntityClassTrioNamingAgainstRule(
      EntityNamingRule<T> entityNamingRule,
      Class<? extends T> entityInterface,
      Class<? extends T> jpaEntityClass,
      Class<? extends T> immutableEntityClass)
      throws InvalidNameException {
    if (!entityNamingRule.entityClassTrioFollowsRule(
        entityInterface, jpaEntityClass, immutableEntityClass)) {
      throw generateInvalidNameException(
          entityNamingRule, entityInterface, jpaEntityClass, immutableEntityClass);
    }
  }

  static <T> void checkEntityInterfaceNamingAgainstRule(
      EntityNamingRule<T> entityNamingRule, Class<T> entityInterface) throws InvalidNameException {
    if (!entityNamingRule.entityInterfaceFollowsRule(entityInterface)) {
      throw generateInvalidNameException(entityNamingRule, entityInterface);
    }
  }

  static <T> void checkJpaEntityClassNamingAgainstRule(
      EntityNamingRule<T> entityNamingRule, Class<? extends T> jpaEntityClass) throws InvalidNameException {
    if (!entityNamingRule.jpaEntityClassFollowsRule(jpaEntityClass)) {
      throw generateInvalidNameException(entityNamingRule, jpaEntityClass);
    }
  }

  static <T> InvalidNameException generateInvalidNameException(
      EntityNamingRule<T> entityNamingRule, Class<?>... cls) {
    String classNamesStr = Arrays.stream(cls).map(Class::getName).collect(Collectors.joining(", "));
    String message =
        String.format(
            "the class set [ %s ] does not following the naming rule: %s ",
            classNamesStr, entityNamingRule.getClass().getName());
    return new InvalidNameException(message);
  }
}
