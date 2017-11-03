package uk.co.strangeskies.modabi.schema;

import java.util.Collection;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.expression.Expression;

@FunctionalInterface
public interface BindingConditionPrototype {
  void accept(BindingConditionVisitor visitor);

  default BindingConditionPrototype and(BindingConditionPrototype condition) {
    return v -> v.allOf(this, condition);
  }

  default BindingConditionPrototype or(BindingConditionPrototype condition) {
    return v -> v.anyOf(this, condition);
  }

  static BindingConditionPrototype allOf(BindingConditionPrototype... conditions) {
    return v -> v.allOf(conditions);
  }

  static BindingConditionPrototype anyOf(BindingConditionPrototype... conditions) {
    return v -> v.anyOf(conditions);
  }

  static BindingConditionPrototype allOf(
      Collection<? extends BindingConditionPrototype> conditions) {
    return v -> v.allOf(conditions);
  }

  static BindingConditionPrototype anyOf(
      Collection<? extends BindingConditionPrototype> conditions) {
    return v -> v.anyOf(conditions);
  }

  static BindingConditionPrototype forbidden() {
    return v -> v.forbidden();
  }

  static BindingConditionPrototype optional() {
    return v -> v.optional();
  }

  static BindingConditionPrototype required() {
    return v -> v.required();
  }

  static BindingConditionPrototype ascending() {
    return v -> v.ascending();
  }

  static BindingConditionPrototype descending() {
    return v -> v.descending();
  }

  static BindingConditionPrototype sorted(Expression comparator) {
    return v -> v.sorted(comparator);
  }

  static BindingConditionPrototype occurrences(Interval<Integer> range) {
    return v -> v.occurrences(range);
  }

  static BindingConditionPrototype isBound(/* TODO */) {
    return v -> v.isBound();
  }

  static BindingConditionPrototype isNotBound(/* TODO */) {
    return v -> v.isNotBound();
  }

  static BindingConditionPrototype validated(Expression predicate) {
    return v -> v.validated(predicate);
  }

  static BindingConditionPrototype synchronous() {
    return v -> v.synchronous();
  }
}
