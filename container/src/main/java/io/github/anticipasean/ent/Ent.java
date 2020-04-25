package io.github.anticipasean.ent;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.pattern.IfMatchClause;
import io.github.anticipasean.ent.pattern.Pattern;
import io.github.anticipasean.ent.state.EmptyAttrEnt;
import io.github.anticipasean.ent.state.SingleAttrEnt;
import io.github.anticipasean.ent.value.Attribute;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;


public interface Ent<ID, C> extends Iterable<Tuple2<ID, C>> {

    /*
         Factory methods
     */

    static <ID, C> Ent<ID, C> of(C valueContainer,
                                 Function<C, ID> idExtractor) {
        Objects.requireNonNull(valueContainer,
                               "valueContainer");
        Objects.requireNonNull(idExtractor,
                               "idExtractor");
        return new SingleAttrEnt<>(idExtractor.apply(valueContainer),
                                   valueContainer);
    }

    static <ID, V> Ent<ID, V> of(ID id,
                                 V value) {
        Objects.requireNonNull(id,
                               "id");
        Objects.requireNonNull(value,
                               "value");
        return new SingleAttrEnt<>(id,
                                   value);
    }


    static <ID, C> Ent<ID, C> empty() {
        return EmptyAttrEnt.emptyEnt();
    }

    /*
        Iterable methods
     */

    int size();

    /*
        Monadic methods
     */

    <R> Ent<ID, R> map(Function<? super C, ? extends R> fn);


    /*
        Map Methods
     */
//    Option<C> getOrElse(ID id, C alternative);
//    Option<C> getOrElseGet(ID id, Supplier<C> alternative);

    /*
        Ent Specific Methods
     */

    <R> Option<R> getAndMatch(ID id, Function<IfMatchClause<C>, R> patternMap);



    @SuppressWarnings("unchecked")
    default <T, U> Attribute<T> deriveAttributeFrom(Class<U> typeToken,
                                                    URI uri,
                                                    T value) {
        return Pattern.forObject(value)
                      .ifOfType(String.class)
                      .then(v -> (Attribute) Attribute.withContainer(v,
                                                                     String.class::cast))
                      .ifOfType(Boolean.class)
                      .then(v -> Attribute.withContainer(v,
                                                         Boolean.class::cast))
                      .ifOfType(Byte.class)
                      .then(v -> Attribute.withContainer(v,
                                                         Byte.class::cast))
                      .ifOfType(Character.class)
                      .then(v -> Attribute.withContainer(v,
                                                         Character.class::cast))
                      .ifOfType(Float.class)
                      .then(v -> Attribute.withContainer(v,
                                                         Float.class::cast))
                      .ifOfType(Integer.class)
                      .then(v -> Attribute.withContainer(v,
                                                         Integer.class::cast))
                      .ifOfType(Long.class)
                      .then(v -> Attribute.withContainer(v,
                                                         Long.class::cast))
                      .ifOfType(Short.class)
                      .then(v -> Attribute.withContainer(v,
                                                         Short.class::cast))
                      .ifOfType(Double.class)
                      .then(v -> Attribute.withContainer(v,
                                                         Double.class::cast))
                      .ifOfType(BigInteger.class)
                      .then(v -> Attribute.withContainer(v,
                                                         BigInteger.class::cast))
                      .ifOfType(BigDecimal.class)
                      .then(v -> Attribute.withContainer(v,
                                                         BigDecimal.class::cast))
                      .ifOfType(LocalDate.class)
                      .then(v -> Attribute.withContainer(v,
                                                         LocalDate.class::cast))
                      .ifOfType(LocalTime.class)
                      .then(v -> Attribute.withContainer(v,
                                                         LocalTime.class::cast))
                      .ifOfType(LocalDateTime.class)
                      .then(v -> Attribute.withContainer(v,
                                                         LocalDateTime.class::cast))
                      .ifOfType(ZonedDateTime.class)
                      .then(v -> Attribute.withContainer(v,
                                                         ZonedDateTime.class::cast))
                      .ifOfType(OffsetDateTime.class)
                      .then(v -> Attribute.withContainer(v,
                                                         OffsetDateTime.class::cast))
                      .ifOfType(Iterable.class)
                      .then(v -> Attribute.withContainer(Optional.ofNullable(v),
                                                         Iterable.class::cast))
                      .orElse(Attribute.withContainer(value,
                                                      typeToken::cast));
    }


}
