package io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.annotation;

import cyclops.control.Option;
import cyclops.data.ImmutableList;
import cyclops.data.Seq;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.AnnotationMemberSpecImpl;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpecImpl;
import java.util.List;
import java.util.function.Function;
import javax.persistence.GenerationType;
import org.hibernate.mapping.PrimaryKey;

public enum JpaPrimaryKeyColumnAnnotationSpecCreator
    implements Function<PrimaryKey, ImmutableList<JavaSourceAnnotationSpec>> {
  INSTANCE;

  /**
   *
   *
   * <pre>
   * GenerationType strategy() default AUTO;
   * String generator() default "";
   * </pre>
   */
  private static JavaSourceAnnotationSpec getGeneratedValueAnnotationSpec() {
    return JavaSourceAnnotationSpecImpl.builder()
        .annotationClass(javax.persistence.GeneratedValue.class)
        .annotationMemberSpecs(Seq.of(AnnotationMemberSpecImpl.of("strategy", GenerationType.AUTO)))
        .build();
  }

  private static JavaSourceAnnotationSpec getIdAnnotationSpec() {
    return JavaSourceAnnotationSpecImpl.builder()
        .annotationClass(javax.persistence.Id.class)
        .build();
  }

  public static JpaPrimaryKeyColumnAnnotationSpecCreator getInstance() {
    return INSTANCE;
  }

  @Override
  public ImmutableList<JavaSourceAnnotationSpec> apply(PrimaryKey primaryKey) {
    if (hasSinglePrimaryKeyColumn(primaryKey)) {
      return Seq.of(getGeneratedValueAnnotationSpec(), getIdAnnotationSpec());
    }
    throw new UnsupportedOperationException(
        "multiple primary key column handling has not been implemented yet");
  }

  private boolean hasSinglePrimaryKeyColumn(PrimaryKey primaryKey) {
    return Option.fromNullable(primaryKey)
        .map(PrimaryKey::getColumns)
        .map(List::size)
        .filter(size -> size == 1)
        .isPresent();
  }
}
