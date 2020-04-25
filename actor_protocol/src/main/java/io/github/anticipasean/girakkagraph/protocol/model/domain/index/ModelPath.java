package io.github.anticipasean.girakkagraph.protocol.model.domain.index;

import akka.japi.Pair;
import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(
    overshadowImplementation = true,
    typeImmutable = "*Impl",
    depluralize = true,
    depluralizeDictionary = {
      "segment:segments",
      "argument:arguments",
      "directive:directives",
      "rawArgument:rawArguments"
    })
public interface ModelPath extends Comparable<ModelPath> {
  String PARAMETER = "__param__";
  String SCHEME = "model";

  static String normalizeSegment(String rawSegment) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, rawSegment);
  }

  @Value.Derived
  default URI uri() {
    String queryAsString = String.join("&", rawArguments());
    String fragmentsAsString = String.join("&", directives());
    try {
      return new URI(
          scheme(),
          null,
          path(),
          queryAsString.length() > 0 ? queryAsString : null,
          fragmentsAsString.length() > 0 ? fragmentsAsString : null);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(
          String.format(
              "the model uri cannot be constructed from the given path [ %s ] and query string [ %s ]",
              path(), queryAsString),
          e);
    }
  }

  default String path() {
    return new StringBuilder("/").append(String.join("/", segments())).toString();
  }

  default String scheme() {
    return SCHEME;
  }

  @Value.Parameter
  List<String> segments();

  @Value.Check
  default void checkSegments() {
    Supplier<String> invalidSegmentMessageSupplier =
        () ->
            "path segments may not contain the path separator '/': [ "
                + String.join(", ", segments())
                + " ]";
    Preconditions.checkState(
        segments().stream().noneMatch(s -> s.indexOf('/') >= 0),
        invalidSegmentMessageSupplier.get());
    Preconditions.checkState(
        segments().stream().noneMatch(String::isEmpty), "no path segment may be empty");
  }

  default ModelPath parentPath() {
    if (rawArguments().size() > 0 || directives().size() > 0) {
      return ModelPathImpl.of(segments());
    }
    if (segments().size() > 1) {
      return ModelPathImpl.of(segments().subList(0, segments().size() - 1));
    } else {
      return ModelPathImpl.builder().build();
    }
  }

  List<String> rawArguments();

  @Value.Lazy
  default Map<String, String> arguments() {
    return rawArguments().stream()
        .map(s -> s.split("=", 1))
        .map(strings -> Pair.create(strings[0], strings.length == 2 ? strings[1] : ""))
        .collect(Collectors.toMap(Pair::first, Pair::second));
  }

  default ModelPath withParameterizedArgument(String argumentKey) {
    ImmutableList<String> updatedArgumentsList =
        ImmutableList.<String>builder()
            .addAll(rawArguments())
            .add(String.join("=", argumentKey, PARAMETER))
            .build();
    return ((ModelPathImpl) this).withRawArguments(updatedArgumentsList);
  }

  default ModelPath withListArgument(String argumentKey, String... values) {
    String listContent =
        new StringBuilder("[").append(String.join(",", values)).append("]").toString();
    ImmutableList<String> updatedArgumentsList =
        ImmutableList.<String>builder()
            .addAll(rawArguments())
            .add(String.join("=", argumentKey, listContent))
            .build();
    return ((ModelPathImpl) this).withRawArguments(updatedArgumentsList);
  }

  default ModelPath withListArgument(String argumentKey, Iterable<String> values) {
    String listContent =
        new StringBuilder(("[")).append(String.join(",", values)).append("]").toString();
    ImmutableList<String> updatedArgumentsList =
        ImmutableList.<String>builder()
            .addAll(rawArguments())
            .add(String.join("=", argumentKey, listContent))
            .build();
    return ((ModelPathImpl) this).withRawArguments(updatedArgumentsList);
  }

  List<String> directives();

  default int depth() {
    return segments().size();
  }

  default String segmentByIndex(int index) {
    return index >= 0 && index < segments().size() ? segments().get(index) : null;
  }

  default ModelPath subPath(int endIndex) {
    if (endIndex >= 0 && endIndex < segments().size()) {
      return ModelPathImpl.of(segments().subList(0, endIndex));
    }
    throw new IndexOutOfBoundsException(
        "endIndex [ "
            + endIndex
            + " ] does not fall within bounds: endIndex >= 0 && endIndex < segments().size() [ "
            + segments().size()
            + " ]");
  }

  //  default String typeName() {
  //    return Optional.ofNullable(segmentByIndex(0))
  //        .orElseThrow(
  //            () ->
  //                new IllegalArgumentException(
  //                    "no root field was provided in the path: " + uri().getPath()));
  //  }
  //
  //  default String attributeName() {
  //    return Optional.ofNullable(segmentByIndex(1))
  //        .orElseThrow(
  //            () ->
  //                new IllegalArgumentException(
  //                    String.format(
  //                        "path has a depth of %d and thus only corresponds to a type",
  // depth())));
  //  }

  default boolean isParent(ModelPath potentialParent) {
    return Objects.requireNonNull(potentialParent, "potential parent path may not be null").depth()
            > 0
        && depth() > 0
        && !this.equals(potentialParent)
        && potentialParent.equals(parentPath());
  }

  default boolean isChild(ModelPath potentialChild) {
    return Objects.requireNonNull(potentialChild, "potential child path may not be null").depth()
            > 0
        && !this.equals(potentialChild)
        && potentialChild.parentPath().equals(this);
  }

  default boolean isSibling(ModelPath potentialSibling) {
    return Objects.requireNonNull(potentialSibling, "potential sibling path may not be null")
                .depth()
            > 0
        && !this.equals(potentialSibling)
        && potentialSibling.parentPath().equals(parentPath());
  }

  default boolean isCousin(ModelPath potentialCousin) {
    return Objects.requireNonNull(potentialCousin, "potential cousin path may not be null").depth()
            > 1
        && depth() > 1
        && !this.equals(potentialCousin)
        && parentPath().isSibling(potentialCousin.parentPath());
  }

  default String rootSegment() {
    return depth() > 0 ? segments().get(0) : null;
  }

  default String lastSegment() {
    return depth() > 0 ? segments().get(depth() - 1) : null;
  }

  @Override
  default int compareTo(ModelPath o) {
    if (o == null) {
      return 1;
    }
    if (this.equals(o)) {
      return 0;
    }
    if (!scheme().equals(o.scheme())) {
      return uri().compareTo(o.uri());
    }
    if (depth() > 0 && o.depth() > 0 && !rootSegment().equals(o.rootSegment())) {
      return uri().compareTo(uri());
    }
    if (depth() > 0
        && o.depth() > 0
        && rootSegment().equals(o.rootSegment())
        && depth() != o.depth()) {
      return depth() - o.depth();
    }
    return uri().compareTo(o.uri());
  }

  //  default ModelPath fromURI(URI modelPathUri) {
  //    Objects.requireNonNull(modelPathUri, () -> "uri parameter to this method may not be null");
  //    if (!modelPathUri.getScheme().equals(scheme())) {
  //      throw new IllegalArgumentException(
  //          "scheme of uri [ "
  //              + modelPathUri.getScheme()
  //              + " ] does not match scheme [ "
  //              + scheme()
  //              + " ]");
  //    }
  //    List<String> segments =
  //
  // Arrays.stream(uri().getPath().split("/")).map(String::trim).collect(Collectors.toList());
  //    Map<String, String> argumentEntries =
  //        Arrays.stream(uri().getRawQuery().split("&"))
  //            .map(s -> s.startsWith("?") ? s.substring(1) : s)
  //            .map(
  //                s -> {
  //                  String[] entry = s.split("=", 1);
  //                  return Pair.create(entry[0], entry.length == 2 ? entry[1] : "");
  //                })
  //            .collect(Collectors.toMap(Pair::first, Pair::second));
  //    ModelPathImpl.Builder builder = ModelPathImpl.builder().addAllSegments(segments);
  //    for (Map.Entry<String, String> argNameArgClassNameEntry : argumentEntries.entrySet()) {
  //      try {
  //        Class<?> argClass = Class.forName(argNameArgClassNameEntry.getValue());
  //        builder.putArgumentsOfType(argNameArgClassNameEntry.getKey(), argClass);
  //      } catch (ClassNotFoundException e) {
  //        throw new IllegalArgumentException(
  //            "unable to map argument class name [ "
  //                + argNameArgClassNameEntry.getValue()
  //                + " ] to class in environment for argument name [ "
  //                + argNameArgClassNameEntry.getKey()
  //                + " ]",
  //            e);
  //      }
  //    }
  //    List<String> directives =
  //        Arrays.stream(modelPathUri.getRawFragment().split("&")).collect(Collectors.toList());
  //    builder.directives(directives);
  //    return builder.build();
  //  }
}
