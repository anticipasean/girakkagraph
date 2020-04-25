package io.github.anticipasean.girakkagraph.protocol.db.command.query;

import io.github.anticipasean.girakkagraph.protocol.db.command.DatabaseProtocol;
import org.immutables.value.Value;

@com.fasterxml.jackson.databind.annotation.JsonSerialize(as = QueryDbImpl.class)
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(as = QueryDbImpl.class)
@Value.Immutable
public interface QueryDb extends DatabaseProtocol<GetDbQueryResult> {}
