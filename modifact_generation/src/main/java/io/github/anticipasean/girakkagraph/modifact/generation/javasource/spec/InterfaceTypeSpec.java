package io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec;

import com.squareup.javapoet.TypeSpec;
import org.immutables.value.Value;

@Value.Immutable
public interface InterfaceTypeSpec extends JavaSourceSpec<TypeSpec> {

  TypeSpec typeSpec();

  @Override
  TypeSpec generateSpec();
}
