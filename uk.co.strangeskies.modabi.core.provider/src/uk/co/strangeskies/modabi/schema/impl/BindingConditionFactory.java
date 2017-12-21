package uk.co.strangeskies.modabi.schema.impl;

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.mathematics.Interval.bounded;
import static uk.co.strangeskies.mathematics.Interval.leftBounded;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.impl.schema.bindingconditions.AllOfCondition;
import uk.co.strangeskies.modabi.impl.schema.bindingconditions.AnyOfCondition;
import uk.co.strangeskies.modabi.impl.schema.bindingconditions.ForbiddenCondition;
import uk.co.strangeskies.modabi.impl.schema.bindingconditions.IsBoundCondition;
import uk.co.strangeskies.modabi.impl.schema.bindingconditions.IsNotBoundCondition;
import uk.co.strangeskies.modabi.impl.schema.bindingconditions.OccurrencesCondition;
import uk.co.strangeskies.modabi.impl.schema.bindingconditions.SortCondition;
import uk.co.strangeskies.modabi.impl.schema.bindingconditions.SynchronizedCondition;
import uk.co.strangeskies.modabi.impl.schema.bindingconditions.ValidationCondition;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.BindingConditionVisitor;
import uk.co.strangeskies.reflection.token.TypeToken;

public class BindingConditionFactory<T> {
  private final TypeToken<T> type;
  private final FunctionalExpressionCompiler compiler;

  public BindingConditionFactory(TypeToken<T> type, FunctionalExpressionCompiler compiler) {
    this.type = type;
    this.compiler = compiler;
  }

  public BindingCondition<T> create(BindingConditionPrototype builder) {
    BindingConditionVisitorImpl visitor = new BindingConditionVisitorImpl();
    return visitor.visit(builder);
  }

  class BindingConditionVisitorImpl implements BindingConditionVisitor {
    Deque<BindingCondition<T>> conditionStack = new ArrayDeque<>();

    protected BindingCondition<T> visit(BindingConditionPrototype prototype) {
      prototype.accept(this);
      return conditionStack.pop();
    }

    protected List<BindingCondition<T>> visitAll(
        Collection<? extends BindingConditionPrototype> prototypes) {
      return prototypes.stream().map(this::visit).collect(toList());
    }

    @Override
    public void allOf(Collection<? extends BindingConditionPrototype> conditions) {
      conditionStack.push(new AllOfCondition<>(visitAll(conditions)));
    }

    @Override
    public void anyOf(Collection<? extends BindingConditionPrototype> conditions) {
      conditionStack.push(new AnyOfCondition<>(visitAll(conditions)));
    }

    @Override
    public void forbidden() {
      conditionStack.push(new ForbiddenCondition<>());
    }

    @Override
    public void optional() {
      conditionStack.push(new OccurrencesCondition<>(bounded(0, 1)));
    }

    @Override
    public void required() {
      conditionStack.push(new OccurrencesCondition<>(leftBounded(1)));
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
      conditionStack.push(new OccurrencesCondition<>(range));
    }

    @Override
    public void isBound() {
      conditionStack.push(new IsBoundCondition<>(null));
    }

    @Override
    public void isNotBound() {
      conditionStack.push(new IsNotBoundCondition<>(null));
    }

    @Override
    public void validated(Expression predicate) {
      conditionStack.push(new ValidationCondition<>(predicate, type, compiler));
    }

    @Override
    public void synchronous() {
      conditionStack.push(new SynchronizedCondition<>());
    }
  }
}
