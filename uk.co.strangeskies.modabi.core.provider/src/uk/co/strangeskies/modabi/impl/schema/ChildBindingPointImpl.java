package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;

import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

public class ChildBindingPointImpl<T> extends BindingPointImpl<T> implements ChildBindingPoint<T> {
	protected ChildBindingPointImpl(ChildBindingPointConfiguratorImpl<T> configurator) {
		super(configurator);
		// TODO Auto-generated constructor stub
	}

	@Override
	public TypeToken<ChildBindingPoint<T>> getThisType() {
		return new TypeToken<ChildBindingPoint<T>>() {}.withTypeArgument(new TypeParameter<T>() {}, dataType());
	}

	@Override
	public boolean extensible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TypeToken<?> preInputType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeToken<?> postInputType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindingCondition<? super T> bindingCondition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> providedValues() {
		// TODO Auto-generated method stub
		return null;
	}
}
