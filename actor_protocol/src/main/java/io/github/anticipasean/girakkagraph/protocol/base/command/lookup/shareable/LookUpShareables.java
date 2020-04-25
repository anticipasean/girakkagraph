package io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable;

import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.LookUpCommand;
import io.github.anticipasean.girakkagraph.protocol.base.util.lookup.ShareableQuery;
import java.util.List;
import org.immutables.value.Value;

@Value.Style(
    typeImmutable = "*Impl",
    depluralize = true,
    depluralizeDictionary = {"shareableQuery:shareableQueries"})
@Value.Immutable
public interface LookUpShareables extends LookUpCommand<LookUpShareableResultsFound> {

  List<ShareableQuery> lookUpShareableQueries();
}
