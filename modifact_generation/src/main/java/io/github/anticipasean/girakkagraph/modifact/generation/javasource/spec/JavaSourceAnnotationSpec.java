package io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec;

import com.squareup.javapoet.AnnotationSpec;
import cyclops.data.ImmutableList;
import cyclops.data.Seq;
import java.lang.annotation.Annotation;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface JavaSourceAnnotationSpec extends JavaSourceSpec<AnnotationSpec> {

  Class<? extends Annotation> annotationClass();

  @Value.Default
  default ImmutableList<AnnotationMemberSpec> annotationMemberSpecs(){
      return Seq.empty();
  }

  @Override
  default AnnotationSpec generateSpec() {
    return annotationMemberSpecs()
        .foldLeft(
            AnnotationSpec.builder(annotationClass()),
            (builder, annotationMemberSpec) ->
                annotationMemberSpec.annotationMemberAnnotationSpecBuilderUpdater().apply(builder))
        .build();
  }
}
