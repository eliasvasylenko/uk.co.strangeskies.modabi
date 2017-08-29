package uk.co.strangeskies.modabi.schema.bindingconditions;

import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;

import uk.co.strangeskies.mathematics.Interval;
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
public class OccurrencesCondition<T> implements BindingCondition<T> {
  private final Interval<Integer> range;

  public static <T> BindingCondition<T> occurrences(Interval<Integer> range) {
    return new OccurrencesCondition<>(range);
  }

  protected OccurrencesCondition(Interval<Integer> range) {
    this.range = range;
  }

  @Override
  public BindingConditionEvaluation<T> forState(ProcessingContext state) {
    return new BindingConditionEvaluation<T>() {
      private int count = 0;

      public void failProcess() {
        throw new ProcessingException(
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
}
