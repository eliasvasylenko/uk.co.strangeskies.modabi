package uk.co.strangeskies.modabi.schema.impl;

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.mathematics.Interval.bounded;
import static uk.co.strangeskies.mathematics.Interval.leftBounded;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.functional.FunctionCompiler;
import uk.co.strangeskies.modabi.schema.BindingConstraintVisitor;
import uk.co.strangeskies.modabi.schema.impl.bindingconstraints.AllOfConstraint;
import uk.co.strangeskies.modabi.schema.impl.bindingconstraints.AnyOfConstraint;
import uk.co.strangeskies.modabi.schema.impl.bindingconstraints.ForbiddenConstraint;
import uk.co.strangeskies.modabi.schema.impl.bindingconstraints.IsBoundConstraint;
import uk.co.strangeskies.modabi.schema.impl.bindingconstraints.IsNotBoundConstraint;
import uk.co.strangeskies.modabi.schema.impl.bindingconstraints.OccurrencesConstraint;
import uk.co.strangeskies.modabi.schema.impl.bindingconstraints.SortCondition;
import uk.co.strangeskies.modabi.schema.impl.bindingconstraints.SynchronizedConstraint;
import uk.co.strangeskies.modabi.schema.impl.bindingconstraints.ValidationConstraint;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingConstraintSpecification;
import uk.co.strangeskies.reflection.token.TypeToken;

public class BindingConditionFactory<T> {
  private final TypeToken<T> type;
  private final FunctionCompiler compiler;

  public BindingConditionFactory(TypeToken<T> type, FunctionCompiler compiler) {
    this.type = type;
    this.compiler = compiler;
  }

  public BindingConstraint<T> create(BindingConstraintSpecification builder) {
    BindingConditionVisitorImpl visitor = new BindingConditionVisitorImpl();
    return visitor.visit(builder);
  }

  class BindingConditionVisitorImpl implements BindingConstraintVisitor {
    Deque<BindingConstraint<T>> conditionStack = new ArrayDeque<>();

    protected BindingConstraint<T> visit(BindingConstraintSpecification prototype) {
      prototype.accept(this);
      return conditionStack.pop();
    }

    protected List<BindingConstraint<T>> visitAll(
        Collection<? extends BindingConstraintSpecification> prototypes) {
      return prototypes.stream().map(this::visit).collect(toList());
    }

    @Override
    public void allOf(Collection<? extends BindingConstraintSpecification> conditions) {
      conditionStack.push(new AllOfConstraint<>(visitAll(conditions)));
    }

    @Override
    public void anyOf(Collection<? extends BindingConstraintSpecification> conditions) {
      conditionStack.push(new AnyOfConstraint<>(visitAll(conditions)));
    }

    @Override
    public void forbidden() {
      conditionStack.push(new ForbiddenConstraint<>());
    }

    @Override
    public void optional() {
      conditionStack.push(new OccurrencesConstraint<>(bounded(0, 1)));
    }

    @Override
    public void required() {
      conditionStack.push(new OccurrencesConstraint<>(leftBounded(1)));
    }

    @Override
    public void ascending() {
      // TODO Auto-generated method stub

    }

    @Override
    public void descending() {
      // TODO Auto-generated method stub

    }

    @Override
    public void sorted(Expression comparator) {
      conditionStack.push(new SortCondition<>(comparator, type, compiler));
    }

    @Override
    public void occurrences(Interval<Integer> range) {
      conditionStack.push(new OccurrencesConstraint<>(range));
    }

    @Override
    public void isBound(QualifiedName name) {
      conditionStack.push(new IsBoundConstraint<>(null));
    }

    @Override
    public void isNotBound(QualifiedName name) {
      conditionStack.push(new IsNotBoundConstraint<>(null));
    }

    @Override
    public void validated(Expression predicate) {
      conditionStack.push(new ValidationConstraint<>(predicate, type, compiler));
    }

    @Override
    public void synchronous() {
      conditionStack.push(new SynchronizedConstraint<>());
    }
  }
}
