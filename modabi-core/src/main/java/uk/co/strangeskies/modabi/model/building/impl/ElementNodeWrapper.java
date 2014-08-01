package uk.co.strangeskies.modabi.model.building.impl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
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

		if (base.getBindingClass() != null
				&& !base.getBindingClass()
						.isAssignableFrom(component.getBindingClass()))
			throw new SchemaException(message);

		if (base.getUnbindingClass() != null
				&& !base.getUnbindingClass().isAssignableFrom(
						component.getUnbindingClass()))
			throw new SchemaException(message);

		if (!component.baseModel().containsAll(base.baseModel()))
			throw new SchemaException(message);

		if (base.getBindingStrategy() != null
				&& base.getBindingStrategy() != component.getBindingStrategy())
			throw new SchemaException(message);

		if (base.getUnbindingStrategy() != null
				&& base.getUnbindingStrategy() != component.getUnbindingStrategy())
			throw new SchemaException(message);

		if (base.getUnbindingMethodName() != null
				&& base.getUnbindingMethodName() != component.getUnbindingMethodName())
			throw new SchemaException(message);

		if (!base.children().isEmpty())
			throw new SchemaException(message);
	}

	@Override
	public Boolean isAbstract() {
		return component.isAbstract();
	}

	@Override
	public List<Model.Effective<? super T>> baseModel() {
		return Collections.unmodifiableList(component.baseModel());
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
	public List<? extends ChildNode.Effective<?>> children() {
		return component.children() + base.children();
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
}
