package io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.annotation;

import static cyclops.matching.Api.Case;
import static cyclops.matching.Api.Match;

import cyclops.control.Option;
import cyclops.data.Seq;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.AnnotationMemberSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.AnnotationMemberSpecImpl;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpecImpl;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Table;

public enum JpaColumnAnnotationSpecCreator implements Function<Property, JavaSourceAnnotationSpec> {
  INSTANCE;

  public static JpaColumnAnnotationSpecCreator getInstance() {
    return INSTANCE;
  }

  /**
   * from org.hibernate.tool.internal.util.JdbcToHibernateTypeHelper#typeHasLength(int)
   *
   * @param sqlType
   * @return
   */
  private static boolean typeHasLength(int sqlType) {
    return (sqlType == Types.CHAR
        || sqlType == Types.DATE
        || sqlType == Types.LONGVARCHAR
        || sqlType == Types.TIME
        || sqlType == Types.TIMESTAMP
        || sqlType == Types.VARCHAR);
  }

  private static boolean typeIsTemporal(int sqlType) {
    return (sqlType == Types.DATE || sqlType == Types.TIME || sqlType == Types.TIMESTAMP);
  }

  /**
   * from org.hibernate.tool.internal.util.JdbcToHibernateTypeHelper#typeHasScaleAndPrecision(int)
   *
   * @param sqlType
   * @return
   */
  private static boolean typeHasScaleAndPrecision(int sqlType) {
    return (sqlType == Types.DECIMAL
        || sqlType == Types.NUMERIC
        || sqlType == Types.REAL
        || sqlType == Types.FLOAT
        || sqlType == Types.DOUBLE);
  }

  /**
   * Basic Column: javax.persistence.Column
   *
   * <pre>
   * String name() default "";
   * boolean unique() default false;
   * boolean nullable() default true;
   * boolean insertable() default true;
   * boolean updatable() default true;
   * String columnDefinition() default "";
   * String table() default "";
   * int length() default 255;
   * int precision() default 0;
   * int scale() default 0;
   * </pre>
   */

  /**
   * Join Column: javax.persistence.JoinColumn
   *
   * <pre>
   * String name() default "";
   * String referencedColumnName() default "";
   * boolean unique() default false;
   * boolean nullable() default true;
   * boolean insertable() default true;
   * boolean updatable() default true;
   * String columnDefinition() default "";
   * String table() default "";
   * ForeignKey foreignKey() default @ForeignKey(PROVIDER_DEFAULT);
   * </pre>
   */
  @SuppressWarnings("unchecked")
  private static Option<AnnotationMemberSpec>
      getJoinColumnReferencedColumnNameAnnotationMemberSpecFromColumnOpt(Option<Column> columnOpt) {
    if (isJoinColumn(columnOpt)) {
      return getForeignKeyForJoinColumnOpt(columnOpt)
          .map(ForeignKey::getReferencedColumns)
          // Hibernate did not declare the generic type on that accessor method
          // so a cast is necessary
          .map(list -> (List<Column>) list)
          .flatMap(columnList -> Option.fromStream(columnList.stream()))
          .map(Column::getName)
          .map(columnName -> AnnotationMemberSpecImpl.of("referencedColumnName", columnName));
    }
    return Option.none();
  }

