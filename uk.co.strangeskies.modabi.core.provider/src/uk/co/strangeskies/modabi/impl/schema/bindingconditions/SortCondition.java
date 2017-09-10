package uk.co.strangeskies.modabi.impl.schema.bindingconditions;

import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;

import java.util.Comparator;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class SortCondition<T> extends BindingConditionImpl<T> {
  private final Comparator<? super T> comparator;

  public SortCondition(BindingConditionPrototype prototype, Comparator<? super T> comparator) {
    super(prototype);
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
                (ChildBindingPoint<T>) state.getBindingPoint(),
                previousBinding,
                (Class<? extends Comparator<?>>) comparator.getClass()),
            state);
      }
    };
  }
}
