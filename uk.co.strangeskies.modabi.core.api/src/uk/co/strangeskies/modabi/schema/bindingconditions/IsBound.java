package uk.co.strangeskies.modabi.schema.bindingconditions;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;

public class IsBound<T> implements BindingCondition<T> {
	private final ChildBindingPoint<?> target;

	protected IsBound(ChildBindingPoint<?> target) {
		this.target = target;
	}

	@Override
	public BindingConditionEvaluation<T> forState(ProcessingContext state) {
		throw new UnsupportedOperationException(); // TODO
	}
}