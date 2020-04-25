package io.github.anticipasean.girakkagraph.modifact.generation.annotation;

import org.immutables.annotate.InjectAnnotation;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@InjectAnnotation(
    type = javax.persistence.ManyToOne.class,
    target = {InjectAnnotation.Where.ACCESSOR},
    code =
        "(fetch = [[fetch]], cascade = [[cascade]], optional = [[optional]], targetEntity = [[targetEntity]])")
public @interface InjectManyToOne {
  CascadeType[] cascade() default {};

  FetchType fetch() default FetchType.EAGER;

  boolean optional() default true;

  String targetEntity() default "void.class";
}
