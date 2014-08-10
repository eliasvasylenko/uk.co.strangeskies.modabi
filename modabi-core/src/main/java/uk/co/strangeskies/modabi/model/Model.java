package uk.co.strangeskies.modabi.model;

public interface Model<T> extends AbstractModel<T, Model.Effective<T>> {
	interface Effective<T> extends Model<T>,
			AbstractModel.Effective<T, Effective<T>> {
		@Override
		Model<T> source();
	}

	default Model<T> source() {
		return this;
	}
}
