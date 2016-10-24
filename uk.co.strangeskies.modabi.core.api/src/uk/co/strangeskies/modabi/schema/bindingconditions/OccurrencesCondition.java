package uk.co.strangeskies.modabi.schema.bindingconditions;

import uk.co.strangeskies.mathematics.Range;
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
	private final Range<Integer> range;

	public static <T> BindingCondition<T> occurrences(Range<Integer> range) {
		return new OccurrencesCondition<>(range);
	}

	protected OccurrencesCondition(Range<Integer> range) {
		this.range = range;
	}

	@Override
	public BindingConditionEvaluation<T> forState(ProcessingContext state) {
		return new BindingConditionEvaluation<T>() {
			private int count = 0;

			public void failProcess() {
				throw new ProcessingException(p -> p.mustHaveDataWithinRange((ChildBindingPoint<?>) state.getNode(), range),
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
