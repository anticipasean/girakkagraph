package io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.annotation;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.AnnotationSpec.Builder;
import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.AnnotationMemberSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.AnnotationMemberSpecImpl;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpec;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec.JavaSourceAnnotationSpecImpl;
import java.util.Iterator;
import java.util.function.Function;
import javax.persistence.Index;
import javax.persistence.Table;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.RootClass;

public enum JpaTableAnnotationSpecCreator implements Function<RootClass, JavaSourceAnnotationSpec> {
  INSTANCE;

  public static JpaTableAnnotationSpecCreator getInstance() {
    return INSTANCE;
  }
  /**
   *
   *
   * <pre>
   * String 	catalog
   *     (Optional) The catalog of the table.
   * Index[] 	indexes
   *     (Optional) Indexes for the table.
   * String 	name
   *     (Optional) The name of the table.
   * String 	schema
   *     (Optional) The schema of the table.
   * UniqueConstraint[] 	uniqueConstraints
   *     (Optional) Unique constraints that are to be placed on the table.
   * </pre>
   *
   * @param rootClass
   * @return
   */
  private static JavaSourceAnnotationSpec createJpaTableAnnotationSpecForRootClass(
      RootClass rootClass) {
    JavaSourceAnnotationSpec tableAnnotationSpec =
        aggregateTuplesOfTableAnnotationSpecEntriesIntoOneAnnotationSpec(rootClass);
    return tableAnnotationSpec;
  }

  private static Option<AnnotationMemberSpec> getSchemaAnnotationMember(RootClass rootClass) {
    return Option.some(rootClass.getTable().getSchema())
        .map(schemaName -> Tuple2.of("schema", schemaName))
        .map(tuple2 -> tuple2.transform(AnnotationMemberSpecImpl::of));
  }

  private static Option<AnnotationMemberSpec> getTableNameAnnotationMember(RootClass rootClass) {
    return Option.some(rootClass.getTable().getName())
        .map(tableName -> Tuple2.of("name", tableName))
        .map(tuple2 -> tuple2.transform(AnnotationMemberSpecImpl::of));
  }

  private static Seq<AnnotationMemberSpec> getIndexesAnnotationMemberSpecs(RootClass rootClass) {
    return createIndexArrayForTypeAnnotation(rootClass.getTable().getIndexIterator());
  }

  private static Option<AnnotationMemberSpec> getCatalogAnnotationMember(RootClass rootClass) {
    return Option.fromNullable(rootClass.getTable().getCatalog())
        .map(catalogName -> Tuple2.of("catalog", catalogName))
        .map(tuple2 -> tuple2.transform(AnnotationMemberSpecImpl::of));
  }

  private static JavaSourceAnnotationSpec
      aggregateTuplesOfTableAnnotationSpecEntriesIntoOneAnnotationSpec(RootClass rootClass) {
    JavaSourceAnnotationSpecImpl.Builder jpaTableAnnotationSpecBuilder =
        JavaSourceAnnotationSpecImpl.builder().annotationClass(Table.class);
    Seq<AnnotationMemberSpec> jpaTableAnnotationMemberSpecs =
        Seq.<Option<AnnotationMemberSpec>>empty()
            .append(getTableNameAnnotationMember(rootClass))
            .append(getCatalogAnnotationMember(rootClass))
            .append(getSchemaAnnotationMember(rootClass))
            .filter(Option::isPresent)
            .flatMap(annMemberSpecOpt -> Seq.of(annMemberSpecOpt.orElse(null)))
            .appendAll(getIndexesAnnotationMemberSpecs(rootClass));
    return jpaTableAnnotationSpecBuilder
        .annotationMemberSpecs(jpaTableAnnotationMemberSpecs)
        .build();
  }

  private static Seq<AnnotationMemberSpec> createIndexArrayForTypeAnnotation(
      Iterator<org.hibernate.mapping.Index> indexIterator) {
    return Seq.fromIterator(indexIterator)
        .map(index -> Tuple2.of(index.getName(), Seq.fromIterator(index.getColumnIterator())))
        .map(tuple -> tuple.map2(columns -> columns.map(Column::getName).join(",")))
        // .peek(tuple -> logger.info("index:[ name: " + tuple._1() + ", column_list: " +
        // tuple._2()))
        .map(
            tuple ->
                tuple.bimap(
                    indexName ->
                        AnnotationMemberSpecImpl.builder().name("name").value(indexName).build(),
                    columnListValue ->
                        AnnotationMemberSpecImpl.builder()
                            .name("columnList")
                            .value(columnListValue)
                            .build()))
        .map(
            tuple ->
                tuple
                    ._1()
                    .annotationMemberAnnotationSpecBuilderUpdater()
                    .andThen(tuple._2().annotationMemberAnnotationSpecBuilderUpdater())
                    .apply(AnnotationSpec.builder(Index.class)))
        .map(Builder::build)
        .map(annotationSpec -> AnnotationMemberSpecImpl.of("indexes", annotationSpec));
  }

  @Override
  public JavaSourceAnnotationSpec apply(RootClass rootClass) {
    return createJpaTableAnnotationSpecForRootClass(rootClass);
  }
}
