package io.github.anticipasean.girakkagraph.springboot.conf;

import graphql.spring.web.reactive.GraphQLInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
    basePackages = {"graphql.spring.web.reactive"},
    excludeFilters = {
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = GraphQLInvocation.class)
    })
@ConditionalOnBean(value = {GraphQLInvocation.class})
/**
 * ComponentScan.Filter must be added to exclude automatic use of GraphQLInvocation class since this
 * class must be overwritten to incorporate actor system and stream integration in the
 * implementation in this repo
 */
public class GraphQLConfiguration {

}
