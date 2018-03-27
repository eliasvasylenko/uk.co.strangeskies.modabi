package uk.co.strangeskies.modabi.schema.impl.bindingconditions;

import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;

import java.util.Comparator;

import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;

public class SortCondition<T> implements BindingCondition<T> {
  private final Expression expression;
  private final Comparator<? super T> comparator;

  public SortCondition(
      Expression expression,
      TypeToken<T> type,
      FunctionalExpressionCompiler compiler) {
    this.expression = expression;
    this.comparator = compiler
        .compile(
            expression,
            new TypeToken<Comparator<T>>() {}.withTypeArguments(new TypeArgument<T>(type) {}))
        .getInstance();
  }

  @Override
  public BindingConditionEvaluation<T> forState(BindingContext state) {
    return new BindingConditionEvaluation<T>() {
      private T previousBinding;

      @Override
      public void beginProcessingNext() {}

      @Override
      public void completeProcessingNext(T binding) {
        if (previousBinding != null && binding != null
            && comparator.compare(previousBinding, binding) > 0) {
          failProcess();
        }

        previousBinding = binding;
      }

      @Override
      public void endProcessing() {}

      @SuppressWarnings("unchecked")
      private BindingException failProcess() {
        return new BindingException(
            MESSAGES
                .mustBeOrdered(
                    (Child<T>) state.getBindingPoint(),
                    previousBinding,
                    (Class<? extends Comparator<?>>) comparator.getClass()),
            state);
      }
    };
  }

  @Override
  public BindingConditionPrototype getPrototype() {
    return v -> v.sorted(expression);
  }
}
