package uk.co.strangeskies.modabi.schema.bindingconditions;

import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;

import java.util.Comparator;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class SortCondition<T> implements BindingCondition<T> {
  private final Comparator<? super T> comparator;

  public static <T> BindingCondition<T> sorted(Comparator<T> order) {
    return new SortCondition<>(order);
  }

  protected SortCondition(Comparator<? super T> comparator) {
    this.comparator = comparator;
  }

  @Override
  public BindingConditionEvaluation<T> forState(ProcessingContext state) {
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
      private ProcessingException failProcess() {
        return new ProcessingException(
            MESSAGES.mustBeOrdered(
                (ChildBindingPoint<T>) state.getNode(),
                previousBinding,
                (Class<? extends Comparator<?>>) comparator.getClass()),
            state);
      }
    };
  }
}
