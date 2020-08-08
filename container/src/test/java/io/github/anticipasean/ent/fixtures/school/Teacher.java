package io.github.anticipasean.ent.fixtures.school;

import java.util.List;

@org.immutables.value.Value.Immutable
@org.immutables.value.Value.Enclosing
public interface Teacher extends Member {

    @Override
    Long id();

    @Override
    PersonName fullName();

    List<Course> coursesTaught();
}