  /**
   * javax.persistence.ForeignKey
   *
   * <pre>
   * String name() default "";
   * ConstraintMode value() default CONSTRAINT;
   * String foreignKeyDefinition() default "";
   * </pre>
   */
  private static Option<AnnotationMemberSpec>
      getJoinColumnForeignKeyAnnotationMemberSpecFromColumnOpt(Option<Column> columnOpt) {

    if (isJoinColumn(columnOpt)) {
      Option<ForeignKey> foreignKeyOption = getForeignKeyForJoinColumnOpt(columnOpt);
      Seq<AnnotationMemberSpec> annotationMemberSpecs =
          Seq.of(
                  foreignKeyOption
                      .map(ForeignKey::getName)
                      .filter(Objects::nonNull)
                      .map(foreignKeyName -> AnnotationMemberSpecImpl.of("name", foreignKeyName)),
                  foreignKeyOption
                      .map(ForeignKey::getKeyDefinition)
                      .filter(Objects::nonNull)
                      .map(keyDef -> AnnotationMemberSpecImpl.of("foreignKeyDefinition", keyDef)))
              .filter(Option::isPresent)
              .map(annMemberSpecOpt -> annMemberSpecOpt.orElse(null));
      return Option.of(annotationMemberSpecs)
          .map(
              annMembSpecs ->
                  JavaSourceAnnotationSpecImpl.builder()
                      .annotationClass(javax.persistence.ForeignKey.class)
                      .annotationMemberSpecs(annMembSpecs)
                      .build())
          .map(JavaSourceAnnotationSpec::generateSpec)
          .map(annotationSpec -> AnnotationMemberSpecImpl.of("foreignKey", annotationSpec));
    }
    return Option.none();
  }

  private static Option<ForeignKey> getForeignKeyForJoinColumnOpt(Option<Column> columnOpt) {
    return columnOpt
        .map(Column::getValue)
        .filter(DependantValue.class::isInstance)
        .map(DependantValue.class::cast)
        .map(
            dependantValue ->
                Option.ofNullable(dependantValue.getTable())
                    .map(Table::getForeignKeys)
                    .map(Map::values)
                    .fold(Seq::fromIterable, Seq::<ForeignKey>empty)
                    .filter(
                        foreignKey ->
                            foreignKey.getColumns().equals(dependantValue.getConstraintColumns())))
        .orElseGet(Seq::<ForeignKey>empty)
        .headOption();
  }

  private static Option<AnnotationMemberSpec> getColumnNameAnnotationMemberSpecFromColumnOpt(
      Option<Column> columnOpt) {
    return columnOpt.map(Column::getName).map(name -> AnnotationMemberSpecImpl.of("name", name));
  }

  private static Option<AnnotationMemberSpec> getColumnUniqueAnnotationMemberSpecFromColumnOpt(
      Option<Column> columnOpt) {
    return columnOpt
        .filter(Column::isUnique)
        .map(column -> AnnotationMemberSpecImpl.of("unique", column.isUnique()));
  }

  private static Option<AnnotationMemberSpec> getColumnNullableAnnotationMemberSpecFromColumnOpt(
      Option<Column> columnOpt) {
    return columnOpt
        .filterNot(Column::isNullable)
        .map(column -> AnnotationMemberSpecImpl.of("nullable", column.isNullable()));
  }

  private static Option<AnnotationMemberSpec> getColumnInsertableAnnotationMemberSpecFromProperty(
      Property property) {
    return Option.ofNullable(property)
        .filter(prop -> !prop.getValue().isSimpleValue() || !prop.isInsertable())
        .map(Property::isInsertable)
        .map(insertable -> AnnotationMemberSpecImpl.of("insertable", insertable));
  }

  private static Option<AnnotationMemberSpec> getColumnUpdatableAnnotationMemberSpecFromProperty(
      Property property) {
    return Option.ofNullable(property)
        .filter(prop -> !prop.getValue().isSimpleValue() || !prop.isUpdateable())
        .map(Property::isUpdateable)
        .map(updatable -> AnnotationMemberSpecImpl.of("updatable", updatable));
  }

  private static Option<AnnotationMemberSpec>
      getColumnColumnDefinitionAnnotationMemberSpecFromColumnOpt(Option<Column> columnOpt) {
    return Match(columnOpt)
        .with(
            Case(
                columnOption ->
                    columnOption
                        .map(Column::getSqlTypeCode)
                        .filter(JpaColumnAnnotationSpecCreator::typeIsTemporal)
                        .isPresent(),
                columnOption ->
                    Option.some(AnnotationMemberSpecImpl.of("columnDefinition", "TIMESTAMP"))),
            //            Case(columnOption -> columnOption.map(Column::))
            columnOption -> Option.none());
  }

  private static Option<AnnotationMemberSpec> getColumnTableAnnotationMemberSpecFromColumnOpt(
      Option<Column> columnOpt) {
    return columnOpt.emptyUnit();
  }

