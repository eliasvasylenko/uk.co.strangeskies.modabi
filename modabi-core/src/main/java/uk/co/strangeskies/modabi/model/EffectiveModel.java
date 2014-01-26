package uk.co.strangeskies.modabi.model;

public interface EffectiveModel<T> extends Model<T> {
	@Override
	public default EffectiveModel<T> effectiveModel() {
		return this;
	}
}
