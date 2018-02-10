package uk.co.strangeskies.modabi.schema.impl.bindingconditions;

import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;

import java.util.function.Predicate;

import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class ValidationCondition<T> implements BindingCondition<T> {
  private final Expression expression;
  private final Predicate<T> test;

  public ValidationCondition(
      Expression expression,
      TypeToken<T> type,
      FunctionalExpressionCompiler compiler) {
    this.expression = expression;
    this.test = compiler
        .compile(
            expression,
            new TypeToken<Predicate<T>>() {}.withTypeArguments(new TypeArgument<T>(type) {}))
        .getInstance();
  }

  @Override
  public BindingConditionEvaluation<T> forState(BindingContext state) {
    return new BindingConditionEvaluation<T>() {
      public void failProcess() {
        throw new BindingException(
            MESSAGES.validationFailed((ChildBindingPoint<?>) state.getBindingPoint(), expression),
            state);
      }

      @Override
      public void beginProcessingNext() {}

      @Override
      public void completeProcessingNext(T binding) {
        if (!test.test(binding)) {
          failProcess();
        }
      }

      @Override
      public void endProcessing() {}
    };
  }

  @Override
  public BindingConditionPrototype getPrototype() {
    return v -> v.validated(expression);
  }
}
