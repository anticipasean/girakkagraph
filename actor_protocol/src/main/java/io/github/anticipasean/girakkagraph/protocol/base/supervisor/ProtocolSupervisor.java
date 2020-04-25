package io.github.anticipasean.girakkagraph.protocol.base.supervisor;

import io.github.anticipasean.girakkagraph.protocol.base.actor.Actor;
import io.github.anticipasean.girakkagraph.protocol.base.role.Role;
import io.github.anticipasean.girakkagraph.protocol.base.role.Roles;

public interface ProtocolSupervisor extends Actor {

  @Override
  default Role role() {
    return Roles.PROTOCOL_SUPERVISOR;
  }
}
