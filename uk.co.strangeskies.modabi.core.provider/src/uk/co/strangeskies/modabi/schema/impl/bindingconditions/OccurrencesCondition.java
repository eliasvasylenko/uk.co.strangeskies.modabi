package uk.co.strangeskies.modabi.schema.impl.bindingconditions;

import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class OccurrencesCondition<T> implements BindingCondition<T> {
  private final Interval<Integer> range;

  public OccurrencesCondition(Interval<Integer> range) {
    this.range = range;
  }

  @Override
  public BindingConditionEvaluation<T> forState(BindingContext state) {
    return new BindingConditionEvaluation<T>() {
      private int count = 0;

      public void failProcess() {
        throw new BindingException(
            MESSAGES.mustHaveDataWithinRange((ChildBindingPoint<?>) state.getBindingPoint(), range),
            state);
      }

      @Override
      public void beginProcessingNext() {
        if (range.isValueAbove(++count)) {
          failProcess();
        }
      }

      @Override
      public void completeProcessingNext(T binding) {}

      @Override
      public void endProcessing() {
        if (!range.contains(count)) {
          failProcess();
        }
      }
    };
  }

  @Override
  public BindingConditionPrototype getPrototype() {
    return v -> v.occurrences(range);
  }
}
