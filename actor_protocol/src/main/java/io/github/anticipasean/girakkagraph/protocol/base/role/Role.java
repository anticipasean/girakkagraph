package io.github.anticipasean.girakkagraph.protocol.base.role;

import java.util.function.UnaryOperator;

/**
 * Actors "play" or in this case, have a role and fall into a hierarchy based on the role they have.
 * Roles also provide a naming convention to help identify actors in the system they're in.
 */
public interface Role {

  /**
   * Name of the role the actor has
   *
   * @return name of the role
   */
  String name();

  /**
   * Way of obtaining a role based name
   *
   * @return function that takes an identifier string appending to it the "slugified" name of the
   *     role
   */
  UnaryOperator<String> actorNamingBasedOnRole();
}
