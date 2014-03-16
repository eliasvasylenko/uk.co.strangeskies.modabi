package uk.co.strangeskies.modabi.model;

public interface EffectiveModel<T> extends AbstractModel<T> {
	@Override
	public default Class<?> getPreInputClass() {
		return null;
	}

	@Override
	public default Class<?> getPostInputClass() {
		return null;
	}
}
