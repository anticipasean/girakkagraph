package io.github.anticipasean.girakkagraph.modifact.generation;

import cyclops.control.Option;
import cyclops.function.FluentFunctions;
import cyclops.function.Function0;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface DevelopmentStage<T> extends Supplier<Option<T>> {

  static <T> DevelopmentStage<T> stage(final DevelopmentStage<DevelopmentStage<T>> nextStage) {
    return new NextDevelopmentStageImpl<>(nextStage);
  }

  static <T> DevelopmentStage<T> stage(final Supplier<DevelopmentStage<T>> nextStage) {
      //Make next stage an Option to fit constructor parameter through a lift
    return new NextDevelopmentStageImpl<T>(FluentFunctions.of(nextStage).lift()::get);
  }

  static <T> DevelopmentStage<T> stop() {
    return Option::<T>none;
  }

  static <T> DevelopmentStage<T> builtResult(final T result) {
    return () -> Option.some(result);
  }

  default boolean complete() {
    return true;
  }

  default DevelopmentStage<T> progress() {
    return this;
  }

  static class NextDevelopmentStageImpl<T> implements DevelopmentStage<T> {

    private final DevelopmentStage<DevelopmentStage<T>> nextStage;

    public NextDevelopmentStageImpl(DevelopmentStage<DevelopmentStage<T>> nextStage) {
      this.nextStage = Objects.requireNonNull(nextStage, "nextStage");
    }

    @Override
    public boolean complete() {
      return false;
    }

    @Override
    public DevelopmentStage<T> progress() {
      return nextStage.get().orElse(Option::<T>none);
    }

    @Override
    public Option<T> get() {
      return nextStageSetup(this);
    }

    Option<T> nextStageSetup(final DevelopmentStage<T> currentStage) {
      return Stream.iterate(currentStage, DevelopmentStage::progress)
          .filter(DevelopmentStage::complete)
          .findFirst()
          .map(DevelopmentStage::get)
          .orElseThrow(
              () -> new NoSuchElementException("the desired object could not be generated"));
    }
  }
}
