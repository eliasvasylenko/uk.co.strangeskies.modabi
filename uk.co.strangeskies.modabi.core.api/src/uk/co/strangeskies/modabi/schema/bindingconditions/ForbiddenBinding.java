package uk.co.strangeskies.modabi.schema.bindingconditions;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;

/**
 * A simple rule for binding points which are required to never be processed.
 * 
 * @author Elias N Vasylenko
 */
public class ForbiddenBinding<T> implements BindingCondition<T> {
	static final ForbiddenBinding<?> INSTANCE = new ForbiddenBinding<>();

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> forbidden() {
		return (BindingCondition<T>) ForbiddenBinding.INSTANCE;
	}

	protected ForbiddenBinding() {}

	@Override
	public BindingConditionEvaluation<T> forState(ProcessingContext state) {
		return new BindingConditionEvaluation<T>() {
			private boolean processed = false;

			@Override
			public void beginProcessingNext() {
				processed = true;
			}

			@Override
			public void completeProcessingNext(T binding) {}

			@Override
			public void endProcessing() {
				if (processed) {
					throw new ProcessingException(p -> p.mustNotHaveData(state.getNode().name()), state);
				}
			}
		};
	}
}
