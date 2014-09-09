package uk.co.strangeskies.modabi.schema.model.building.impl;

import java.util.List;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public final class DataNodeWrapper<T>
		extends
		BindingNodeWrapper<T, DataBindingType.Effective<T>, DataNode.Effective<? super T>, DataNode<T>, DataNode.Effective<T>>
		implements DataNode.Effective<T> {
	public DataNodeWrapper(DataBindingType.Effective<T> component) {
		super(component);
	}

	public DataNodeWrapper(DataBindingType.Effective<T> component,
			DataNode.Effective<? super T> base) {
		super(component, base);

		String message = "Cannot override '" + base.getName() + "' with '"
				+ component.getName() + "'.";

		if (base.providedValue() != null
				&& !component.getDataClass().isAssignableFrom(
						base.providedValue().getClass()))
			throw new SchemaException(message);

		DataBindingType.Effective<? super T> check = component;
		while (!check.equals(base.type())) {
			check = (DataBindingType.Effective<? super T>) check.baseType();
			if (check == null)
				throw new SchemaException(message);
		}

		System.out.println(base != null ? base.getName()
				: "" + " : " + component != null ? component.getName() : "");
	}

	@Override
	public DataSource providedValueBuffer() {
		return getBase() == null ? null : getBase().providedValueBuffer();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> providedValue() {
		return getBase() == null ? null : (List<T>) getBase().providedValue();
	}

	@Override
	public ValueResolution valueResolution() {
		return getBase() == null ? null : getBase().valueResolution();
	}

	@Override
	public DataNode.Format format() {
		return getBase() == null ? null : getBase().format();
	}

	@Override
	public DataBindingType.Effective<T> type() {
		return getComponent();
	}

	@Override
	public Boolean optional() {
		return getBase() == null ? null : getBase().optional();
	}
}