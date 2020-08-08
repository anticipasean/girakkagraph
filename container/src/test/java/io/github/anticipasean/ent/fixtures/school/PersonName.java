package io.github.anticipasean.ent.fixtures.school;

@org.immutables.value.Value.Immutable

public interface PersonName {

    Long id();

    String preferredName();

    String firstName();

    String middleName();

    String lastName();
}
