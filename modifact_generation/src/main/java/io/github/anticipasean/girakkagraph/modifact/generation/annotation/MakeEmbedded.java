package io.github.anticipasean.girakkagraph.modifact.generation.annotation;

import org.immutables.annotate.InjectAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@InjectAnnotation(
    type = javax.persistence.Embedded.class,
    target = {InjectAnnotation.Where.ACCESSOR})
public @interface MakeEmbedded {}