  private static Option<AnnotationMemberSpec> getColumnLengthAnnotationMemberSpecFromColumnOpt(
      Option<Column> columnOpt) {
    return columnOpt
        .filter(
            column ->
                typeHasLength(column.getSqlTypeCode())
                    || column.getLength() != Column.DEFAULT_LENGTH)
        .map(column -> AnnotationMemberSpecImpl.of("length", column.getLength()));
  }

  private static Option<AnnotationMemberSpec> getColumnPrecisionAnnotationMemberSpecFromColumnOpt(
      Option<Column> columnOpt) {
    return columnOpt
        .filter(
            column ->
                typeHasScaleAndPrecision(column.getSqlTypeCode())
                    || column.getPrecision() != Column.DEFAULT_PRECISION)
        .map(column -> AnnotationMemberSpecImpl.of("precision", column.getPrecision()));
  }

  private static Option<AnnotationMemberSpec> getColumnScaleAnnotationMemberSpecFromColumnOpt(
      Option<Column> columnOpt) {
    return columnOpt
        .filter(
            column ->
                typeHasScaleAndPrecision(column.getSqlTypeCode())
                    || column.getScale() != Column.DEFAULT_SCALE)
        .map(column -> AnnotationMemberSpecImpl.of("scale", column.getScale()));
  }

  private static Seq<Column> getColumnSequenceFromProperty(Property property) {
    return Option.ofNullable(getSelectableIteratorFromPropertyOnPersistentClass(property))
        .fold(Seq::fromIterator, Seq::<Selectable>empty)
        .filter(selectable -> Column.class.isAssignableFrom(selectable.getClass()))
        .map(Column.class::cast);
  }

  @SuppressWarnings("unchecked")
  private static Iterator<Selectable> getSelectableIteratorFromPropertyOnPersistentClass(
      Property property) {
    return property.<Iterator<Selectable>>getColumnIterator();
  }

  private static boolean isJoinColumn(Option<Column> columnOpt) {
    return columnOpt.map(Column::getValue).filter(DependantValue.class::isInstance).isPresent();
  }

  @Override
  public JavaSourceAnnotationSpec apply(Property property) {
    Option<Column> columnOpt = getColumnSequenceFromProperty(property).headOption();
    Seq<AnnotationMemberSpec> annotationMemberSpecs =
        Seq.of(
                getColumnNameAnnotationMemberSpecFromColumnOpt(columnOpt),
                getColumnUniqueAnnotationMemberSpecFromColumnOpt(columnOpt),
                getColumnNullableAnnotationMemberSpecFromColumnOpt(columnOpt),
                getColumnInsertableAnnotationMemberSpecFromProperty(property),
                getColumnUpdatableAnnotationMemberSpecFromProperty(property),
                getColumnColumnDefinitionAnnotationMemberSpecFromColumnOpt(columnOpt),
                getColumnTableAnnotationMemberSpecFromColumnOpt(columnOpt),
                getColumnLengthAnnotationMemberSpecFromColumnOpt(columnOpt),
                getColumnPrecisionAnnotationMemberSpecFromColumnOpt(columnOpt),
                getColumnScaleAnnotationMemberSpecFromColumnOpt(columnOpt),
                getJoinColumnReferencedColumnNameAnnotationMemberSpecFromColumnOpt(columnOpt),
                getJoinColumnForeignKeyAnnotationMemberSpecFromColumnOpt(columnOpt))
            .filter(Option::isPresent)
            .map(annotationMemberSpecOpt -> annotationMemberSpecOpt.orElse(null));
    if (isJoinColumn(columnOpt)) {
      return JavaSourceAnnotationSpecImpl.builder()
          .annotationClass(javax.persistence.JoinColumn.class)
          .annotationMemberSpecs(annotationMemberSpecs)
          .build();
    } else {
      return JavaSourceAnnotationSpecImpl.builder()
          .annotationClass(javax.persistence.Column.class)
          .annotationMemberSpecs(annotationMemberSpecs)
          .build();
    }
  }
}
