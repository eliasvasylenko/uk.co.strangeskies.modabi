package uk.co.strangeskies.modabi.model;

import uk.co.strangeskies.modabi.processing.UnbindingContext;

public interface Model<T> extends AbstractModel<T> {
	public EffectiveModel<T> effectiveModel();

	void unbind(UnbindingContext<T> context);
}
