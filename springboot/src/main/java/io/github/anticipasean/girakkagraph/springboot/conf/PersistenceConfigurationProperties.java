package io.github.anticipasean.girakkagraph.springboot.conf;

import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConventionFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "girakkagraph")
@ConstructorBinding
public class PersistenceConfigurationProperties {

  private final List<String> packagesToScanForEntities;
  private final Class<? extends EntityNamingConventionFactory> entityNamingConventionFactoryClass;
  private final EntityNamingConventionFactory<?> entityNamingConventionFactory;

  public PersistenceConfigurationProperties(
      List<String> packagesToScanForEntities,
      Class<? extends EntityNamingConventionFactory> entityNamingConventionFactoryClass) {
    this.packagesToScanForEntities = packagesToScanForEntities;
    this.entityNamingConventionFactoryClass = entityNamingConventionFactoryClass;
    this.entityNamingConventionFactory = obtainFactoryFromClass(entityNamingConventionFactoryClass);
  }

  public EntityNamingConventionFactory<?> entityNamingConventionFactory() {
    return entityNamingConventionFactory;
  }

  private EntityNamingConventionFactory<?> obtainFactoryFromClass(
      Class<? extends EntityNamingConventionFactory> entityNamingConventionFactoryClass) {
    if (entityNamingConventionFactoryClass == null) {
      Supplier<String> messageSupplier =
          () ->
              "no Class of type EntityNamingConventionFactory "
                  + "was provided for understanding how entities in this service are named";
      throw new IllegalStateException(messageSupplier.get());
    }
    Method getInstanceMethodOnFactory =
        getEntityNamingConventionFactoryGetInstanceMethod(entityNamingConventionFactoryClass);
    return getEntityNamingConventionFactoryUsingMethod(getInstanceMethodOnFactory);
  }

  private Method getEntityNamingConventionFactoryGetInstanceMethod(
      Class<? extends EntityNamingConventionFactory> entityNamingConventionFactoryClass) {
    try {
      return Arrays.stream(entityNamingConventionFactoryClass.getMethods())
          .filter(method -> method.getName().equalsIgnoreCase("getInstance"))
          .findAny()
          .orElseThrow(NoSuchMethodException::new);
    } catch (NoSuchMethodException e) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "%s must declare a static factory method getInstance for itself\n"
                      + "methods actually declared on this class include: [ %s ]",
                  EntityNamingConventionFactory.class.getName(),
                  Arrays.stream(entityNamingConventionFactoryClass.getMethods())
                      .map(
                          method ->
                              String.format(
                                  "[ name: %s, params: %s ]",
                                  method.getName(),
                                  Arrays.stream(method.getParameters())
                                      .map(Parameter::getParameterizedType)
                                      .map(Type::getTypeName)
                                      .collect(Collectors.joining(", "))))
                      .collect(Collectors.joining(",\n\t")));
      throw new IllegalArgumentException(messageSupplier.get(), e);
    }
  }

  private EntityNamingConventionFactory<?> getEntityNamingConventionFactoryUsingMethod(
      Method getInstanceMethodOnFactory) {
    Object invocationResult = tryToInvokeGetInstanceMethodOnFactory(getInstanceMethodOnFactory);
    return castInvocationResultToEntityNamingConventionFactory(
        getInstanceMethodOnFactory, invocationResult);
  }

  private Object tryToInvokeGetInstanceMethodOnFactory(Method getInstanceMethodOnFactory) {
    try {
      return getInstanceMethodOnFactory.invoke(null);
    } catch (IllegalAccessException e) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "getInstance method [ %s ] on EntityNamingConventionFactor parameter is private.",
                  getInstanceMethodOnFactory.getName());
      throw new IllegalStateException(messageSupplier.get(), e);
    } catch (InvocationTargetException e) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unable to run getInstance method [ %s ] on EntityNamingConventionFactor parameter",
                  getInstanceMethodOnFactory.getName());
      throw new IllegalStateException(messageSupplier.get(), e);
    }
  }

  private EntityNamingConventionFactory<?> castInvocationResultToEntityNamingConventionFactory(
      Method getInstanceMethodOnFactory, Object invocationResult) {
    try {
      return EntityNamingConventionFactory.class.cast(invocationResult);
    } catch (ClassCastException e) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unable to cast invocation result [ %s ] of method [ %s ]",
                  invocationResult, getInstanceMethodOnFactory);
      throw new IllegalStateException(messageSupplier.get(), e);
    }
  }

  public List<String> packagesToScanForEntities() {
    return packagesToScanForEntities;
  }

  public Class<? extends EntityNamingConventionFactory> entityNamingConventionFactoryClass() {
    return entityNamingConventionFactoryClass;
  }
}
