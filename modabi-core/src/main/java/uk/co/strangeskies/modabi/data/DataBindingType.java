package uk.co.strangeskies.modabi.data;

public interface DataBindingType<T> extends AbstractDataBindingType<T> {
	EffectiveDataBindingType<T> getEffective();
}
