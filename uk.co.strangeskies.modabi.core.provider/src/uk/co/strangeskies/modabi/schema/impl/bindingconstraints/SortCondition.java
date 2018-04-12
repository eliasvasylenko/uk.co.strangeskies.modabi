package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;

import java.util.Comparator;

import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingProcedure;
import uk.co.strangeskies.modabi.schema.BindingConstraintSpecification;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;

public class SortCondition<T> implements BindingConstraint<T> {
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
  public BindingProcedure<T> procedeWithState(BindingContext state) {
    return new BindingProcedure<T>() {
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
  public BindingConstraintSpecification getSpecification() {
    return v -> v.sorted(expression);
  }
}
