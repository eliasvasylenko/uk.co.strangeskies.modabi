/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.impl.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.impl.schema.utilities.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class BindingNodeConfiguratorImpl<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T, N, ?>, T>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		BindingNodeConfigurator<S, N, T> {
	private TypeToken<T> dataType;
	private TypeToken<T> effectiveDataType;
	private BoundSet inferenceBounds = new BoundSet();

	private BindingStrategy bindingStrategy;
	private TypeToken<?> bindingType;
	private TypeToken<?> effectiveBindingType;

	private UnbindingStrategy unbindingStrategy;
	private TypeToken<?> unbindingType;
	private TypeToken<?> effectiveUnbindingType;
	private String unbindingMethod;
	private Boolean unbindingMethodUnchecked;

	private TypeToken<?> unbindingFactoryType;
	private TypeToken<?> effectiveUnbindingFactoryType;

	private List<QualifiedName> unbindingParameterNames;

	protected final BoundSet getInferenceBounds() {
		return inferenceBounds;
	}

	public TypeToken<T> getEffectiveDataType() {
		return effectiveDataType;
	}

	public TypeToken<?> getEffectiveBindingType() {
		return effectiveBindingType;
	}

	public TypeToken<?> getEffectiveUnbindingType() {
		return effectiveUnbindingType;
	}

	public TypeToken<?> getEffectiveUnbindingFactoryType() {
		return effectiveUnbindingFactoryType;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ChildrenConfigurator createChildrenConfigurator() {
		OverrideMerge<? extends BindingNode<?, ?, ?>, ? extends BindingNodeConfigurator<?, ?, ?>> overrideMerge = overrideMerge(
				null, this);

		/*
		 * Get declared data types, or overridden types thereof.
		 */
		effectiveDataType = (TypeToken<T>) (TypeToken<? super T>) overrideMerge
				.getValueWithOverride(this.dataType, BindingNode::getDataType,
						(o, n) -> true);
		effectiveBindingType = overrideMerge.getValueWithOverride(this.bindingType,
				BindingNode::getBindingType, (o, n) -> true);
		effectiveUnbindingType = overrideMerge.getValueWithOverride(
				this.unbindingType, BindingNode::getUnbindingType, (o, n) -> true);
		effectiveUnbindingFactoryType = overrideMerge.getValueWithOverride(
				this.unbindingFactoryType, BindingNode::getUnbindingFactoryType,
				(o, n) -> true);

		/*
		 * Incorporate bounds from inherited types.
		 */
		if (effectiveDataType != null) {
			if (this.dataType != null) {
				for (TypeToken<?> overriddenType : overrideMerge
						.getOverridenValues(BindingNode::getDataType)) {
					effectiveDataType = effectiveDataType.withUpperBound(overriddenType
							.deepCopy());
				}
			}

			effectiveDataType.incorporateInto(inferenceBounds);
		}
		if (effectiveBindingType != null) {
			if (this.bindingType != null) {
				for (TypeToken<?> overriddenType : overrideMerge
						.getOverridenValues(BindingNode::getBindingType)) {
					effectiveBindingType = effectiveBindingType
							.withUpperBound(overriddenType.deepCopy());
				}
			}

			effectiveBindingType.incorporateInto(inferenceBounds);
		}
		if (effectiveUnbindingType != null) {
			if (this.unbindingType != null) {
				for (TypeToken<?> overriddenType : overrideMerge
						.getOverridenValues(BindingNode::getUnbindingType)) {
					effectiveUnbindingType = effectiveUnbindingType
							.withUpperBound(overriddenType.deepCopy());
				}
			}

			effectiveUnbindingType.incorporateInto(inferenceBounds);
		}
		if (effectiveUnbindingFactoryType != null) {
			if (this.unbindingFactoryType != null) {
				for (TypeToken<?> overriddenType : overrideMerge
						.getOverridenValues(BindingNode::getUnbindingFactoryType)) {
					effectiveUnbindingFactoryType = effectiveUnbindingFactoryType
							.withUpperBound(overriddenType.deepCopy());
				}
			}

			effectiveUnbindingFactoryType.incorporateInto(inferenceBounds);
		}

		/*
		 * Effective binding and unbinding types.
		 */

		BindingStrategy bindingStrategy = overrideMerge.getValueWithOverride(
				this.bindingStrategy, BindingNode::getBindingStrategy);
		TypeToken<?> inputTarget;
		if (effectiveBindingType != null)
			inputTarget = effectiveBindingType;
		else if (bindingStrategy != null || !isChildContextAbstract())
			inputTarget = effectiveDataType;
		else
			inputTarget = null;

		UnbindingStrategy unbindingStrategy = overrideMerge.getValueWithOverride(
				this.unbindingStrategy, BindingNode::getUnbindingStrategy);
		TypeToken<?> outputSource;
		if (effectiveUnbindingType != null)
			outputSource = effectiveUnbindingType;
		else if (unbindingStrategy != null || !isChildContextAbstract())
			outputSource = effectiveDataType;
		else
			outputSource = null;

		/*
		 * TODO make 'hasInput' optional for IMPLEMENT_IN_PLACE
		 */
		return new SequentialChildrenConfigurator(
				new SchemaNodeConfigurationContext<ChildNode<?, ?>>() {
					@Override
					public BoundSet boundSet() {
						return inferenceBounds;
					}

					@Override
					public DataLoader dataLoader() {
						return getDataLoader();
					}

					@Override
					public boolean isAbstract() {
						return isChildContextAbstract();
					}

					@Override
					public boolean isInputExpected() {
						return true;
					}

					@Override
					public boolean isInputDataOnly() {
						return isDataContext();
					}

					@Override
					public boolean isConstructorExpected() {
						return bindingStrategy == BindingStrategy.CONSTRUCTOR;
					}

					@Override
					public boolean isStaticMethodExpected() {
						return bindingStrategy == BindingStrategy.STATIC_FACTORY;
					}

					@Override
					public Namespace namespace() {
						return getNamespace();
					}

					@Override
					public TypeToken<?> inputTargetType(QualifiedName node) {
						return inputTarget;
					}

					@Override
					public TypeToken<?> outputSourceType() {
						return outputSource;
					}

					@Override
					public void addChild(ChildNode<?, ?> result) {}

					@Override
					public <U extends ChildNode<?, ?>> List<U> overrideChild(
							QualifiedName id, TypeToken<U> nodeClass) {
						return null;
					}

					@Override
					public List<? extends SchemaNode<?, ?>> overriddenNodes() {
						return getOverriddenNodes();
					}
				});
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> BindingNodeConfigurator<?, ?, V> dataType(
			TypeToken<? extends V> dataType) {
		assertConfigurable(this.dataType);

		if (dataType == null)
			throw new IllegalArgumentException("Data type must not be null.");

		if (!dataType.getResolver().getBounds()
				.isProperType(dataType.getAnnotatedDeclaration().getType()))
			throw new IllegalArgumentException("Data type must be proper.");

		this.dataType = (TypeToken<T>) dataType;

		return (BindingNodeConfigurator<?, ?, V>) this;
	}

	public TypeToken<T> getDataType() {
		return dataType;
	}

	protected abstract boolean isDataContext();

	@Override
	public final S bindingType(TypeToken<?> bindingClass) {
		assertConfigurable(this.bindingType);
		this.bindingType = bindingClass;

		return getThis();
	}

	public TypeToken<?> getBindingType() {
		return bindingType;
	}

	@Override
	public S unbindingType(TypeToken<?> unbindingClass) {
		assertConfigurable(this.unbindingType);
		this.unbindingType = unbindingClass;

		return getThis();
	}

	public TypeToken<?> getUnbindingType() {
		return unbindingType;
	}

	@Override
	public S unbindingMethod(String unbindingMethod) {
		assertConfigurable(this.unbindingMethod);
		this.unbindingMethod = unbindingMethod;

		return getThis();
	}

	public String getUnbindingMethod() {
		return unbindingMethod;
	}

	@Override
	public S unbindingMethodUnchecked(boolean unchecked) {
		assertConfigurable(this.unbindingMethodUnchecked);
		this.unbindingMethodUnchecked = unchecked;

		return getThis();
	}

	public Boolean getUnbindingMethodUnchecked() {
		return unbindingMethodUnchecked;
	}

	@Override
	public final S bindingStrategy(BindingStrategy strategy) {
		assertConfigurable(bindingStrategy);
		bindingStrategy = strategy;

		return getThis();
	}

	public BindingStrategy getBindingStrategy() {
		return bindingStrategy;
	}

	@Override
	public final S unbindingStrategy(UnbindingStrategy strategy) {
		assertConfigurable(unbindingStrategy);
		unbindingStrategy = strategy;

		return getThis();
	}

	public UnbindingStrategy getUnbindingStrategy() {
		return unbindingStrategy;
	}

	@Override
	public S unbindingFactoryType(TypeToken<?> factoryClass) {
		assertConfigurable(unbindingFactoryType);
		unbindingFactoryType = factoryClass;

		return getThis();
	}

	public TypeToken<?> getUnbindingFactoryType() {
		return unbindingFactoryType;
	}

	@Override
	public final S providedUnbindingMethodParameters(
			List<QualifiedName> parameterNames) {
		assertConfigurable(unbindingParameterNames);
		unbindingParameterNames = new ArrayList<>(parameterNames);

		return getThis();
	}

	public List<QualifiedName> getUnbindingParameterNames() {
		return unbindingParameterNames;
	}

	@Override
	public S providedUnbindingMethodParameters(String... parameterNames) {
		return providedUnbindingMethodParameters(Arrays.asList(parameterNames)
				.stream().map(n -> new QualifiedName(n, getName().getNamespace()))
				.collect(Collectors.toList()));
	}
}
