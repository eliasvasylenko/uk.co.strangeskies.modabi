package uk.co.strangeskies.modabi.model.building.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public class ElementNodeWrapper<T> implements ElementNode.Effective<T> {
	private final Class<T> dataClass;
	private final AbstractModel.Effective<? super T, ?> component;
	private final ElementNode.Effective<? super T> base;

	public ElementNodeWrapper(AbstractModel.Effective<? super T, ?> component,
			Class<T> dataClass) {
		this.dataClass = dataClass;
		this.component = component;
		base = null;
	}

	public ElementNodeWrapper(Model.Effective<T> component,
			ElementNode.Effective<? super T> base) {
		this.dataClass = component.getDataClass();
		this.component = component;
		this.base = base;

		String message = component.getName() + " / " + base.getName();

		if (base.getDataClass() != null
				&& !base.getDataClass().isAssignableFrom(component.getDataClass()))
			throw new SchemaException(message);

		if (!component.baseModel().containsAll(base.baseModel()))
			throw new SchemaException(message);

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

		System.out.println(base.getName()
				+ " : "
				+ base.children().stream().map(SchemaNode::getName)
						.collect(Collectors.toList()));

		// if (!base.children().isEmpty())
		// throw new SchemaException(message); TODO URGENT!
	}

	@Override
	public Boolean isAbstract() {
		return component.isAbstract();
	}

	@Override
	public Set<Model.Effective<? super T>> baseModel() {
		return Collections.unmodifiableSet(component.baseModel());
	}

	@Override
	public Class<T> getDataClass() {
		return dataClass;
	}

	@Override
	public BindingStrategy getBindingStrategy() {
		return component.getBindingStrategy();
	}

	@Override
	public Class<?> getBindingClass() {
		return component.getBindingClass();
	}

	@Override
	public UnbindingStrategy getUnbindingStrategy() {
		return component.getUnbindingStrategy();
	}

	@Override
	public Class<?> getUnbindingClass() {
		return component.getUnbindingClass();
	}

	@Override
	public String getUnbindingMethodName() {
		return component.getUnbindingMethodName();
	}

	@Override
	public Method getUnbindingMethod() {
		return component.getUnbindingMethod();
	}

	@Override
	public Class<?> getUnbindingFactoryClass() {
		return component.getUnbindingFactoryClass();
	}

	@Override
	public String getName() {
		return component.getName();
	}

	@Override
	public List<ChildNode.Effective<?>> children() {
		return component.children();
	}

	@Override
	public Method getOutMethod() {
		return base == null ? null : base.getOutMethod();
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
	public String getInMethodName() {
		return base == null ? null : base.getInMethodName();
	}

	@Override
	public Method getInMethod() {
		return base == null ? null : base.getInMethod();
	}

	@Override
	public Boolean isInMethodChained() {
		return base == null ? null : base.isInMethodChained();
	}

	@Override
	public Class<?> getPreInputClass() {
		return base == null ? null : base.getPreInputClass();
	}

	@Override
	public Class<?> getPostInputClass() {
		return base == null ? null : base.getPostInputClass();
	}

	@Override
	public List<String> getProvidedUnbindingMethodParameterNames() {
		return component.getProvidedUnbindingMethodParameterNames();
	}

	@Override
	public List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters() {
		return component.getProvidedUnbindingMethodParameters();
	}
}
