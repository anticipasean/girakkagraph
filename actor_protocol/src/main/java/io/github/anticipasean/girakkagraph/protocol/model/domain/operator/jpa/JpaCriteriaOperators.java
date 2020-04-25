package io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa;

import com.google.common.collect.ImmutableMap;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.ModelOperatorImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.OperatorDatabase;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface JpaCriteriaOperators {

  static Map<String, JpaCriteriaOperator<?>> newCallNameToOperatorMapInstance() {
    Iterator<? extends JpaCriteriaOperator<?>> iterator =
        Stream.of(
                JpaOrderingOperator.UnaryFunctional.values(),
                JpaComparativeOperator.Binary.values(),
                JpaComparativeOperator.Ternary.values(),
                JpaAggregationOperator.UnaryFunctional.values())
            .flatMap(Arrays::stream)
            .iterator();
    ImmutableMap.Builder<String, JpaCriteriaOperator<?>> mapBuilder = ImmutableMap.builder();
    Set<String> callNames = new HashSet<>();
    while (iterator.hasNext()) {
      JpaCriteriaOperator<?> operator = iterator.next();
      String callName = operator.callName();
      if (callNames.contains(callName)) {
        Supplier<String> messageSupplier =
            () ->
                String.format(
                    "the following operator call name [ %s ] has already "
                        + "been mapped to a jpa criteria operator, meaning "
                        + "there is more than one operator with the same "
                        + "call name. Operator call names must be unique.",
                    callName);
        throw new IllegalStateException(messageSupplier.get());
      }
      mapBuilder.put(callName, operator);
      callNames.add(callName);
    }
    return mapBuilder.build();
  }

  static OperatorDatabase populateOperatorDatabaseWithJpaCriteriaOperators(
      OperatorDatabase operatorDatabase) {
    Logger logger = LoggerFactory.getLogger(JpaCriteriaOperators.class);
    logger.info("populating_operator_db_with_jpa_criteria_operators");
    try {
      newCallNameToOperatorMapInstance().values().stream()
          .map(
              jpaCritOper ->
                  ModelOperatorImpl.builder().operatorSupplier(() -> jpaCritOper).build())
          .forEach(
              modelOperator -> operatorDatabase.getModelOperatorRepository().insert(modelOperator));
    } catch (IllegalStateException ise) {
      logger.error(
          "unable to complete inserting jpa criteria operators into "
              + "operator db due to a likely missing parameter on one of the operators:",
          ise);
      throw ise;
    }
    return operatorDatabase;
  }
}
