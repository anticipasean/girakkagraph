package io.github.anticipasean.girakkagraph.protocol.base.service;

import io.github.anticipasean.girakkagraph.protocol.base.actor.Actor;
import io.github.anticipasean.girakkagraph.protocol.base.role.Role;
import io.github.anticipasean.girakkagraph.protocol.base.role.Roles;

public interface ActorRouterService extends Actor {

  @Override
  default Role role() {
    return Roles.SERVICE_PROVIDER;
  }
}
