package io.github.anticipasean.girakkagraph.protocol.base.protocol;

import io.github.anticipasean.girakkagraph.protocol.base.actor.RegistrableCommandProtocolActor;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;

public abstract class RegistrableProtocolActor<T extends Command> extends RegistrableCommandProtocolActor<T> implements Protocol {

    protected RegistrableProtocolActor(SpawnedContext<Command, T> spawnedContext) {
        super(spawnedContext);
    }
}
