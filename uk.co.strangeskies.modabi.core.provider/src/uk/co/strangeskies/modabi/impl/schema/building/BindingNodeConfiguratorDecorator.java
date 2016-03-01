package uk.co.strangeskies.modabi.impl.schema.building;

import java.util.List;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class BindingNodeConfiguratorDecorator<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T, ?, ?>, T>
		implements BindingNodeConfigurator<S, N, T> {
	private S component;

	public BindingNodeConfiguratorDecorator(S component) {
		this.component = component;
	}

	protected void setComponent(S component) {
		this.component = component;
	}

	public S getComponent() {
		return component;
	}

	@SuppressWarnings("unchecked")
	private S getThis() {
		return (S) this;
	}

	@Override
	public S bindingStrategy(BindingStrategy strategy) {
		component = component.bindingStrategy(strategy);
		return getThis();
	}

	@Override
	public S bindingType(TypeToken<?> bindingType) {
		component = component.bindingType(bindingType);
		return getThis();
	}

	@Override
	public S unbindingStrategy(UnbindingStrategy strategy) {
		component = component.unbindingStrategy(strategy);
		return getThis();
	}

	@Override
	public S unbindingFactoryType(TypeToken<?> factoryType) {
		component = component.unbindingFactoryType(factoryType);
		return getThis();
	}

	@Override
	public S unbindingType(TypeToken<?> unbindingType) {
		component = component.unbindingType(unbindingType);
		return getThis();
	}

	@Override
	public S unbindingMethod(String unbindingMethod) {
		component = component.unbindingMethod(unbindingMethod);
		return getThis();
	}

	@Override
	public S unbindingMethodUnchecked(boolean unchecked) {
		component = component.unbindingMethodUnchecked(unchecked);
		return getThis();
	}

	@Override
	public S providedUnbindingMethodParameters(List<QualifiedName> parameterNames) {
		component = component.providedUnbindingMethodParameters(parameterNames);
		return getThis();
	}

	@Override
	public S providedUnbindingMethodParameters(String... parameterNames) {
		component = component.providedUnbindingMethodParameters(parameterNames);
		return getThis();
	}

	@Override
	public S name(QualifiedName name) {
		component = component.name(name);
		return getThis();
	}

	@Override
	public S isAbstract(boolean isAbstract) {
		component = component.isAbstract(isAbstract);
		return getThis();
	}

	@Override
	public ChildBuilder addChild() {
		return component.addChild();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> BindingNodeConfigurator<?, ?, V> dataType(TypeToken<? extends V> bindingClass) {
		component = (S) component.dataType(bindingClass);
		return (BindingNodeConfigurator<?, ?, V>) this;
	}
}
