package io.github.anticipasean.girakkagraph.modifact.generation.style;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@Value.Modifiable
public interface School extends GirrakagraphEntity {

  String name();

  List<Student> students();

  List<Teacher> teachers();
}
