package io.github.anticipasean.ent;

import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import io.github.anticipasean.ent.fixtures.SchoolExample;
import io.github.anticipasean.ent.fixtures.school.Course;
import io.github.anticipasean.ent.fixtures.school.CourseImpl;
import io.github.anticipasean.ent.fixtures.school.CourseImpl.Builder;
import io.github.anticipasean.ent.fixtures.school.ExamResult;
import io.github.anticipasean.ent.fixtures.school.Member;
import io.github.anticipasean.ent.fixtures.school.Student;
import io.github.anticipasean.ent.fixtures.school.Teacher;
import io.github.anticipasean.ent.pattern.pair.Pattern2;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.function.UnaryOperator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SchoolExampleEntTest {

    @Test
    public void matchGetExamResult() {
        Long philosophyCourseId = SchoolExample.philosophyCourse()
                                               .id();
        Ent<Long, Course> coursesEnt = Ent.fromValuesIterable(Seq.of(SchoolExample.philosophyCourse()),
                                                              Course::id);
        Option<Tuple2<Long, Seq<ExamResult>>> examsForCourse = coursesEnt.matchGet(philosophyCourseId,
                                                                                   matcher -> matcher.caseWhenKeyValue()
                                                                                                     .valueMapsTo(course -> Option.of(course.exams()
                                                                                                                                            .stream()
                                                                                                                                            .flatMap(exam -> exam.results()
                                                                                                                                                                 .stream())
                                                                                                                                            .collect(Seq.collector())))
                                                                                                     .then(Tuple2::of)
                                                                                                     .elseNullable());
        Assert.assertTrue(examsForCourse.isPresent());
        Assert.assertEquals((int) examsForCourse.map(Tuple2::_2)
                                                .map(Seq::size)
                                                .orElse(-1),
                            2);
    }

    @Test
    public void addNewCourse() {

        Seq<Member> schoolMembers = Seq.of(SchoolExample.mathematicsCourseTeacher(),
                                           SchoolExample.student1(),
                                           SchoolExample.student2());
        Seq<Tuple2<String, Object>> courseAttributes = Seq.<String>of("id",
                                                                      "courseName",
                                                                      "startDate",
                                                                      "endDate").zip(Seq.<Object>of(1231L,
                                                                                                    "Differential Equations",
                                                                                                    LocalDate.now()
                                                                                                             .plus(2,
                                                                                                                   ChronoUnit.MONTHS),
                                                                                                    LocalDate.now()
                                                                                                             .plus(5,
                                                                                                                   ChronoUnit.MONTHS)));
        Ent<String, Member> ent = Ent.fromValuesIterable(schoolMembers,
                                                         member -> String.join(":",
                                                                               member.getClass()
                                                                                     .getTypeName()
                                                                                     .contains("Student") ? "student" : "teacher",
                                                                               member.fullName()
                                                                                     .firstName()));
        Pattern2<String, Object, String, UnaryOperator<Builder>> courseBuilderMappingPattern = matcher -> {
            return matcher.caseWhenKeyValue()
                          .valueOfType(Student.class)
                          .then((s, student) -> Tuple2.of(s,
                                                          (UnaryOperator<CourseImpl.Builder>) (CourseImpl.Builder builder) -> builder.addStudentIds(student.id())))
                          .valueOfType(Teacher.class)
                          .then((s, teacher) -> Tuple2.of(s,
                                                          builder -> builder.teacher(teacher)))
                          .keyFitsAndValueOfType(k -> k.contains("id"),
                                                 Long.class)
                          .then((s, aLong) -> Tuple2.of(s,
                                                        builder -> builder.id(aLong)))
                          .keyFitsAndValueOfType(k -> k.contains("courseName"),
                                                 String.class)
                          .then((s, v) -> Tuple2.of(s,
                                                    builder -> builder.name(v)))
                          .keyFitsAndValueOfType(k -> k.contains("startDate"),
                                                 LocalDate.class)
                          .then((s, localDate) -> Tuple2.of(s,
                                                            builder -> builder.startDate(localDate)))
                          .keyFitsAndValueOfType(k -> k.contains("endDate"),
                                                 LocalDate.class)
                          .then((s, localDate) -> Tuple2.of(s,
                                                            builder -> builder.endDate(localDate)))
                          .elseThrow(kvTuple -> new IllegalArgumentException("no match was found for tuple: " + kvTuple));
        };

        Course course = ent.map(member -> (Object) member)
                           .putAll(courseAttributes.toMap(Tuple2::_1,
                                                          Tuple2::_2))
                           .matchFoldRight(courseBuilderMappingPattern,
                                           CourseImpl.builder(),
                                           (builder, stringUnaryOperatorTuple2) -> stringUnaryOperatorTuple2._2()
                                                                                                            .apply(builder))
                           .build();
        Assert.assertEquals((long) course.id(),
                            1231L);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getStudentExamResults() {
        Student student1 = SchoolExample.student1();
        Ent<Long, ExamResult> examResults = Ent.fromValuesIterable(ReactiveSeq.concat(SchoolExample.calculus3Exam1Results()
                                                                                                   .stream(),
                                                                                      SchoolExample.philosophyExam1Results()
                                                                                                   .stream(),
                                                                                      SchoolExample.philosophyExam2Results()
                                                                                                   .stream()),
                                                                   ExamResult::id);
        System.out.println(examResults.mkString());
        double student1ExamScoresMean = examResults.filterValues(examResult -> examResult.studentId()
                                                                                         .equals(student1.id()))
                                                   .mean(longExamResultTuple2 -> longExamResultTuple2._2()
                                                                                                     .score());
        System.out.println(student1ExamScoresMean);
    }

}
