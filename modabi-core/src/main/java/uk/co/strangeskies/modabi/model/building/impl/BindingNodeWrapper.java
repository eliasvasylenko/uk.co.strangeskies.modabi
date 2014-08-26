package uk.co.strangeskies.modabi.model.building.impl;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.UnbindingStrategy;

public abstract class BindingNodeWrapper<T, C extends BindingNode.Effective<? super T, ?, ?>, B extends BindingChildNode.Effective<? super T, ?, ?>, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
		implements BindingChildNode.Effective<T, S, E> {
	private final C component;
	private final B base;

	public BindingNodeWrapper(C component) {
		this.component = component;
		base = null;
	}

	public BindingNodeWrapper(C component, B base) {
		this.component = component;
		this.base = base;

		String message = "Cannot override '" + base.getName() + "' with '"
				+ component.getName() + "'.";

		if (base.getDataClass() != null
				&& !base.getDataClass().isAssignableFrom(component.getDataClass()))
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

		if (!component.children().containsAll(base.children()))
			throw new SchemaException(message);
	}

	protected C getComponent() {
		return component;
	}

	protected B getBase() {
		return base;
	}

	@Override
	public final Boolean isOrdered() {
		return base == null ? null : base.isOrdered();
	}

	@Override
	public final Boolean isExtensible() {
		return base == null ? null : base.isExtensible();
	}

	@Override
	public final Boolean isAbstract() {
		return component.isAbstract();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Class<T> getDataClass() {
		return (Class<T>) component.getDataClass();
	}

	@Override
	public final BindingStrategy getBindingStrategy() {
		return component.getBindingStrategy();
	}

	@Override
	public final Class<?> getBindingClass() {
		return component.getBindingClass();
	}

	@Override
	public final UnbindingStrategy getUnbindingStrategy() {
		return component.getUnbindingStrategy();
	}

	@Override
	public final Class<?> getUnbindingClass() {
		return component.getUnbindingClass();
	}

	@Override
	public final String getUnbindingMethodName() {
		return component.getUnbindingMethodName();
	}

	@Override
	public final Method getUnbindingMethod() {
		return component.getUnbindingMethod();
	}

	@Override
	public final Class<?> getUnbindingFactoryClass() {
		return component.getUnbindingFactoryClass();
	}

	@Override
	public final QualifiedName getName() {
		return component.getName();
	}

	@Override
	public final List<ChildNode.Effective<?, ?>> children() {
		return component.children();
	}

	@Override
	public final Method getOutMethod() {
		return base == null ? null : base.getOutMethod();
	}

	@Override
	public final String getOutMethodName() {
		return base == null ? null : base.getOutMethodName();
	}

	@Override
	public final Boolean isOutMethodIterable() {
		return base == null ? null : base.isOutMethodIterable();
	}

	@Override
	public final Range<Integer> occurances() {
		return base == null ? null : base.occurances();
	}

	@Override
	public final String getInMethodName() {
		return base == null ? null : base.getInMethodName();
	}

	@Override
	public final Method getInMethod() {
		return base == null ? null : base.getInMethod();
	}

	@Override
	public final Boolean isInMethodChained() {
		return base == null ? null : base.isInMethodChained();
	}

	@Override
	public final Class<?> getPreInputClass() {
		return base == null ? null : base.getPreInputClass();
	}

	@Override
	public final Class<?> getPostInputClass() {
		return base == null ? null : base.getPostInputClass();
	}

	@Override
	public final List<QualifiedName> getProvidedUnbindingMethodParameterNames() {
		return component.getProvidedUnbindingMethodParameterNames();
	}

	@Override
	public final List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters() {
		return component.getProvidedUnbindingMethodParameters();
	}

	@SuppressWarnings("unchecked")
	@Override
	public S source() {
		return (S) this;
	}
}
