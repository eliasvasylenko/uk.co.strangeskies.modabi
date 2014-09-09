package uk.co.strangeskies.modabi.schema.model;

public interface Model<T> extends
		AbstractModel<T, Model<T>, Model.Effective<T>> {
	interface Effective<T> extends Model<T>,
			AbstractModel.Effective<T, Model<T>, Effective<T>> {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<Effective<T>> getEffectiveClass() {
		return (Class) Effective.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default Class<Model<T>> getNodeClass() {
		return (Class) Model.class;
	}
}
