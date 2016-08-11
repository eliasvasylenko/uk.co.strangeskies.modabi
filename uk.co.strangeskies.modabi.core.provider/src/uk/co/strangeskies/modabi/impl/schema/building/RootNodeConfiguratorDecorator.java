/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl.schema.building;

import java.util.List;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.processing.InputBindingStrategy;
import uk.co.strangeskies.modabi.processing.OutputBindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.RootNode;
import uk.co.strangeskies.modabi.schema.RootNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class RootNodeConfiguratorDecorator<S extends RootNodeConfigurator<S, N, T>, N extends RootNode<T, N>, T>
		implements RootNodeConfigurator<S, N, T> {
	private S component;

	public RootNodeConfiguratorDecorator(S component) {
		this.component = component;
	}

	protected void setComponent(S component) {
		this.component = component;
	}

	public S getComponent() {
		return component;
	}

	@Override
	public QualifiedName getName() {
		return component.getName();
	}

	@Override
	@SuppressWarnings("unchecked")
	public S getThis() {
		return (S) this;
	}

	@Override
	public S export(boolean export) {
		component.export(export);
		return getThis();
	}

	@Override
	public Boolean getExported() {
		return component.getExported();
	}

	@Override
	public S inputBindingStrategy(InputBindingStrategy strategy) {
		component = component.inputBindingStrategy(strategy);
		return getThis();
	}

	@Override
	public InputBindingStrategy getInputBindingStrategy() {
		return component.getInputBindingStrategy();
	}

	@Override
	public S inputBindingType(String bindingType) {
		component = component.inputBindingType(bindingType);
		return getThis();
	}

	@Override
	public S inputBindingType(TypeToken<?> bindingType) {
		component = component.inputBindingType(bindingType);
		return getThis();
	}

	@Override
	public TypeToken<?> getInputBindingType() {
		return component.getInputBindingType();
	}

	@Override
	public S outputBindingStrategy(OutputBindingStrategy strategy) {
		component = component.outputBindingStrategy(strategy);
		return getThis();
	}

	@Override
	public OutputBindingStrategy getOutputBindingStrategy() {
		return component.getOutputBindingStrategy();
	}

	@Override
	public S outputBindingFactoryType(String factoryType) {
		component = component.outputBindingFactoryType(factoryType);
		return getThis();
	}

	@Override
	public S outputBindingFactoryType(TypeToken<?> factoryType) {
		component = component.outputBindingFactoryType(factoryType);
		return getThis();
	}

	@Override
	public TypeToken<?> getOutputBindingFactoryType() {
		return component.getOutputBindingFactoryType();
	}

	@Override
	public S outputBindingType(String unbindingType) {
		component = component.outputBindingType(unbindingType);
		return getThis();
	}

	@Override
	public S outputBindingType(TypeToken<?> unbindingType) {
		component = component.outputBindingType(unbindingType);
		return getThis();
	}

	@Override
	public TypeToken<?> getOutputBindingType() {
		return component.getOutputBindingType();
	}

	@Override
	public S outputBindingMethod(String unbindingMethod) {
		component = component.outputBindingMethod(unbindingMethod);
		return getThis();
	}

	@Override
	public String getOutputBindingMethod() {
		return component.getOutputBindingMethod();
	}

	@Override
	public S outputBindingMethodUnchecked(boolean unchecked) {
		component = component.outputBindingMethodUnchecked(unchecked);
		return getThis();
	}

	@Override
	public Boolean getOutputBindingMethodUnchecked() {
		return component.getOutputBindingMethodUnchecked();
	}

	@Override
	public S providedOutputBindingMethodParameters(List<QualifiedName> parameterNames) {
		component = component.providedOutputBindingMethodParameters(parameterNames);
		return getThis();
	}

	@Override
	public S providedOutputBindingMethodParameters(String... parameterNames) {
		component = component.providedOutputBindingMethodParameters(parameterNames);
		return getThis();
	}

	@Override
	public List<QualifiedName> getProvidedOutputBindingMethodParameters() {
		return component.getProvidedOutputBindingMethodParameters();
	}

	@Override
	public S name(QualifiedName name) {
		component = component.name(name);
		return getThis();
	}

	@Override
	public S concrete(boolean abstractness) {
		component = component.concrete(abstractness);
		return getThis();
	}

	@Override
	public Boolean getConcrete() {
		return component.getConcrete();
	}

	@Override
	public ChildBuilder addChild() {
		return component.addChild();
	}

	@SuppressWarnings("unchecked")
	@Override
	public BindingNodeConfigurator<?, ?, ? extends T> dataType(String bindingClass) {
		component = (S) component.dataType(bindingClass);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> BindingNodeConfigurator<?, ?, V> dataType(TypeToken<? extends V> bindingClass) {
		component = (S) component.dataType(bindingClass);
		return (BindingNodeConfigurator<?, ?, V>) this;
	}

	@Override
	public TypeToken<T> getDataType() {
		return component.getDataType();
	}

	@Override
	public List<? extends ChildNodeConfigurator<?, ?>> getChildren() {
		return getComponent().getChildren();
	}
}
