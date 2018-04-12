package uk.co.strangeskies.modabi.schema;

import java.util.Collection;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;

@FunctionalInterface
public interface BindingConstraintSpecification {
  void accept(BindingConstraintVisitor visitor);

  default BindingConstraintSpecification and(BindingConstraintSpecification condition) {
    return v -> v.allOf(this, condition);
  }

  default BindingConstraintSpecification or(BindingConstraintSpecification condition) {
    return v -> v.anyOf(this, condition);
  }

  static BindingConstraintSpecification allOf(BindingConstraintSpecification... conditions) {
    return v -> v.allOf(conditions);
  }

  static BindingConstraintSpecification anyOf(BindingConstraintSpecification... conditions) {
    return v -> v.anyOf(conditions);
  }

  static BindingConstraintSpecification allOf(
      Collection<? extends BindingConstraintSpecification> conditions) {
    return v -> v.allOf(conditions);
  }

  static BindingConstraintSpecification anyOf(
      Collection<? extends BindingConstraintSpecification> conditions) {
    return v -> v.anyOf(conditions);
  }

  static BindingConstraintSpecification forbidden() {
    return v -> v.forbidden();
  }

  static BindingConstraintSpecification optional() {
    return v -> v.optional();
  }

  static BindingConstraintSpecification required() {
    return v -> v.required();
  }

  static BindingConstraintSpecification ascending() {
    return v -> v.ascending();
  }

  static BindingConstraintSpecification descending() {
    return v -> v.descending();
  }

  static BindingConstraintSpecification sorted(Expression comparator) {
    return v -> v.sorted(comparator);
  }

  static BindingConstraintSpecification occurrences(Interval<Integer> range) {
    return v -> v.occurrences(range);
  }

  static BindingConstraintSpecification isBound(QualifiedName name) {
    return v -> v.isBound(name);
  }

  static BindingConstraintSpecification isNotBound(QualifiedName name) {
    return v -> v.isNotBound(name);
  }

  static BindingConstraintSpecification validated(Expression predicate) {
    return v -> v.validated(predicate);
  }

  static BindingConstraintSpecification synchronous() {
    return v -> v.synchronous();
  }
}
