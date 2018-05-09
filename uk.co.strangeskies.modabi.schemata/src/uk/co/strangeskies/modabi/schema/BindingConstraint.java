package uk.co.strangeskies.modabi.schema;

import java.util.Collection;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;

@FunctionalInterface
public interface BindingConstraint {
  void accept(BindingConstraintVisitor visitor);

  default BindingConstraint and(BindingConstraint condition) {
    return v -> v.allOf(this, condition);
  }

  default BindingConstraint or(BindingConstraint condition) {
    return v -> v.anyOf(this, condition);
  }

  static BindingConstraint allOf(BindingConstraint... conditions) {
    return v -> v.allOf(conditions);
  }

  static BindingConstraint anyOf(BindingConstraint... conditions) {
    return v -> v.anyOf(conditions);
  }

  static BindingConstraint allOf(
      Collection<? extends BindingConstraint> conditions) {
    return v -> v.allOf(conditions);
  }

  static BindingConstraint anyOf(
      Collection<? extends BindingConstraint> conditions) {
    return v -> v.anyOf(conditions);
  }

  static BindingConstraint forbidden() {
    return v -> v.forbidden();
  }

  static BindingConstraint optional() {
    return v -> v.optional();
  }

  static BindingConstraint required() {
    return v -> v.required();
  }

  static BindingConstraint ascending() {
    return v -> v.ascending();
  }

  static BindingConstraint descending() {
    return v -> v.descending();
  }

  static BindingConstraint sorted(Expression comparator) {
    return v -> v.sorted(comparator);
  }

  static BindingConstraint occurrences(Interval<Integer> range) {
    return v -> v.occurrences(range);
  }

  static BindingConstraint isBound(QualifiedName name) {
    return v -> v.isBound(name);
  }

  static BindingConstraint isNotBound(QualifiedName name) {
    return v -> v.isNotBound(name);
  }

  static BindingConstraint validated(Expression predicate) {
    return v -> v.validated(predicate);
  }

  static BindingConstraint synchronous() {
    return v -> v.synchronous();
  }
}
