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
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.Property;

public enum JpaOneToManyAnnotationSpecCreator
    implements Function<Property, Option<JavaSourceAnnotationSpec>> {
  INSTANCE;

  public static JpaOneToManyAnnotationSpecCreator getInstance() {
    return INSTANCE;
  }

  private static Option<Collection> extractOneToManyCollectionValueFromProperty(Property property) {
    return Option.some(property).map(Property::getValue).ofType(Collection.class);
  }

  /**
   * javax.persistence.OneToMany:
   *
   * <pre>
   * Class targetEntity() default void.class;
   * CascadeType[] cascade() default {};
   * FetchType fetch() default LAZY;
   * String mappedBy() default "";
   * boolean orphanRemoval() default false;
   * </pre>
   */
  private static Option<AnnotationMemberSpec> createTargetEntityAnnotationMemberSpecWithProperty(
      Property property) {
    // TODO: replace refEntityNameString with class reference in this and ManyToOne
    return extractOneToManyCollectionValueFromProperty(property)
        .map(Collection::getElement)
        .ofType(OneToMany.class)
        .map(OneToMany::getReferencedEntityName)
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

  private static Option<AnnotationMemberSpec> createCascadeAnnotationMemberSpecWithProperty(
      Property property) {
    return Option.some(property)
        .map(Property::getCascade)
        .map(JpaOneToManyAnnotationSpecCreator::parseCascadeTypeArrayFromString)
        .filterNot(ImmutableList::isEmpty)
        .map(
            cascadeTypes ->
                AnnotationMemberSpecImpl.of("cascade", cascadeTypes.toArray(CascadeType[]::new)));
  }

  private static Option<AnnotationMemberSpec> createFetchAnnotationMemberSpecWithProperty(
      Property property) {
    return extractOneToManyCollectionValueFromProperty(property)
        .map(Collection::isLazy)
        .map(lazyFlag -> lazyFlag ? FetchType.LAZY : FetchType.EAGER)
        .map(fetchType -> AnnotationMemberSpecImpl.of("fetch", fetchType));
  }

  private static Option<AnnotationMemberSpec> createMappedByAnnotationMemberSpecWithProperty(
      Property property) {
    // TODO: add contextual parameter for pluralization and field naming consistency
    return extractOneToManyCollectionValueFromProperty(property)
        .map(Collection::getRole)
        .map(roleStr -> roleStr.indexOf(".") >= 0 ? roleStr.split("\\.")[1] : roleStr)
        .map(fieldName -> AnnotationMemberSpecImpl.of("mappedBy", fieldName));
  }

  private static Option<AnnotationMemberSpec> createOrphanRemovalAnnotationMemberSpecWithProperty(
      Property property) {
    return Option.none();
  }

  @Override
  public Option<JavaSourceAnnotationSpec> apply(Property property) {
    if (!extractOneToManyCollectionValueFromProperty(property).isPresent()) {
      return Option.none();
    }
    Seq<AnnotationMemberSpec> annotationMemberSpecs =
        Seq.of(
                createTargetEntityAnnotationMemberSpecWithProperty(property),
                createCascadeAnnotationMemberSpecWithProperty(property),
                createFetchAnnotationMemberSpecWithProperty(property),
                createMappedByAnnotationMemberSpecWithProperty(property),
                createOrphanRemovalAnnotationMemberSpecWithProperty(property))
            .filter(Option::isPresent)
            .map(annMemberSpecOpt -> annMemberSpecOpt.orElse(null));
    Option<JavaSourceAnnotationSpec> annotationSpecOption =
        Option.of(annotationMemberSpecs)
            .filterNot(Seq::isEmpty)
            .map(
                annMembSpecSeq ->
                    JavaSourceAnnotationSpecImpl.builder()
                        .annotationClass(javax.persistence.OneToMany.class)
                        .annotationMemberSpecs(annMembSpecSeq)
                        .build());
    return annotationSpecOption;
  }
}
