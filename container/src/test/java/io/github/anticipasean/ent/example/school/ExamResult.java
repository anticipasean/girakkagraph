package io.github.anticipasean.ent.example.school;

//@org.immutables.value.Value.Immutable

public interface ExamResult {

    Long id();

    Long studentId();

    Long courseId();

    Long examId();

    Double score();
}
