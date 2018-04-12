package uk.co.strangeskies.modabi.schema;

import java.util.Arrays;
import java.util.Collection;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;

public interface BindingConstraintVisitor {
  default void allOf(BindingConstraintSpecification... conditions) {
    allOf(Arrays.asList(conditions));
  }

  default void anyOf(BindingConstraintSpecification... conditions) {
    anyOf(Arrays.asList(conditions));
  }

  void allOf(Collection<? extends BindingConstraintSpecification> conditions);

  void anyOf(Collection<? extends BindingConstraintSpecification> conditions);

  void forbidden();

  void optional();

  void required();

  void ascending();

  void descending();

  void sorted(Expression comparator);

  void occurrences(Interval<Integer> range);

  void isBound(QualifiedName name);

  void isNotBound(QualifiedName name);

  void validated(Expression predicate);

  void synchronous();
}
