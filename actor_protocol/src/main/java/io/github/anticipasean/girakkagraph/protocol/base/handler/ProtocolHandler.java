package io.github.anticipasean.girakkagraph.protocol.base.handler;

import io.github.anticipasean.girakkagraph.protocol.base.actor.Actor;
import io.github.anticipasean.girakkagraph.protocol.base.role.Role;
import io.github.anticipasean.girakkagraph.protocol.base.role.Roles;

public interface ProtocolHandler extends Actor {
  @Override
  default Role role() {
    return Roles.PROTOCOL_HANDLER;
  }
}
