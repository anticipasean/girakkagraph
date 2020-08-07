package io.github.anticipasean.ent.example.school;

import java.time.LocalDate;
import java.util.List;

//@org.immutables.value.Value.Immutable

public interface Course {

    Long id();

    String name();

    Teacher teacher();

    List<Student> students();

    List<Exam> exams();

    LocalDate startDate();

    LocalDate endDate();
}
