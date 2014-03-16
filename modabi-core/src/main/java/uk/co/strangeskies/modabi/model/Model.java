package uk.co.strangeskies.modabi.model;

public interface Model<T> extends AbstractModel<T> {
	public EffectiveModel<T> effectiveModel();

	@Override
	public default Class<?> getPreInputClass() {
		return null;
	}

	@Override
	public default Class<?> getPostInputClass() {
		return null;
	}
}
