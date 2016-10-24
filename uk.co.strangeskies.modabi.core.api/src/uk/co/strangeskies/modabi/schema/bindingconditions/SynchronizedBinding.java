package uk.co.strangeskies.modabi.schema.bindingconditions;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;

/**
 * A binding condition which only allows processing of each item to proceed once
 * the previous item has completed.
 * 
 * @author Elias N Vasylenko
 */
public class SynchronizedBinding<T> implements BindingCondition<T> {
	static final SynchronizedBinding<?> INSTANCE = new SynchronizedBinding<>();

	/**
	 * @return an instance of {@link SynchronizedBinding}
	 */
	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> asynchronous() {
		return (BindingCondition<T>) INSTANCE;
	}

	protected SynchronizedBinding() {}

	@Override
	public BindingConditionEvaluation<T> forState(ProcessingContext state) {
		return new BindingConditionEvaluation<T>() {
			private boolean locked = false;

			@Override
			public synchronized void beginProcessingNext() {
				while (locked) {
					try {
						wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				locked = true;
			}

			@Override
			public synchronized void completeProcessingNext(T binding) {
				locked = false;
				notifyAll();
			}

			@Override
			public void endProcessing() {}
		};
	}
}
