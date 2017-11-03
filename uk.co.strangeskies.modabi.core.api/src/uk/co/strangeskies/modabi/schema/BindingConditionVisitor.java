package uk.co.strangeskies.modabi.schema;

import java.util.Arrays;
import java.util.Collection;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.expression.Expression;

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

  void sorted(Expression comparator);

  void occurrences(Interval<Integer> range);

  void isBound(/* TODO */);

  void isNotBound(/* TODO */);

  void validated(Expression predicate);

  void synchronous();
}
