package io.github.anticipasean.girakkagraph.springboot.conf;

import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConvention;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public interface JpaConfiguration {

  @Bean
  DataSource dataSource();

  @Bean(name = "entityNamingConvention")
  EntityNamingConvention<?> entityNamingConvention();

  @Bean
  LocalContainerEntityManagerFactoryBean entityManagerFactory();

  @Bean
  default PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager txManager = new JpaTransactionManager();
    txManager.setEntityManagerFactory(entityManagerFactory);
    return txManager;
  }
}
