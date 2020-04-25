package io.github.anticipasean.girakkagraph.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.immutables.annotate.InjectAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@InjectAnnotation(
    type = com.fasterxml.jackson.databind.annotation.JsonSerialize.class,
    target = {InjectAnnotation.Where.IMMUTABLE_TYPE})
public @interface MakeJsonable {}
