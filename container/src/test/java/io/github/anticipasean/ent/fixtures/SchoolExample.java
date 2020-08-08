package io.github.anticipasean.ent.fixtures;

import io.github.anticipasean.ent.fixtures.school.Course;
import io.github.anticipasean.ent.fixtures.school.CourseImpl;
import io.github.anticipasean.ent.fixtures.school.Exam;
import io.github.anticipasean.ent.fixtures.school.ExamImpl;
import io.github.anticipasean.ent.fixtures.school.ExamResult;
import io.github.anticipasean.ent.fixtures.school.ExamResultImpl;
import io.github.anticipasean.ent.fixtures.school.PersonNameImpl;
import io.github.anticipasean.ent.fixtures.school.Student;
import io.github.anticipasean.ent.fixtures.school.StudentImpl;
import io.github.anticipasean.ent.fixtures.school.Teacher;
import io.github.anticipasean.ent.fixtures.school.TeacherImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public interface SchoolExample {


    static Student student1() {
        return StudentImpl.builder()
                          .fullName(PersonNameImpl.builder()
                                                  .id(131324L)
                                                  .firstName("Robert")
                                                  .middleName("Harold")
                                                  .lastName("Harris")
                                                  .preferredName("Bob")
                                                  .build())
                          .id(124223L)
                          .build();
    }

    static Student student2() {
        return StudentImpl.builder()
                          .fullName(PersonNameImpl.builder()
                                                  .id(587873L)
                                                  .firstName("Sarah")
                                                  .middleName("Elizabeth")
                                                  .lastName("Perrywinkle")
                                                  .preferredName("Sarah")
                                                  .build())
                          .id(247827L)
                          .build();
    }

    static Student student3() {
        return StudentImpl.builder()
                          .fullName(PersonNameImpl.builder()
                                                  .id(587936L)
                                                  .firstName("Frank")
                                                  .middleName("Christopher")
                                                  .lastName("Plumer")
                                                  .preferredName("Frankie")
                                                  .build())
                          .id(323423L)
                          .build();
    }

    static Teacher philosophyCourseTeacher() {
        return TeacherImpl.builder()
                          .fullName(PersonNameImpl.builder()
                                                  .id(123112L)
                                                  .preferredName("Diane")
                                                  .firstName("Diane")
                                                  .middleName("Florence")
                                                  .lastName("Stevens")
                                                  .build())
                          .id(182L)
                          .build();
    }

    static Teacher mathematicsCourseTeacher() {
        return TeacherImpl.builder()
                          .fullName(PersonNameImpl.builder()
                                                  .id(123123L)
                                                  .preferredName("Erin")
                                                  .firstName("Erin")
                                                  .middleName("Jennifer")
                                                  .lastName("Collins")
                                                  .build())
                          .id(186L)
                          .build();
    }

    static List<ExamResult> philosophyExam1Results() {
        return Arrays.asList(ExamResultImpl.builder()
                                           .courseId(2342L)
                                           .examId(12311L)
                                           .id(543325L)
                                           .studentId(student1().id())
                                           .score(0.712D)
                                           .build(),
                             ExamResultImpl.builder()
                                           .courseId(2342L)
                                           .examId(12311L)
                                           .id(543326L)
                                           .studentId(student2().id())
                                           .score(0.891D)
                                           .build(),
                             ExamResultImpl.builder()
                                           .courseId(2342L)
                                           .examId(12311L)
                                           .id(543327L)
                                           .studentId(student3().id())
                                           .score(0.798D)
                                           .build());
    }

    static List<ExamResult> philosophyExam2Results() {
        return Arrays.asList(ExamResultImpl.builder()
                                           .courseId(2342L)
                                           .examId(12356L)
                                           .id(543387L)
                                           .studentId(124223L)
                                           .score(0.677D)
                                           .build(),
                             ExamResultImpl.builder()
                                           .courseId(2342L)
                                           .examId(12356L)
                                           .id(543390L)
                                           .studentId(587873L)
                                           .score(0.923D)
                                           .build(),
                             ExamResultImpl.builder()
                                           .courseId(2342L)
                                           .examId(12356L)
                                           .id(543327L)
                                           .studentId(323423L)
                                           .score(0.958D)
                                           .build());
    }

    static Exam philosophyExam1() {
        return ExamImpl.builder()
                       .administeredDateTime(LocalDateTime.now()
                                                          .minus(1,
                                                                 ChronoUnit.WEEKS))
                       .id(12311L)
                       .name("Socrates and Plato")
                       .version("1.2")
                       .addAllResults(philosophyExam1Results())
                       .build();
    }

    static Exam philosophyExam2() {
        return ExamImpl.builder()
                       .administeredDateTime(LocalDateTime.now())
                       .id(12356L)
                       .name("Descartes and Hume")
                       .version("1.5")
                       .build();
    }


    static Course philosophyCourse() {
        return CourseImpl.builder()
                         .id(2342L)
                         .teacher(philosophyCourseTeacher())
                         .addStudentIds(student1().id(),
                                        student2().id(),
                                        student3().id())
                         .startDate(LocalDate.now()
                                             .minus(1,
                                                    ChronoUnit.MONTHS))
                         .endDate(LocalDate.now()
                                           .plus(2,
                                                 ChronoUnit.MONTHS))
                         .name("Philosophy 101")
                         .addExams(philosophyExam1())
                         .build();
    }


    static List<ExamResult> calculus3Exam1Results() {
        return Arrays.asList(ExamResultImpl.builder()
                                           .courseId(2367L)
                                           .examId(12388L)
                                           .id(543398L)
                                           .studentId(student1().id())
                                           .score(0.812D)
                                           .build(),
                             ExamResultImpl.builder()
                                           .courseId(2367L)
                                           .examId(12388L)
                                           .id(543399L)
                                           .studentId(student2().id())
                                           .score(0.881D)
                                           .build());
    }

    static Exam calculus3Exam1() {
        return ExamImpl.builder()
                       .administeredDateTime(LocalDateTime.now()
                                                          .minus(2,
                                                                 ChronoUnit.WEEKS))
                       .id(12388L)
                       .name("Vector Analysis")
                       .version("2.1")
                       .addAllResults(calculus3Exam1Results())
                       .build();
    }

    static Course calculus3Course() {
        return CourseImpl.builder()
                         .id(2367L)
                         .teacher(mathematicsCourseTeacher())
                         .addStudentIds(student1().id(),
                                        student2().id())
                         .startDate(LocalDate.now()
                                             .minus(1,
                                                    ChronoUnit.MONTHS))
                         .endDate(LocalDate.now()
                                           .plus(2,
                                                 ChronoUnit.MONTHS))
                         .name("Calculus 3")
                         .addExams(calculus3Exam1())
                         .build();
    }

}
