package io.github.anticipasean.girakkagraph.protocol.base.protocol;

import io.github.anticipasean.girakkagraph.protocol.base.actor.Actor;
import io.github.anticipasean.girakkagraph.protocol.base.role.Role;
import io.github.anticipasean.girakkagraph.protocol.base.role.Roles;

interface Protocol extends Actor {

  @Override
  default Role role() {
    return Roles.PROTOCOL;
  }
}
