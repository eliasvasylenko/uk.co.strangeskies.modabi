package uk.co.strangeskies.modabi.model.building.impl;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public final class DataNodeWrapper<T> implements DataNode.Effective<T> {
	private final DataBindingType.Effective<T> type;

	public DataNodeWrapper(DataBindingType<T> node) {
		this.type = node.effective();
	}

	@Override
	public BufferedDataSource providedValueBuffer() {
		return null;
	}

	@Override
	public T providedValue() {
		return null;
	}

	@Override
	public ValueResolution valueResolution() {
		return null;
	}

	@Override
	public String getOutMethodName() {
		return null;
	}

	@Override
	public Boolean isOutMethodIterable() {
		return null;
	}

	@Override
	public Range<Integer> occurances() {
		return null;
	}

	@Override
	public Class<T> getDataClass() {
		return type.getDataClass();
	}

	@Override
	public BindingStrategy getBindingStrategy() {
		return type.effective().getBindingStrategy();
	}

	@Override
	public Class<?> getBindingClass() {
		return type.getBindingClass();
	}

	@Override
	public UnbindingStrategy getUnbindingStrategy() {
		return type.effective().getUnbindingStrategy();
	}

	@Override
	public Class<?> getUnbindingClass() {
		return type.getUnbindingClass();
	}

	@Override
	public Class<?> getUnbindingFactoryClass() {
		return type.getUnbindingFactoryClass();
	}

	@Override
	public String getUnbindingMethodName() {
		return type.effective().getUnbindingMethodName();
	}

	@Override
	public List<String> getProvidedUnbindingMethodParameterNames() {
		return type.effective().getProvidedUnbindingMethodParameterNames();
	}

	@Override
	public String getName() {
		return type.getName();
	}

	@Override
	public List<ChildNode.Effective<?, ?>> children() {
		return type.children();
	}

	@Override
	public String getInMethodName() {
		return null;
	}

	@Override
	public Boolean isInMethodChained() {
		return null;
	}

	@Override
	public DataNode.Format format() {
		return null;
	}

	@Override
	public DataBindingType.Effective<T> type() {
		return null;
	}

	@Override
	public Boolean optional() {
		return null;
	}

	@Override
	public Method getOutMethod() {
		return null;
	}

	@Override
	public Method getUnbindingMethod() {
		return type.getUnbindingMethod();
	}

	@Override
	public List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters() {
		return type.getProvidedUnbindingMethodParameters();
	}

	@Override
	public Method getInMethod() {
		return null;
	}

	@Override
	public DataNode<T> source() {
		return this;
	}
}