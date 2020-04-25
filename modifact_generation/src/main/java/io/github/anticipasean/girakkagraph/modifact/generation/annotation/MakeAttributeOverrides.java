package io.github.anticipasean.girakkagraph.modifact.generation.annotation;

import org.immutables.annotate.InjectAnnotation;

import javax.persistence.AttributeOverride;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@InjectAnnotation(
    type = javax.persistence.AttributeOverrides.class,
    code = "([[*]])",
    target = {InjectAnnotation.Where.ACCESSOR})
public @interface MakeAttributeOverrides {
  @graphql.annotations.annotationTypes.GraphQLField
  AttributeOverride[] value();
}
