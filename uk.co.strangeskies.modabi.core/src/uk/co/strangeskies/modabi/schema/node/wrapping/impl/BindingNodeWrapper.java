package uk.co.strangeskies.modabi.schema.node.wrapping.impl;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;

public abstract class BindingNodeWrapper<T, C extends BindingNode.Effective<? super T, ?, ?>, B extends BindingNode.Effective<? super T, ?, ?>, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		implements BindingNode.Effective<T, S, E> {
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

		if (base.getBindingStrategy() != null
				&& base.getBindingStrategy() != component.getBindingStrategy())
			throw new SchemaException(message);

		if (base.getUnbindingStrategy() != null
				&& base.getUnbindingStrategy() != component.getUnbindingStrategy())
			throw new SchemaException(message);

		if (base.getBindingClass() != null
				&& !base.getBindingClass()
						.isAssignableFrom(component.getBindingClass()))
			throw new SchemaException(message);

		if (base.getUnbindingClass() != null
				&& !base.getUnbindingClass().isAssignableFrom(
						component.getUnbindingClass()))
			throw new SchemaException(message);

		if (base.getUnbindingMethodName() != null
				&& base.getUnbindingMethodName() != component.getUnbindingMethodName())
			throw new SchemaException(message);

		if (base.getProvidedUnbindingMethodParameterNames() != null
				&& base.getProvidedUnbindingMethodParameterNames() != component
						.getProvidedUnbindingMethodParameterNames())
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
