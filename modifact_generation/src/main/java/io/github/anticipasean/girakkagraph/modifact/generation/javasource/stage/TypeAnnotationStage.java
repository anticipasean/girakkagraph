package io.github.anticipasean.girakkagraph.modifact.generation.javasource.stage;

import com.squareup.javapoet.TypeSpec;
import cyclops.data.ImmutableList;
import io.github.anticipasean.girakkagraph.modifact.generation.DevelopmentStage;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.wiring.TypeAnnotationWiring;
import org.hibernate.mapping.PersistentClass;
import org.immutables.value.Value;

@Value.Immutable
public interface TypeAnnotationStage extends TypeSpecStage {

  PersistentClass persistentClass();

  @Override
  default DevelopmentStage<TypeSpec> nextDevelopmentStage() {
    ImmutableList<JavaSourceAnnotationSpec> javaSourceAnnotationSpecs =
        TypeAnnotationWiring.getInstance().apply(this);
    return DevelopmentStage.stage(
        () ->
            TypeAttributeAccessorMethodsStageImpl.builder()
                .persistentClass(persistentClass())
                .javaSourceAnnotationSpecs(javaSourceAnnotationSpecs)
                .build()
                .nextDevelopmentStage());
  }
}
