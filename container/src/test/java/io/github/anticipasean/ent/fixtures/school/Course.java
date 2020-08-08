package io.github.anticipasean.ent.fixtures.school;

import java.time.LocalDate;
import java.util.List;

@org.immutables.value.Value.Immutable
@org.immutables.value.Value.Enclosing
public interface Course {

    Long id();

    String name();

    Teacher teacher();

    List<Long> studentIds();

    List<Exam> exams();

    LocalDate startDate();

    LocalDate endDate();
}
