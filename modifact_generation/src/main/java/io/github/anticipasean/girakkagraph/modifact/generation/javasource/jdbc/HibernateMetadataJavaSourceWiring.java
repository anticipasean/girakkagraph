package io.github.anticipasean.girakkagraph.modifact.generation.javasource.jdbc;

import com.squareup.javapoet.TypeSpec;
import cyclops.companion.Reducers;
import cyclops.control.Option;
import cyclops.data.LinkedMap;
import cyclops.data.Seq;
import cyclops.function.Lambda;
import io.github.anticipasean.girakkagraph.modifact.generation.DevelopmentStage;
import io.github.anticipasean.girakkagraph.modifact.generation.Modifact;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.stage.TypeAnnotationStageImpl;
import io.github.anticipasean.girakkagraph.modifact.generation.stage.JavaSourceStage;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum HibernateMetadataJavaSourceWiring
    implements Function<JavaSourceStage, DevelopmentStage<Modifact>> {
  INSTANCE;
  private final Logger logger;

  HibernateMetadataJavaSourceWiring() {
    this.logger = LoggerFactory.getLogger(HibernateMetadataJavaSourceWiring.class);
  }

  public static HibernateMetadataJavaSourceWiring getInstance() {
    return INSTANCE;
  }

  @Override
  public DevelopmentStage<Modifact> apply(JavaSourceStage javaSourceStage) {
    logger.info("processing hibernate metasource as javasource");
    logger.info(
        String.format(
            "entity_bindings: [ number: %d ]",
            javaSourceStage.hibernateMetadata().getEntityBindings().size()));
    Seq<PersistentClass> persistentClassSequence =
        Seq.fromIterable(javaSourceStage.hibernateMetadata().getEntityBindings());
    Option<PersistentClass> persistentClassOption =
        persistentClassSequence
            .filter(persistentClass -> persistentClass.getClassName().equals("SAccountE30"))
            .headOption();
    DevelopmentStage<TypeSpec> typeSpecStage =
        DevelopmentStage.stage(() -> determineTypeAnnotations(persistentClassOption));
    typeSpecStage.get();
    return DevelopmentStage.stop();
  }

  private DevelopmentStage<TypeSpec> determineTypeAnnotations(
      Option<PersistentClass> persistentClassOption) {
    if (!persistentClassOption.isPresent()) {
      return DevelopmentStage.stop();
    }
    PersistentClass persistentClass = persistentClassOption.orElse(null);
    return DevelopmentStage.stage(
        () ->
            TypeAnnotationStageImpl.builder()
                .persistentClass(persistentClass)
                .build()
                .nextDevelopmentStage());
  }

  private String stringifyMetaAttributes(Map rawMetaAttributesMap) {
    Map<String, Object> metaAttributes = rawMetaAttributesMap;
    if (metaAttributes == null) {
      return "";
    }
    LinkedMap<String, Object> metaAttributesImmutMap = LinkedMap.fromMap(metaAttributes);
    String mapAsString =
        metaAttributesImmutMap.foldMap(
            stringObjectTuple2 ->
                String.join(
                    ": ",
                    stringObjectTuple2._1(),
                    Option.ofNullable(stringObjectTuple2._2()).orElse("").toString()),
            Reducers.toString(",\n"));
    return mapAsString;
  }

  private void logAttributesForPersistentClass(PersistentClass persistentClass) {
    Seq<Property> propertySeq =
        extractPersistentClassAttributesAsHibernatePropertiesSequence(persistentClass);
    logger.info(
        "persistent_class: "
            + persistentClass.getClassName()
            + ", attributes: [\n"
            + propertySeq.map(Property::getName).intersperse(",\n").join()
            + "\n]");
    logger.info(
        "first_attribute: "
            + Seq.of(
                    Lambda.l1(Property::getName),
                    Lambda.l1(Property::getType),
                    Lambda.l1(Property::getValue),
                    Lambda.l1(Property::getValue)
                        .andThen(Value::getColumnIterator)
                        .andThen(Iterator::next))
                .zip(
                    Seq.of(propertySeq.headOrElse(null)).cycle(4),
                    (propertyFunction1, property) ->
                        Option.fromNullable(propertyFunction1.apply(property))
                            .map(Object::toString))
                .join(",\n"));
    propertySeq
        .map(
            property ->
                String.join(
                    ": ",
                    property.getName(),
                    stringifyMetaAttributes(property.getMetaAttributes())))
        .forEach(logger::info);
  }

  @SuppressWarnings("unchecked")
  private Seq<Property> extractPersistentClassAttributesAsHibernatePropertiesSequence(
      PersistentClass persistentClass) {
    return Seq.fromIterator(persistentClass.getPropertyIterator());
  }
}
