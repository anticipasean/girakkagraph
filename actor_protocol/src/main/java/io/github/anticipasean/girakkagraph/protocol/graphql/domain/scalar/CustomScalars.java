package io.github.anticipasean.girakkagraph.protocol.graphql.domain.scalar;

import graphql.Scalars;
import graphql.language.ArrayValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface CustomScalars {

  GraphQLScalarType Blob =
      GraphQLScalarType.newScalar()
          .name("Blob")
          .description("byte[]: Byte Array: potentially a file or encrypted value")
          .coercing(new BlobCoercing())
          .build();

  class BlobCoercing implements Coercing<byte[], byte[]> {

    final Function<Object, String> coercingExceptionGenerator =
        o ->
            String.format(
                "Expected type 'byte[]' but was '%s':",
                (o == null ? "null" : o.getClass().getSimpleName()));
    final BiFunction<Object, Throwable, CoercingSerializeException>
        coercingSerializeExceptionGenerator =
            (obj, throwable) ->
                new CoercingSerializeException(coercingExceptionGenerator.apply(obj), throwable);
    final BiFunction<Object, Throwable, CoercingParseValueException>
        parseSerializeExceptionGenerator =
            (obj, throwable) ->
                new CoercingParseValueException(coercingExceptionGenerator.apply(obj), throwable);
    final BiFunction<Object, Throwable, CoercingParseLiteralException>
        coercingParseLiteralExceptionGenerator =
            (obj, throwable) ->
                new CoercingParseLiteralException(coercingExceptionGenerator.apply(obj), throwable);
    final Supplier<IllegalArgumentException> astConversionExceptionSupplier =
        () ->
            new IllegalArgumentException(
                String.format(
                    "not of GraphQL AST node type that may have a byte[] value extracted: [ %s ]",
                    Stream.of(StringValue.class, ArrayValue.class)
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", "))));
    /**
     * Called to convert a Java object result of a DataFetcher to a valid runtime value for the
     * scalar type.
     *
     * <p>Note : Throw {@link CoercingSerializeException} if there is fundamental problem during
     * serialisation, don't return null to indicate failure.
     *
     * <p>Note : You should not allow {@link RuntimeException}s to come out of your serialize
     * method, but rather catch them and fire them as {@link CoercingSerializeException} instead as
     * per the method contract.
     *
     * @param dataFetcherResult is never null
     * @return a serialized value which may be null.
     * @throws CoercingSerializeException if value input can't be serialized
     */
    @Override
    public byte[] serialize(Object dataFetcherResult) throws CoercingSerializeException {
      return castToBlob(dataFetcherResult, coercingSerializeExceptionGenerator);
    }

    /**
     * Called to resolve an input from a query variable into a Java object acceptable for the scalar
     * type.
     *
     * <p>Note : You should not allow {@link RuntimeException}s to come out of your parseValue
     * method, but rather catch them and fire them as {@link CoercingParseValueException} instead as
     * per the method contract.
     *
     * @param input is never null
     * @return a parsed value which is never null
     * @throws CoercingParseValueException if value input can't be parsed
     */
    @Override
    public byte[] parseValue(Object input) throws CoercingParseValueException {
      if (input instanceof String) {
        return ((String) input).getBytes();
      }
      return castToBlob(input, parseSerializeExceptionGenerator);
    }

    private <T extends Exception> byte[] castToBlob(
        Object input, BiFunction<Object, Throwable, T> exceptionGenerator) throws T {
      try {
        return (byte[]) input;
      } catch (Exception e) {
        throw exceptionGenerator.apply(input, e);
      }
    }

    /**
     * Called during query validation to convert a query input AST node into a Java object
     * acceptable for the scalar type. The input object will be an instance of {@link Value}.
     *
     * <p>Note : You should not allow {@link RuntimeException}s to come out of your parseLiteral
     * method, but rather catch them and fire them as {@link CoercingParseLiteralException} instead
     * as per the method contract.
     *
     * @param input is never null
     * @return a parsed value which is never null
     * @throws CoercingParseLiteralException if input literal can't be parsed
     */
    @Override
    public byte[] parseLiteral(Object input) throws CoercingParseLiteralException {
      if (!(input instanceof StringValue) && !(input instanceof ArrayValue)) {
        throw coercingParseLiteralExceptionGenerator.apply(
            input, astConversionExceptionSupplier.get());
      }
      if (input instanceof StringValue) {
        StringValue stringInputValue = (StringValue) input;
        return castToBlob(
            stringInputValue.getValue().getBytes(), coercingParseLiteralExceptionGenerator);
      } else {
        ArrayValue arrayInputValue = (ArrayValue) input;
        try {
          int arrayLen = arrayInputValue.getValues().size();
          byte[] byteArray = new byte[arrayLen];
          AtomicInteger index = new AtomicInteger(0);
          arrayInputValue.getValues().stream()
              .map(value -> (IntValue) value)
              .map(Scalars.GraphQLByte.getCoercing()::parseLiteral)
              .map(o -> (Byte) o)
              .forEach(aByte -> byteArray[index.getAndIncrement()] = aByte);
          return byteArray;
        } catch (Exception e) {
          throw coercingParseLiteralExceptionGenerator.apply(input, e);
        }
      }
    }
  }
}
