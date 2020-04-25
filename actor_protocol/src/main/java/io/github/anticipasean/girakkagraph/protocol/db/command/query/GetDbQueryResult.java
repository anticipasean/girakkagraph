package io.github.anticipasean.girakkagraph.protocol.db.command.query;

import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.db.command.DatabaseProtocol;
import org.immutables.value.Value;

@com.fasterxml.jackson.databind.annotation.JsonSerialize(as = GetDbQueryResultImpl.class)
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(as = GetDbQueryResultImpl.class)
@Value.Immutable
public interface GetDbQueryResult extends DatabaseProtocol<Command> {}
