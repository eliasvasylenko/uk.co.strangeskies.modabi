package uk.co.strangeskies.modabi.model;

public interface Model<T> extends AbstractModel<T> {
	public EffectiveModel<T> effectiveModel();
}
