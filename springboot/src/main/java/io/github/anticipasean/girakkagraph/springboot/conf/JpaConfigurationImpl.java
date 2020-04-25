package io.github.anticipasean.girakkagraph.springboot.conf;

import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConvention;
import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConventionFactory;
import java.util.List;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;

@Component
@Profile({"default"})
// @EnableJpaRepositories
// @EntityScan(basePackages = {"io.github.anticipasean.girakkagraph.entities"})
public class JpaConfigurationImpl implements JpaConfiguration {

  private DataSource dataSource;
  private List<String> packagesToScanForEntities;
  private EntityNamingConventionFactory<?> entityNamingConventionFactory;

  @Autowired
  private JpaConfigurationImpl(
      DataSource dataSource,
      PersistenceConfigurationProperties persistenceConfigurationProperties) {
    this.dataSource = dataSource;
    this.packagesToScanForEntities = persistenceConfigurationProperties.packagesToScanForEntities();
    this.entityNamingConventionFactory =
        persistenceConfigurationProperties.entityNamingConventionFactory();
  }

  @Override
  public DataSource dataSource() {
    return dataSource;
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
    vendorAdapter.setGenerateDdl(false);

    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setJpaVendorAdapter(vendorAdapter);
    factory.setPackagesToScan(packagesToScanForEntities.toArray(new String[0]));
    factory.setDataSource(dataSource);
    return factory;
  }

}
