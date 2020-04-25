package io.github.anticipasean.girakkagraph.modifact.generation.javasource.wiring;

import com.squareup.javapoet.AnnotationSpec;
import cyclops.control.Option;
import cyclops.data.ImmutableList;
import cyclops.data.Seq;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.PropertyMethodSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.annotation.JpaColumnAnnotationSpecCreator;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.annotation.JpaManyToOneAnnotationSpecCreator;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.annotation.JpaOneToManyAnnotationSpecCreator;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.annotation.JpaPrimaryKeyColumnAnnotationSpecCreator;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.stage.TypeAttributeAccessorMethodsStage;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum BeanStyleAccessorMethodSpecWiring
    implements Function<TypeAttributeAccessorMethodsStage, ImmutableList<PropertyMethodSpec>> {
  INSTANCE;
  private final Logger logger = LoggerFactory.getLogger(BeanStyleAccessorMethodSpecWiring.class);

  public static BeanStyleAccessorMethodSpecWiring getInstance() {
    return INSTANCE;
  }

  @Override
  public ImmutableList<PropertyMethodSpec> apply(
      TypeAttributeAccessorMethodsStage typeAttributeAccessorMethodsStage) {
    PersistentClass persistentClass = typeAttributeAccessorMethodsStage.persistentClass();
    logger.info("persistent_class: " + persistentClass.getClassName());
    Seq<PropertyMethodSpec> accessorMethodSpecs =
        generateAccessorMethodSpecsFromPropertiesOnPersistentClass(persistentClass);
    return accessorMethodSpecs;
  }

  private Seq<PropertyMethodSpec> generateAccessorMethodSpecsFromPropertiesOnPersistentClass(
      PersistentClass persistentClass) {
    Seq<Property> hibernatePropertySequence =
        getPropertySequenceFromPersistentClass(persistentClass);

    logger.info(String.format("root_table: %s", persistentClass.getRootTable()));
    Option.ofNullable(persistentClass.getRootTable())
        .map(Table::getPrimaryKey)
        .peek(
            primaryKey -> {
              logger.info("primary_key: " + primaryKey);
            });
    Option.ofNullable(persistentClass.getRootTable())
        .map(Table::getIdentifierValue)
        .peek(
            keyValue -> {
              logger.info("identifier_value: " + keyValue);
            });
    Seq<ForeignKey> foreignKeySeq =
        Option.ofNullable(persistentClass.getRootTable())
            .map(Table::getForeignKeys)
            .map(Map::values)
            .map(Seq::fromIterable)
            .orElseGet(Seq::empty)
            .peek(
                foreignKey -> {
                  logger.info("foreign_key: " + foreignKey.toString());
                });
    getPropertySequenceFromPersistentClass(persistentClass)
        .filter(prop -> prop.getValue() instanceof ManyToOne)
        .map(JpaManyToOneAnnotationSpecCreator.getInstance())
        .peek(
            javaSourceAnnotationSpecOption -> {
              logger.info(
                  "many_to_one_annot: "
                      + javaSourceAnnotationSpecOption
                          .map(JavaSourceAnnotationSpec::generateSpec)
                          .map(AnnotationSpec::toString)
                          .orElse(""));
            });
    getPropertySequenceFromPersistentClass(persistentClass)
        .filter(
            prop ->
                !prop.getValue().getClass().isAssignableFrom(SimpleValue.class)
                    && !prop.getValue().getClass().isAssignableFrom(ManyToOne.class))
        .map(JpaOneToManyAnnotationSpecCreator.getInstance())
        .peek(
            javaSourceAnnotationSpecs -> {
              logger.info(
                  "one_to_many_option: "
                      + javaSourceAnnotationSpecs
                          .map(JavaSourceAnnotationSpec::generateSpec)
                          .map(Object::toString)
                          .orElse(""));
            });
    Option.ofNullable(persistentClass.getRootTable())
        .map(Table::getColumnIterator)
        .map(iterator -> (Iterator<Column>) iterator)
        .fold(Seq::fromIterator, Seq::<Column>empty)
        .peek(
            column -> {
              logger.info(
                  String.format(
                      "column: [ \n\tname: %s,\n\tcanonical_name: %s,\n\tvalue: %s\n]",
                      column.getName(), column.getCanonicalName(), column.getValue()));
            });
    Option<ImmutableList<JavaSourceAnnotationSpec>> primaryKeyColumnAnnotationSpecs =
        Option.of(persistentClass.getTable())
            .map(Table::getPrimaryKey)
            .map(JpaPrimaryKeyColumnAnnotationSpecCreator.getInstance());
    ImmutableList<JavaSourceAnnotationSpec> columnAnnotationSpecs =
        hibernatePropertySequence.map(JpaColumnAnnotationSpecCreator.getInstance());

    // TODO: pick up here
    return Seq.empty();
  }

  @SuppressWarnings("unchecked")
  private void logJoins(PersistentClass persistentClass) {
    Option.ofNullable(persistentClass.getJoinIterator())
        .map(iterator -> (Iterator<Join>) iterator)
        .fold(Seq::<Join>fromIterator, Seq::<Join>empty)
        .peek(
            join -> {
              logger.info("join: " + join.toString());
            });
  }

  @SuppressWarnings("unchecked")
  private Seq<Property> getPropertySequenceFromPersistentClass(PersistentClass persistentClass) {
    return Seq.fromIterator(persistentClass.<Iterator<Property>>getPropertyIterator());
  }
}
