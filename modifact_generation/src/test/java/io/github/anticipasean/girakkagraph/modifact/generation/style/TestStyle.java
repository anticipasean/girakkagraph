package io.github.anticipasean.girakkagraph.modifact.generation.style;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.immutables.value.Value;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS) // Make it class retention for incremental compilation
@Value.Style(
    get = {"is*", "get*"}, // Detect 'get' and 'is' prefixes in accessor methods
    //        init = "set*", // Builder initialization methods will have 'set' prefix
    typeAbstract = {"*"}, // 'Base' prefix will be detected and trimmed
    typeImmutable = "*Impl", // No prefix or suffix for generated immutable type
    //        builder = "new", // construct builder using 'new' instead of factory method
    //        build = "create", // rename 'build' method on builder to 'create'
    typeModifiable = "*Jpa",
    beanFriendlyModifiables = true,
    depluralizeDictionary = {"entry:entries", "policy:policies"},
    isInitialized = "initialized",
    validationMethod = Value.Style.ValidationMethod.NONE,
    overshadowImplementation = true,
    visibility =
        Value.Style.ImplementationVisibility.PUBLIC, // Generated class will be always public
    //    defaults = @Value.Immutable(copy = false, ) // Disable copy methods by default
    //    privateNoargConstructor = true,
    packageGenerated = "*.impl",
    passAnnotations = {
      org.hibernate.annotations.Target.class,
      org.hibernate.annotations.CreationTimestamp.class,
      org.hibernate.annotations.UpdateTimestamp.class,
      org.hibernate.annotations.Formula.class,
      javax.persistence.Version.class,
      javax.persistence.UniqueConstraint.class,
      javax.persistence.Transient.class,
      javax.persistence.Temporal.class,
      javax.persistence.TableGenerator.class,
      javax.persistence.Table.class,
      javax.persistence.StoredProcedureParameter.class,
      javax.persistence.SqlResultSetMappings.class,
      javax.persistence.SqlResultSetMapping.class,
      javax.persistence.SequenceGenerator.class,
      javax.persistence.SecondaryTables.class,
      javax.persistence.SecondaryTable.class,
      javax.persistence.QueryHint.class,
      javax.persistence.PrimaryKeyJoinColumns.class,
      javax.persistence.PrimaryKeyJoinColumn.class,
      javax.persistence.PreUpdate.class,
      javax.persistence.PreRemove.class,
      javax.persistence.PrePersist.class,
      javax.persistence.PostUpdate.class,
      javax.persistence.PostRemove.class,
      javax.persistence.PostPersist.class,
      javax.persistence.PostLoad.class,
      javax.persistence.PersistenceUnits.class,
      javax.persistence.PersistenceUnit.class,
      javax.persistence.PersistenceProperty.class,
      javax.persistence.PersistenceContexts.class,
      javax.persistence.PersistenceContext.class,
      javax.persistence.OrderColumn.class,
      javax.persistence.OrderBy.class,
      javax.persistence.OneToOne.class,
      javax.persistence.OneToMany.class,
      javax.persistence.NamedSubgraph.class,
      javax.persistence.NamedStoredProcedureQuery.class,
      javax.persistence.NamedStoredProcedureQueries.class,
      javax.persistence.NamedQuery.class,
      javax.persistence.NamedQueries.class,
      javax.persistence.NamedNativeQuery.class,
      javax.persistence.NamedNativeQueries.class,
      javax.persistence.NamedEntityGraphs.class,
      javax.persistence.NamedEntityGraph.class,
      javax.persistence.NamedAttributeNode.class,
      javax.persistence.MapsId.class,
      javax.persistence.MappedSuperclass.class,
      javax.persistence.MapKeyTemporal.class,
      javax.persistence.MapKeyJoinColumns.class,
      javax.persistence.MapKeyJoinColumn.class,
      javax.persistence.MapKeyEnumerated.class,
      javax.persistence.MapKeyColumn.class,
      javax.persistence.MapKeyClass.class,
      javax.persistence.MapKey.class,
      javax.persistence.ManyToOne.class,
      javax.persistence.ManyToMany.class,
      javax.persistence.Lob.class,
      javax.persistence.JoinTable.class,
      javax.persistence.JoinColumns.class,
      javax.persistence.JoinColumn.class,
      javax.persistence.Inheritance.class,
      javax.persistence.Index.class,
      javax.persistence.IdClass.class,
      javax.persistence.Id.class,
      javax.persistence.GeneratedValue.class,
      javax.persistence.ForeignKey.class,
      javax.persistence.FieldResult.class,
      javax.persistence.ExcludeSuperclassListeners.class,
      javax.persistence.ExcludeDefaultListeners.class,
      javax.persistence.Enumerated.class,
      javax.persistence.EntityResult.class,
      javax.persistence.EntityListeners.class,
      javax.persistence.Entity.class,
      javax.persistence.EmbeddedId.class,
      javax.persistence.Embedded.class,
      javax.persistence.Embeddable.class,
      javax.persistence.ElementCollection.class,
      javax.persistence.DiscriminatorValue.class,
      javax.persistence.DiscriminatorColumn.class,
      javax.persistence.Converts.class,
      javax.persistence.Converter.class,
      javax.persistence.Convert.class,
      javax.persistence.ConstructorResult.class,
      javax.persistence.ColumnResult.class,
      javax.persistence.Column.class,
      javax.persistence.CollectionTable.class,
      javax.persistence.Cacheable.class,
      javax.persistence.Basic.class,
      javax.persistence.AttributeOverrides.class,
      javax.persistence.AttributeOverride.class,
      javax.persistence.AssociationOverrides.class,
      javax.persistence.AssociationOverride.class,
      javax.persistence.Access.class
    })
public @interface TestStyle {}
