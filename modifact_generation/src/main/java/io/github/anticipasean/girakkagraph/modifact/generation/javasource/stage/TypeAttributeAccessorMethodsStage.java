package io.github.anticipasean.girakkagraph.modifact.generation.javasource.stage;

import com.squareup.javapoet.TypeSpec;
import cyclops.data.ImmutableList;
import io.github.anticipasean.girakkagraph.modifact.generation.DevelopmentStage;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.PropertyMethodSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.wiring.BeanStyleAccessorMethodSpecWiring;
import org.hibernate.mapping.PersistentClass;
import org.immutables.value.Value;

@Value.Immutable
public interface TypeAttributeAccessorMethodsStage extends TypeSpecStage {

  PersistentClass persistentClass();

  ImmutableList<JavaSourceAnnotationSpec> javaSourceAnnotationSpecs();

  @Override
  default DevelopmentStage<TypeSpec> nextDevelopmentStage() {
    ImmutableList<PropertyMethodSpec> methodSpecSeq =
        BeanStyleAccessorMethodSpecWiring.getInstance().apply(this);
    return DevelopmentStage.stop();
  }
}
