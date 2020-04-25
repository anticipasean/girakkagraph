package io.github.anticipasean.girakkagraph.protocol.base.util.shareable;

import java.util.List;
import java.util.function.Supplier;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(
    depluralize = true,
    depluralizeDictionary = {"shareable:shareables"},
    typeImmutable = "*Impl",
    typeAbstract = "*")
public interface Shareables {
  List<Supplier<Shareable>> shareableSuppliers();
}
