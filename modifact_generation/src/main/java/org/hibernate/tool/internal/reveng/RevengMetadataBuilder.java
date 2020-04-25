/*
 * Created on 2004-11-23
 *
 */
package org.hibernate.tool.internal.reveng;

import java.util.Properties;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.internal.BootstrapContextImpl;
import org.hibernate.boot.internal.InFlightMetadataCollectorImpl;
import org.hibernate.boot.internal.MetadataBuilderImpl.MetadataBuildingOptionsImpl;
import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.mapping.Table;
import org.hibernate.tool.api.reveng.RevengDialect;
import org.hibernate.tool.api.reveng.RevengDialectFactory;
import org.hibernate.tool.api.reveng.RevengStrategy;
import org.hibernate.tool.internal.reveng.binder.BinderContext;
import org.hibernate.tool.internal.reveng.binder.RootClassBinder;
import org.hibernate.tool.internal.reveng.reader.DatabaseReader;
import org.jboss.logging.Logger;

/**
 * @author max
 * @author koen
 */
public class RevengMetadataBuilder {

  private static final Logger LOGGER = Logger.getLogger(RevengMetadataBuilder.class);
  private final Properties properties;
  private final MetadataBuildingContext metadataBuildingContext;
  private final InFlightMetadataCollectorImpl metadataCollector;
  private final RevengStrategy revengStrategy;
  private final BinderContext binderContext;
  private final StandardServiceRegistry serviceRegistry;

  private RevengMetadataBuilder(Properties properties, RevengStrategy reverseEngineeringStrategy) {
    this.properties = properties;
    this.revengStrategy = reverseEngineeringStrategy;
    this.serviceRegistry = new StandardServiceRegistryBuilder().applySettings(properties).build();
    MetadataBuildingOptionsImpl metadataBuildingOptions =
        new MetadataBuildingOptionsImpl(serviceRegistry);
    BootstrapContextImpl bootstrapContext =
        new BootstrapContextImpl(serviceRegistry, metadataBuildingOptions);
    metadataBuildingOptions.setBootstrapContext(bootstrapContext);
    this.metadataCollector =
        new InFlightMetadataCollectorImpl(bootstrapContext, metadataBuildingOptions);
    this.metadataBuildingContext =
        new MetadataBuildingContextRootImpl(
            bootstrapContext, metadataBuildingOptions, metadataCollector);
    this.binderContext =
        BinderContext.create(
            metadataBuildingContext, metadataCollector, reverseEngineeringStrategy, properties);
  }

  public static RevengMetadataBuilder create(
      Properties properties, RevengStrategy reverseEngineeringStrategy) {
    return new RevengMetadataBuilder(properties, reverseEngineeringStrategy);
  }

  public Metadata build() {
    Metadata result = createMetadata();
    createPersistentClasses(readFromDatabase());
    return result;
  }

  private MetadataImpl createMetadata() {
    MetadataImpl result = metadataCollector.buildMetadataInstance(metadataBuildingContext);
    result.getTypeConfiguration().scope(metadataBuildingContext);
    return result;
  }

  private RevengMetadataCollector readFromDatabase() {
    RevengDialect mdd =
        RevengDialectFactory.createMetaDataDialect(
            serviceRegistry.getService(JdbcServices.class).getDialect(), properties);
    DatabaseReader reader = DatabaseReader.create(properties, revengStrategy, mdd, serviceRegistry);
    RevengMetadataCollector revengMetadataCollector =
        new RevengMetadataCollector(metadataCollector);
    reader.readDatabaseSchema(revengMetadataCollector);
    return revengMetadataCollector;
  }

  // TODO: this naively just create an entity per table
  // should have an opt-out option to mark some as helper tables, subclasses etc.
  /*if(table.getPrimaryKey()==null || table.getPrimaryKey().getColumnSpan()==0) {
   log.warn("Cannot create persistent class for " + table + " as no primary key was found.");
      continue;
      // TODO: just create one big embedded composite id instead.
  }*/
  private void createPersistentClasses(RevengMetadataCollector revengMetadataCollector) {
    RootClassBinder rootClassBinder = RootClassBinder.create(binderContext);
    for (Table table : metadataCollector.collectTableMappings()) {
      if (table.getColumnSpan() == 0) {
        LOGGER.warn("Cannot create persistent class for " + table + " as no columns were found.");
        continue;
      }
      if (revengStrategy.isManyToManyTable(table)) {
        LOGGER.debug("Ignoring " + table + " as class since rev.eng. says it is a many-to-many");
        continue;
      }
      rootClassBinder.bind(table, revengMetadataCollector);
    }
    metadataCollector.processSecondPasses(metadataBuildingContext);
  }
}
