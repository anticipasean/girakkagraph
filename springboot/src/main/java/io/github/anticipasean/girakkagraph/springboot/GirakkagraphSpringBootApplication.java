package io.github.anticipasean.girakkagraph.springboot;

import io.github.anticipasean.girakkagraph.springboot.conf.PersistenceConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"io.github.anticipasean.girakkagraph"})
@EnableConfigurationProperties({PersistenceConfigurationProperties.class})
public class GirakkagraphSpringBootApplication {

  public static void main(String[] args) {
    SpringApplication.run(GirakkagraphSpringBootApplication.class, args);
  }
}
