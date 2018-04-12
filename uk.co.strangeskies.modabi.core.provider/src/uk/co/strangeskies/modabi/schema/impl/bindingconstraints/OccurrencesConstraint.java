package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingConstraintSpecification;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingProcedure;
import uk.co.strangeskies.modabi.schema.Child;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class OccurrencesConstraint<T> implements BindingConstraint<T> {
  private final Interval<Integer> range;

  public OccurrencesConstraint(Interval<Integer> range) {
    this.range = range;
  }

  @Override
  public BindingProcedure<T> procedeWithState(BindingContext state) {
    return new BindingProcedure<T>() {
      private int count = 0;

      public void failProcess() {
        throw new BindingException(
            MESSAGES.mustHaveDataWithinRange((Child<?>) state.getBindingPoint(), range),
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
  public BindingConstraintSpecification getSpecification() {
    return v -> v.occurrences(range);
  }
}
