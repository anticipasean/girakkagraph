package io.github.anticipasean.girakkagraph.modifact.generation.annotation;

import org.immutables.annotate.InjectAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
@InjectAnnotation(
    type = javax.persistence.Transient.class,
    target = InjectAnnotation.Where.SYNTHETIC_FIELDS)
public @interface MakeSyntheticFieldsTransient {}
