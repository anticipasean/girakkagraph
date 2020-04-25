package io.github.anticipasean.girakkagraph.style.naming;

public abstract class EntityNamingConventionFactory<T> {

  protected EntityNamingConventionFactory() {

  }

  public abstract EntityNamingConvention<T> getEntityNamingConventionInstance();

}
