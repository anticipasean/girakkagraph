package io.github.anticipasean.girakkagraph.modifact.generation.annotation;

import org.immutables.annotate.InjectAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@InjectAnnotation(
    type = javax.persistence.Embeddable.class,
    target = {InjectAnnotation.Where.MODIFIABLE_TYPE})
public @interface MakeEmbeddable {}
