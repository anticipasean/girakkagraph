package io.github.anticipasean.ent.fixtures.school;

import java.time.LocalDateTime;
import java.util.List;

@org.immutables.value.Value.Immutable
@org.immutables.value.Value.Enclosing
public interface Exam {

    Long id();

    String name();

    String version();

    LocalDateTime administeredDateTime();

    List<ExamResult> results();
}
