package io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import cyclops.data.ImmutableList;
import cyclops.data.Seq;
import javax.lang.model.element.Modifier;
import org.immutables.value.Value;

@Value.Immutable
public interface PropertyMethodSpec extends JavaSourceSpec<MethodSpec> {

  String methodName();

  ImmutableList<AnnotationSpec> annotationSpec();

  ImmutableList<Modifier> modifiers();

  TypeName returnType();

  @Override
  default MethodSpec generateSpec() {
    return MethodSpec.methodBuilder(methodName())
        .addAnnotations(annotationSpec())
        .addModifiers(modifiers())
        .returns(returnType())
        .build();
  }
}
