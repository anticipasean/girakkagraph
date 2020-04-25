package io.github.anticipasean.girakkagraph.modifact.generation.javasource.wiring;

import static cyclops.matching.Api.Case;
import static cyclops.matching.Api.Match;

import cyclops.control.Try;
import cyclops.data.ImmutableList;
import cyclops.data.Seq;
import io.github.anticipasean.girakkagraph.modifact.generation.annotation.MakeEntity;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpecImpl;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.annotation.JpaTableAnnotationSpecCreator;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.stage.TypeAnnotationStage;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TypeAnnotationWiring
    implements Function<TypeAnnotationStage, ImmutableList<JavaSourceAnnotationSpec>> {
  INSTANCE;
  private static Logger logger = LoggerFactory.getLogger(TypeAnnotationWiring.class);

  public static TypeAnnotationWiring getInstance() {
    return INSTANCE;
  }

  private static <T> Predicate<T> isOfType(Class<?> expectedType) {
    return o -> o != null && expectedType.isAssignableFrom(o.getClass());
  }

  private static <T> Try<T, ClassCastException> tryDynamicCast(
      Class<T> expectedType, Object value) {
    return Try.withCatch(() -> expectedType.cast(value), ClassCastException.class);
  }

  private static Seq<JavaSourceAnnotationSpec> generateAnnotationSpecsForRootClass(
      PersistentClass persistentClass) {
    Try<RootClass, ClassCastException> rootClassTry =
        tryDynamicCast(RootClass.class, persistentClass);
    if (rootClassTry.isFailure()) {
      rootClassTry.onFail(
          e ->
              logger.error(
                  String.format(
                      "unable to cast persistent class [ %s ] as root class type: %s",
                      persistentClass.getClassName(), RootClass.class.getName()),
                  e));
      return Seq.<JavaSourceAnnotationSpec>empty();
    }
    RootClass rootClass = rootClassTry.orElse(null);
    JavaSourceAnnotationSpec tableAnnotationSpecForRootClass =
        JpaTableAnnotationSpecCreator.getInstance().apply(rootClass);
    Seq<JavaSourceAnnotationSpec> immutablesProjectAnnotations =
        getImmutablesProjectAnnotationSpecsForTypeSpec();
    return Seq.cons(tableAnnotationSpecForRootClass, immutablesProjectAnnotations);
  }

  private static Seq<JavaSourceAnnotationSpec> getImmutablesProjectAnnotationSpecsForTypeSpec() {
    return Seq.of(
        JavaSourceAnnotationSpecImpl.builder().annotationClass(MakeEntity.class).build(),
        JavaSourceAnnotationSpecImpl.builder().annotationClass(Value.Immutable.class).build(),
        JavaSourceAnnotationSpecImpl.builder().annotationClass(Value.Modifiable.class).build());
  }

  private static Seq<JavaSourceAnnotationSpec> logUnsupportedTypeAnnotationWiring(
      PersistentClass persistentClass) {
    Supplier<String> messageSupplier =
        () ->
            String.format(
                "no case has been established for handling type [ %s ] for type "
                    + "annotation wiring for persistent class instance [ %s ]",
                persistentClass.getClass().getName(), persistentClass);
    logger.warn(messageSupplier.get());
    return Seq.<JavaSourceAnnotationSpec>empty();
  }

  @Override
  public ImmutableList<JavaSourceAnnotationSpec> apply(TypeAnnotationStage typeAnnotationStage) {
    return Match(typeAnnotationStage.persistentClass())
        .with(
            Case(
                isOfType(RootClass.class),
                TypeAnnotationWiring::generateAnnotationSpecsForRootClass),
            TypeAnnotationWiring::logUnsupportedTypeAnnotationWiring);
  }
}
