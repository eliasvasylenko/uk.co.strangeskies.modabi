package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;

import java.util.function.Predicate;

import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.functional.FunctionCompiler;
import uk.co.strangeskies.modabi.schema.BindingProcedure;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingProcess;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class ValidationConstraint<T> implements BindingProcedure<T> {
  private final Expression expression;
  private final Predicate<T> test;

  public ValidationConstraint(
      Expression expression,
      TypeToken<T> type,
      FunctionCompiler compiler) {
    this.expression = expression;
    this.test = compiler
        .compile(
            expression,
            new TypeToken<Predicate<T>>() {}.withTypeArguments(new TypeArgument<T>(type) {}))
        .getInstance();
  }

  @Override
  public BindingProcess<T> procedeWithState(BindingContext state) {
    return new BindingProcess<T>() {
      public void failProcess() {
        throw new BindingException(
            MESSAGES.validationFailed((Child<?>) state.getBindingPoint(), expression),
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
  public BindingConstraint getConstraint() {
    return v -> v.validated(expression);
  }
}
