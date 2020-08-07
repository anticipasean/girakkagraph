package io.github.anticipasean.ent.example.school;

import java.time.LocalDateTime;
import java.util.List;

//@org.immutables.value.Value.Immutable

public interface Exam {

    Long id();

    String name();

    String version();

    LocalDateTime administeredDateTime();

    List<ExamResult> results();
}
