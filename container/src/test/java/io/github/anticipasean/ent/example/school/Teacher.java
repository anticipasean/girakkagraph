package io.github.anticipasean.ent.example.school;

import java.util.List;

//@org.immutables.value.Value.Immutable

public interface Teacher {

    Long id();

    PersonName fullName();

    List<Course> coursesTaught();
}
