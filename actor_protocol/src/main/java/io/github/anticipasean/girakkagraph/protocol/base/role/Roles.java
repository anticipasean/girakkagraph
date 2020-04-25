package io.github.anticipasean.girakkagraph.protocol.base.role;

import com.google.common.base.CaseFormat;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.WaitingDelegateActor;
import java.util.function.UnaryOperator;

public enum Roles implements Role {
  /**
   * Supervises the creation of and any exceptions not handled by a particular protocol, including
   * whether to restart that protocol when its state has gone awry
   */
  PROTOCOL_SUPERVISOR,
  /**
   * Creates a service or series of services that pass a related set of messages or commands to one
   * another and to those of other protocols to modify application state of a particular domain
   */
  PROTOCOL,
  /**
   * Provides a service focusing on handling a specific type of command or subset of the commands
   * with the domain of the protocol of which it is a part Service providers may act as a router and
   * create subordinates--workers--routing the commands sent to them unaltered to these workers so
   * that they are not blocked and free to take whatever commands are delivered in the time it takes
   * to do that work
   */
  SERVICE_PROVIDER,
  /**
   * Performs the work routed to it by a service provider for a specific type of command or subset
   * of the commands its service provider handles without maintaining any knowledge--state-- of the
   * work it has performed already Workers should be interchangeable, all having the same state, so
   * that any item of work routed to them would have the same result no matter which worker in the
   * worker pool received the given command
   */
  WORKER,
  /**
   * Handles a particular chunk of the work on behalf of the actor who spawns it in order to prevent
   * that actor from being blocked from taking other commands Protocol handlers should have a
   * limited lifetime, limited either through the use of preset timeouts or dependence on a process
   * that has a limited execution time. Protocol handlers are ideally very limited in the chunk of
   * work they perform so that they can be made generic and used by actors of many protocols and
   * types e.g. {@link WaitingDelegateActor}. However, sometimes a protocol handler may perform a
   * lot of work on behalf of its parent actor if the work in question needs to be tracked from
   * start to finish very carefully, involves communication with (and thus waiting for) other
   * services or protocols, or is nuanced in a such a way that the service-provider-to-worker
   * relationship makes tracking the state of a command/request cumbersome
   */
  PROTOCOL_HANDLER;

  @Override
  public UnaryOperator<String> actorNamingBasedOnRole() {
    return id ->
        String.join("_", id, CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, name()));
  }
}
