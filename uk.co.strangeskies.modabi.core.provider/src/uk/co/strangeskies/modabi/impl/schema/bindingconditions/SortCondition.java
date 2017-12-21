package uk.co.strangeskies.modabi.impl.schema.bindingconditions;

import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;

import java.util.Comparator;

import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionVisitor;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
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
            new TypeToken<Comparator<T>>() {}.withTypeArguments(new TypeArgument<T>(type) {}));
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
                    (ChildBindingPoint<T>) state.getBindingPoint(),
                    previousBinding,
                    (Class<? extends Comparator<?>>) comparator.getClass()),
            state);
      }
    };
  }

  @Override
  public void accept(BindingConditionVisitor visitor) {
    visitor.sorted(expression);
  }
}
