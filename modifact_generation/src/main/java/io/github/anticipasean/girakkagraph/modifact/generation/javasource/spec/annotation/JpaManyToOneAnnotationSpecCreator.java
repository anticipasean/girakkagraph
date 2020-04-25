package io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.annotation;

import cyclops.control.Option;
import cyclops.data.HashSet;
import cyclops.data.ImmutableList;
import cyclops.data.Seq;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.AnnotationMemberSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.AnnotationMemberSpecImpl;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpecImpl;
import java.util.Arrays;
import java.util.function.Function;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.Property;

public enum JpaManyToOneAnnotationSpecCreator
    implements Function<Property, Option<JavaSourceAnnotationSpec>> {
  INSTANCE;

  public static JpaManyToOneAnnotationSpecCreator getInstance() {
    return INSTANCE;
  }

  private static Option<ManyToOne> extractManyToOneValueFromProperty(Property property) {
    return Option.of(property).map(Property::getValue).ofType(ManyToOne.class);
  }

  /**
   * {@code @}javax.persistence.ManyToOne:
   *
   * <pre>
   * Class targetEntity() default void.class;
   * CascadeType[] cascade() default {};
   * FetchType fetch() default EAGER;
   * boolean optional() default true;
   * </pre>
   */
  private static Option<AnnotationMemberSpec> createTargetEntityAnnotationMemberSpecFromProperty(
      Property property) {
    return extractManyToOneValueFromProperty(property)
        .map(ManyToOne::getReferencedEntityName)
        .map(refEntityName -> AnnotationMemberSpecImpl.of("targetEntity", refEntityName));
  }

  private static ImmutableList<CascadeType> parseCascadeTypeArrayFromString(
      String cascadeTypeArrStr) {
    HashSet<String> cascadeTypeNameSet =
        Seq.fromStream(Arrays.stream(CascadeType.values())).map(CascadeType::name).toHashSet();
    return Seq.fromStream(Arrays.stream(cascadeTypeArrStr.split(",")))
        .map(String::toUpperCase)
        .filter(cascadeTypeNameSet::contains)
        .map(CascadeType::valueOf);
  }

  private static Option<AnnotationMemberSpec> createCascadeAnnotationMemberSpecFromProperty(
      Property property) {
    return Option.some(property)
        .map(Property::getCascade)
        .map(JpaManyToOneAnnotationSpecCreator::parseCascadeTypeArrayFromString)
        .filterNot(ImmutableList::isEmpty)
        .map(
            cascadeTypes ->
                AnnotationMemberSpecImpl.of("cascade", cascadeTypes.toArray(CascadeType[]::new)));
  }

  private static Option<AnnotationMemberSpec> createFetchAnnotationMemberSpecFromProperty(
      Property property) {
    return extractManyToOneValueFromProperty(property)
        .map(ManyToOne::isLazy)
        .map(lazyFlag -> lazyFlag ? FetchType.LAZY : FetchType.EAGER)
        .map(fetchType -> AnnotationMemberSpecImpl.of("fetch", fetchType));
  }

  private static Option<AnnotationMemberSpec> createOptionalAnnotationMemberSpecFromProperty(
      Property property) {
    return Option.of(property)
        .map(Property::isOptional)
        .map(optional -> AnnotationMemberSpecImpl.of("optional", optional));
  }

  @Override
  public Option<JavaSourceAnnotationSpec> apply(Property property) {
    Option<ManyToOne> manyToOneOption = extractManyToOneValueFromProperty(property);
    if (manyToOneOption.isPresent()) {
      Seq<AnnotationMemberSpec> annotationMemberSpecs =
          gatherAnnotationMemberSpecsForManyToOneValueOnProperty(property);
      return Option.some(annotationMemberSpecs)
          .map(
              annotationMemberSpecsSeq ->
                  JavaSourceAnnotationSpecImpl.builder()
                      .annotationClass(javax.persistence.ManyToOne.class)
                      .annotationMemberSpecs(annotationMemberSpecsSeq)
                      .build());
    }
    return Option.none();
  }

  @SuppressWarnings("unchecked")
  private static Seq<AnnotationMemberSpec> gatherAnnotationMemberSpecsForManyToOneValueOnProperty(
      Property property) {
    return Seq.<Option<AnnotationMemberSpec>>of(
            createTargetEntityAnnotationMemberSpecFromProperty(property),
            createCascadeAnnotationMemberSpecFromProperty(property),
            createFetchAnnotationMemberSpecFromProperty(property),
            createOptionalAnnotationMemberSpecFromProperty(property))
        .filter(Option::isPresent)
        .map(annMembSpecOpt -> annMembSpecOpt.orElse(null));
  }
}
