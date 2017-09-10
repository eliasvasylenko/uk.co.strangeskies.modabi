package uk.co.strangeskies.modabi.schema;

import java.util.Arrays;
import java.util.Collection;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface BindingConditionVisitor {
  default void allOf(BindingConditionPrototype... conditions) {
    allOf(Arrays.asList(conditions));
  }

  default void anyOf(BindingConditionPrototype... conditions) {
    anyOf(Arrays.asList(conditions));
  }

  void allOf(Collection<? extends BindingConditionPrototype> conditions);

  void anyOf(Collection<? extends BindingConditionPrototype> conditions);

  void forbidden();

  void optional();

  void required();

  void ascending();

  void descending();

  void sorted(ValueExpression comparator);

  void occurrences(Interval<Integer> range);

  void isBound(/* TODO */);

  void isNotBound(/* TODO */);

  void validated(ValueExpression predicate);

  void synchronous();
}
