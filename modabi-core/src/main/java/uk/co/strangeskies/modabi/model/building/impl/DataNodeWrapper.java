package uk.co.strangeskies.modabi.model.building.impl;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public final class DataNodeWrapper<T> implements DataNode.Effective<T> {
	private final DataBindingType.Effective<T> component;
	private final DataNode.Effective<? super T> base;

	public DataNodeWrapper(DataBindingType.Effective<T> component) {
		this.component = component;
		base = null;
	}

	public DataNodeWrapper(DataBindingType.Effective<T> component,
			DataNode.Effective<? super T> base) {
		this.component = component;
		this.base = base;

		String message = "Cannot override '" + base.getName() + "' with '"
				+ component.getName() + "'.";

		if (base.getDataClass() != null
				&& !base.getDataClass().isAssignableFrom(component.getDataClass()))
			throw new SchemaException(message);

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

		if (base.getBindingStrategy() != null)
			throw new SchemaException(message);

		if (base.getUnbindingStrategy() != null)
			throw new SchemaException(message);

		if (base.getBindingClass() != null)
			throw new SchemaException(message);

		if (base.getUnbindingClass() != null)
			throw new SchemaException(message);

		if (base.getUnbindingMethodName() != null)
			throw new SchemaException(message);

		if (base.getProvidedUnbindingMethodParameterNames() != null)
			throw new SchemaException(message);

		if (!component.children().containsAll(base.children()))
			throw new SchemaException(message);
	}

	@Override
	public Class<T> getDataClass() {
		return component.getDataClass();
	}

	@Override
	public BufferedDataSource providedValueBuffer() {
		return base == null ? null : base.providedValueBuffer();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T providedValue() {
		return base == null ? null : (T) base.providedValue();
	}

	@Override
	public ValueResolution valueResolution() {
		return base == null ? null : base.valueResolution();
	}

	@Override
	public String getOutMethodName() {
		return base == null ? null : base.getOutMethodName();
	}

	@Override
	public Boolean isOutMethodIterable() {
		return base == null ? null : base.isOutMethodIterable();
	}

	@Override
	public Range<Integer> occurances() {
		return base == null ? null : base.occurances();
	}

	@Override
	public BindingStrategy getBindingStrategy() {
		return component.effective().getBindingStrategy();
	}

	@Override
	public Class<?> getBindingClass() {
		return component.getBindingClass();
	}

	@Override
	public UnbindingStrategy getUnbindingStrategy() {
		return component.effective().getUnbindingStrategy();
	}

	@Override
	public Class<?> getUnbindingClass() {
		return component.getUnbindingClass();
	}

	@Override
	public Class<?> getUnbindingFactoryClass() {
		return component.getUnbindingFactoryClass();
	}

	@Override
	public String getUnbindingMethodName() {
		return component.effective().getUnbindingMethodName();
	}

	@Override
	public List<QualifiedName> getProvidedUnbindingMethodParameterNames() {
		return component.effective().getProvidedUnbindingMethodParameterNames();
	}

	@Override
	public QualifiedName getName() {
		return component.getName();
	}

	@Override
	public List<ChildNode.Effective<?, ?>> children() {
		return component.children();
	}

	@Override
	public String getInMethodName() {
		return base == null ? null : base.getInMethodName();
	}

	@Override
	public Boolean isInMethodChained() {
		return base == null ? null : base.isInMethodChained();
	}

	@Override
	public DataNode.Format format() {
		return base == null ? null : base.format();
	}

	@Override
	public DataBindingType.Effective<T> type() {
		return component;
	}

	@Override
	public Boolean optional() {
		return base == null ? null : base.optional();
	}

	@Override
	public Method getOutMethod() {
		return base == null ? null : base.getOutMethod();
	}

	@Override
	public Method getUnbindingMethod() {
		return component.getUnbindingMethod();
	}

	@Override
	public List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters() {
		return component.getProvidedUnbindingMethodParameters();
	}

	@Override
	public Boolean isAbstract() {
		return component.isAbstract();
	}

	@Override
	public Method getInMethod() {
		return base == null ? null : base.getInMethod();
	}

	@Override
	public DataNode<T> source() {
		return this;
	}
}