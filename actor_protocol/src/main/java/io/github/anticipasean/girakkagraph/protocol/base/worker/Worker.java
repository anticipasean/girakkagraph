package io.github.anticipasean.girakkagraph.protocol.base.worker;

import io.github.anticipasean.girakkagraph.protocol.base.actor.Actor;
import io.github.anticipasean.girakkagraph.protocol.base.role.Role;
import io.github.anticipasean.girakkagraph.protocol.base.role.Roles;

public interface Worker extends Actor {

  @Override
  default Role role() {
    return Roles.WORKER;
  }
}
