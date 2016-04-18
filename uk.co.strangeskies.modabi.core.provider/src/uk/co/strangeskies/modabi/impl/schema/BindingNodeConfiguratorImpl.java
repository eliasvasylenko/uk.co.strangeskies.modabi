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
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

public abstract class BindingNodeConfiguratorImpl<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T, N, ?>, T>
		extends SchemaNodeConfiguratorImpl<S, N> implements BindingNodeConfigurator<S, N, T> {
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

	@Override
	protected ChildrenConfigurator createChildrenConfigurator() {
		OverrideMerge<? extends BindingNode<T, ?, ?>, ? extends BindingNodeConfigurator<?, ?, ?>> overrideMerge = overrideMerge(
				null, this);

		/*
		 * Get declared data types, or overridden types thereof.
		 */
		effectiveDataType = overrideMerge.getOverride(BindingNode::getDataType, this.dataType)
				.orMerged((o, n) -> o.withEquality(n)).validate((o, n) -> true).get();
		effectiveBindingType = overrideMerge.getOverride(BindingNode::getBindingType, this.bindingType)
				.orMerged((o, n) -> o.withEquality(n)).validate((o, n) -> true).get();
		effectiveUnbindingType = overrideMerge.getOverride(BindingNode::getUnbindingType, this.unbindingType)
				.orMerged((o, n) -> o.withEquality(n)).validate((o, n) -> true).get();
		effectiveUnbindingFactoryType = overrideMerge
				.getOverride(BindingNode::getUnbindingFactoryType, this.unbindingFactoryType)
				.orMerged((o, n) -> o.withEquality(n)).validate((o, n) -> true).get();

		/*
		 * Incorporate bounds from inherited types.
		 */
		if (effectiveDataType != null) {
			effectiveDataType = effectiveDataType.deepCopy();

			if (this.dataType != null) {
				for (TypeToken<?> overriddenType : overrideMerge.getOverridenValues(BindingNode::getDataType)) {
					/*
					 * only perform more complex type override behaviour if not already
					 * directly assignable
					 */
					if (!overriddenType.isProper()
							|| !Types.isAssignable(effectiveDataType.getType(), overriddenType.getType())) {
						effectiveDataType = effectiveDataType.withUpperBound(overriddenType.deepCopy());
					}
				}
			}

			effectiveDataType.incorporateInto(inferenceBounds);
		}
		if (effectiveBindingType != null) {
			effectiveBindingType = effectiveBindingType.deepCopy();

			if (this.bindingType != null) {
				for (TypeToken<?> overriddenType : overrideMerge.getOverridenValues(BindingNode::getBindingType)) {
					/*
					 * only perform more complex type override behaviour if not already
					 * directly assignable
					 */
					if (!overriddenType.isProper()
							|| !Types.isAssignable(effectiveBindingType.getType(), overriddenType.getType())) {
						effectiveBindingType = effectiveBindingType.withUpperBound(overriddenType.deepCopy());
					}
				}
			}

			effectiveBindingType.incorporateInto(inferenceBounds);
		}
		if (effectiveUnbindingType != null) {
			effectiveUnbindingType = effectiveUnbindingType.deepCopy();

			if (this.unbindingType != null) {
				for (TypeToken<?> overriddenType : overrideMerge.getOverridenValues(BindingNode::getUnbindingType)) {
					/*
					 * only perform more complex type override behaviour if not already
					 * directly assignable
					 */
					if (!overriddenType.isProper()
							|| !Types.isAssignable(effectiveUnbindingType.getType(), overriddenType.getType())) {
						effectiveUnbindingType = effectiveUnbindingType.withUpperBound(overriddenType.deepCopy());
					}
				}
			}

			effectiveUnbindingType.incorporateInto(inferenceBounds);
		}
		if (effectiveUnbindingFactoryType != null) {
			effectiveUnbindingFactoryType = effectiveUnbindingFactoryType.deepCopy();

			if (this.unbindingFactoryType != null) {
				for (TypeToken<?> overriddenType : overrideMerge.getOverridenValues(BindingNode::getUnbindingFactoryType)) {
					/*
					 * only perform more complex type override behaviour if not already
					 * directly assignable
					 */
					if (!overriddenType.isProper()
							|| !Types.isAssignable(effectiveUnbindingFactoryType.getType(), overriddenType.getType())) {
						effectiveUnbindingFactoryType = effectiveUnbindingFactoryType.withUpperBound(overriddenType.deepCopy());
					}
				}
			}

			effectiveUnbindingFactoryType.incorporateInto(inferenceBounds);
		}

		/*
		 * Effective binding and unbinding types.
		 */

		BindingStrategy bindingStrategy = overrideMerge.getOverride(BindingNode::getBindingStrategy, this.bindingStrategy)
				.get();
		TypeToken<?> inputTarget;
		if (effectiveBindingType != null)
			inputTarget = effectiveBindingType;
		else if (bindingStrategy == BindingStrategy.TARGET_ADAPTOR)
			inputTarget = getInputTargetForTargetAdapter();
		else if (bindingStrategy != null || !isChildContextAbstract())
			inputTarget = effectiveDataType;
		else
			inputTarget = null;

		UnbindingStrategy unbindingStrategy = overrideMerge
				.getOverride(BindingNode::getUnbindingStrategy, this.unbindingStrategy).get();
		TypeToken<?> outputSource;
		if (effectiveUnbindingType != null)
			outputSource = effectiveUnbindingType;
		else if (unbindingStrategy != null || !isChildContextAbstract())
			outputSource = effectiveDataType;
		else
			outputSource = null;

		return new SequentialChildrenConfigurator(new SchemaNodeConfigurationContext() {
			@Override
			public SchemaNode<?, ?> parentNodeProxy() {
				return getSchemaNodeProxy();
			}

			@Override
			public BoundSet boundSet() {
				return inferenceBounds;
			}

			@Override
			public DataLoader dataLoader() {
				return getDataLoader();
			}

			@Override
			public Imports imports() {
				return getImports();
			}

			@Override
			public boolean isAbstract() {
				return isChildContextAbstract();
			}

			@Override
			public boolean isInputExpected() {
				return bindingStrategy != BindingStrategy.SOURCE_ADAPTOR;
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
			public TypeToken<?> inputTargetType() {
				return inputTarget;
			}

			@Override
			public TypeToken<?> outputSourceType() {
				return outputSource;
			}

			@Override
			public List<? extends SchemaNode<?, ?>> overriddenNodes() {
				return getOverriddenNodes();
			}
		});
	}

	protected TypeToken<?> getInputTargetForTargetAdapter() {
		if (isChildContextAbstract())
			return null;
		else
			throw new UnsupportedOperationException(
					"Non-abstract base binding node cannot use binding strategy " + BindingStrategy.TARGET_ADAPTOR.toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public BindingNodeConfigurator<?, ?, ? extends T> dataType(String dataType) {
		return dataType((TypeToken<? extends T>) parseTypeWithSubstitutedBrackets(dataType, getImports()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> BindingNodeConfigurator<?, ?, V> dataType(TypeToken<? extends V> dataType) {
		assertConfigurable(this.dataType);

		this.dataType = (TypeToken<T>) dataType;

		return (BindingNodeConfigurator<?, ?, V>) this;
	}

	public TypeToken<T> getDataType() {
		return dataType;
	}

	protected abstract boolean isDataContext();

	@Override
	public S bindingType(String bindingType) {
		return bindingType(parseTypeWithSubstitutedBrackets(bindingType, getImports()));

	}

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
	public S unbindingType(String unbindingType) {
		return unbindingType(parseTypeWithSubstitutedBrackets(unbindingType, getImports()));
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
	public S unbindingFactoryType(String factoryType) {
		return unbindingFactoryType(TypeToken.fromString(factoryType, getImports()));
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
	public final S providedUnbindingMethodParameters(List<QualifiedName> parameterNames) {
		assertConfigurable(unbindingParameterNames);
		unbindingParameterNames = new ArrayList<>(parameterNames);

		return getThis();
	}

	public List<QualifiedName> getUnbindingParameterNames() {
		return unbindingParameterNames;
	}

	@Override
	public S providedUnbindingMethodParameters(String... parameterNames) {
		return providedUnbindingMethodParameters(Arrays.asList(parameterNames).stream()
				.map(n -> new QualifiedName(n, getName().getNamespace())).collect(Collectors.toList()));
	}
}
