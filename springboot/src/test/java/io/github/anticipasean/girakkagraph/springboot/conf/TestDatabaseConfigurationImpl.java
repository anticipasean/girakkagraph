package io.github.anticipasean.girakkagraph.springboot.conf;

import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConvention;
import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConventionFactory;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseConfigurer;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;

@Component
@Profile({"test"})
// @EntityScan(basePackages = {"io.github.anticipasean.girakkagraph.entities"})
public class TestDatabaseConfigurationImpl implements JpaConfiguration {

  private final List<String> packagesToScanForEntities;
  private final EntityNamingConventionFactory<?> entityNamingConventionFactory;

  @Autowired
  public TestDatabaseConfigurationImpl(
      PersistenceConfigurationProperties persistenceConfigurationProperties) {
    this.packagesToScanForEntities = persistenceConfigurationProperties.packagesToScanForEntities();
    LoggerFactory.getLogger(TestDatabaseConfigurationImpl.class)
        .info(
            "packagesToScanForEntities: [ "
                + Optional.ofNullable(packagesToScanForEntities)
                    .map(strings -> strings.stream().collect(Collectors.joining(", ")))
                    .orElse("_null_")
                + " ]");
    this.entityNamingConventionFactory =
        persistenceConfigurationProperties.entityNamingConventionFactory();
  }

  @Bean
  public EmbeddedDatabaseConnection embeddedDatabaseConnection() {
    return EmbeddedDatabaseConnection.H2;
  }

  @Override
  public DataSource dataSource() {
    EmbeddedDatabaseFactory embeddedDatabaseFactory = new EmbeddedDatabaseFactory();
    embeddedDatabaseFactory.setDatabaseConfigurer(
        new H2TestDatabaseConfigurer(embeddedDatabaseConnection()));
    embeddedDatabaseFactory.setDatabaseName("App");
    return embeddedDatabaseFactory.getDatabase();
  }

  @Override
  public EntityNamingConvention<?> entityNamingConvention() {
    return entityNamingConventionFactory.getEntityNamingConventionInstance();
  }

  @Override
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

    if (packagesToScanForEntities == null) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unable to create the entity "
                      + "manager factory, as no packages to scan for entities has been supplied");
      throw new IllegalStateException(messageSupplier.get());
    }

    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(true);

    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setJpaVendorAdapter(vendorAdapter);
    factory.setPackagesToScan(packagesToScanForEntities.toArray(new String[0]));
    factory.setDataSource(dataSource());
    return factory;
  }

  public static class H2TestDatabaseConfigurer implements EmbeddedDatabaseConfigurer {

    private EmbeddedDatabaseConnection embeddedDatabaseConnection;
    private Logger logger = LoggerFactory.getLogger(H2TestDatabaseConfigurer.class);

    public H2TestDatabaseConfigurer(EmbeddedDatabaseConnection embeddedDatabaseConnection) {
      this.embeddedDatabaseConnection = embeddedDatabaseConnection;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void configureConnectionProperties(
        ConnectionProperties properties, String databaseName) {
      try {
        properties.setDriverClass(
            (Class<? extends Driver>)
                Class.forName(embeddedDatabaseConnection.getDriverClassName()));
      } catch (ClassNotFoundException e) {
        logger.error("unable to find H2 driver for test database connection creation", e);
        throw new RuntimeException(e);
      }
      properties.setUrl(
          embeddedDatabaseConnection
              .getUrl(databaseName)
              .concat(";INIT=CREATE SCHEMA IF NOT EXISTS dbo"));
      properties.setUsername("sa");
      properties.setPassword("sa");
    }

    @Override
    public void shutdown(DataSource dataSource, String databaseName) {
      try {
        Objects.requireNonNull(
                dataSource.getConnection(),
                String.format("connection is null on datasource for database [%s]", databaseName))
            .close();
      } catch (SQLException e) {
        logger.error(
            String.format("unable to close datasource connection for database [%s]", databaseName),
            e);
      }
    }
  }
}
