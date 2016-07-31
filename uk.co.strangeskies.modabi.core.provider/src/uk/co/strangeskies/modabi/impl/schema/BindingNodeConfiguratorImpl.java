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
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.impl.schema.utilities.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.processing.InputBindingStrategy;
import uk.co.strangeskies.modabi.processing.OutputBindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

public abstract class BindingNodeConfiguratorImpl<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T, N>, T>
		extends SchemaNodeConfiguratorImpl<S, N> implements BindingNodeConfigurator<S, N, T> {
	private TypeToken<T> dataType;
	private TypeToken<T> effectiveDataType;
	private BoundSet inferenceBounds = new BoundSet();

	private InputBindingStrategy bindingStrategy;
	private TypeToken<?> bindingType;
	private TypeToken<?> effectiveBindingType;

	private OutputBindingStrategy unbindingStrategy;
	private TypeToken<?> unbindingType;
	private TypeToken<?> effectiveUnbindingType;
	private String unbindingMethod;
	private Boolean unbindingMethodUnchecked;

	private TypeToken<?> unbindingFactoryType;
	private TypeToken<?> effectiveUnbindingFactoryType;

	private List<QualifiedName> unbindingParameterNames;

	public BindingNodeConfiguratorImpl() {}

	public BindingNodeConfiguratorImpl(BindingNodeConfiguratorImpl<S, N, T> copy) {
		super(copy);

		this.dataType = copy.dataType;
		this.effectiveDataType = copy.effectiveDataType;
		this.inferenceBounds = copy.inferenceBounds;

		this.bindingStrategy = copy.bindingStrategy;
		this.bindingType = copy.bindingType;
		this.effectiveBindingType = copy.effectiveBindingType;

		this.unbindingStrategy = copy.unbindingStrategy;
		this.unbindingType = copy.unbindingType;
		this.effectiveUnbindingType = copy.effectiveUnbindingType;
		this.unbindingMethod = copy.unbindingMethod;
		this.unbindingMethodUnchecked = copy.unbindingMethodUnchecked;

		this.unbindingFactoryType = copy.unbindingFactoryType;
		this.effectiveUnbindingFactoryType = copy.effectiveUnbindingFactoryType;

		this.unbindingParameterNames = copy.unbindingParameterNames;
	}

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
		/*
		 * Get declared data types, or overridden types thereof.
		 */
		effectiveDataType = getOverride(BindingNode::dataType, BindingNodeConfigurator::getDataType)
				.orMerged((o, n) -> o.withEquality(n)).validate((o, n) -> true).tryGet();
		effectiveBindingType = getOverride(BindingNode::inputBindingType, BindingNodeConfigurator::getInputBindingType)
				.orMerged((o, n) -> o.withEquality(n)).validate((o, n) -> true).tryGet();
		effectiveUnbindingType = getOverride(BindingNode::outputBindingType, BindingNodeConfigurator::getOutputBindingType)
				.orMerged((o, n) -> o.withEquality(n)).validate((o, n) -> true).tryGet();
		effectiveUnbindingFactoryType = getOverride(BindingNode::outputBindingFactoryType,
				BindingNodeConfigurator::getOutputBindingFactoryType).orMerged((o, n) -> o.withEquality(n))
						.validate((o, n) -> true).tryGet();

		/*
		 * Incorporate bounds from inherited types.
		 */
		if (effectiveDataType != null) {
			effectiveDataType = effectiveDataType.deepCopy();

			if (this.dataType != null) {
				for (TypeToken<?> overriddenType : getOverridenValues(BindingNode::dataType)) {
					/*
					 * only perform more complex type override behavior if not already
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
				for (TypeToken<?> overriddenType : getOverridenValues(BindingNode::inputBindingType)) {
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
				for (TypeToken<?> overriddenType : getOverridenValues(BindingNode::outputBindingType)) {
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
				for (TypeToken<?> overriddenType : getOverridenValues(BindingNode::outputBindingFactoryType)) {
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

		InputBindingStrategy bindingStrategy = getOverride(BindingNode::inputBindingStrategy,
				BindingNodeConfigurator::getInputBindingStrategy).orDefault(InputBindingStrategy.PROVIDED).get();
		TypeToken<?> inputTarget;
		if (effectiveBindingType != null)
			inputTarget = effectiveBindingType;
		else if (bindingStrategy == InputBindingStrategy.TARGET_ADAPTOR)
			inputTarget = getInputTargetForTargetAdapter();
		else if (bindingStrategy != null || !isChildContextAbstract())
			inputTarget = effectiveDataType;
		else
			inputTarget = null;

		OutputBindingStrategy unbindingStrategy = getOverride(BindingNode::outputBindingStrategy,
				BindingNodeConfigurator::getOutputBindingStrategy).orDefault(OutputBindingStrategy.SIMPLE).get();
		TypeToken<?> outputSource;
		if (effectiveUnbindingType != null)
			outputSource = effectiveUnbindingType;
		else if (unbindingStrategy != null || !isChildContextAbstract())
			outputSource = effectiveDataType;
		else
			outputSource = null;

		return new SequentialChildrenConfigurator(new SchemaNodeConfigurationContext() {
			@Override
			public SchemaNode<?> parent() {
				return getResult();
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
				return bindingStrategy != InputBindingStrategy.SOURCE_ADAPTOR;
			}

			@Override
			public boolean isInputDataOnly() {
				return isDataContext();
			}

			@Override
			public boolean isConstructorExpected() {
				return bindingStrategy == InputBindingStrategy.CONSTRUCTOR;
			}

			@Override
			public boolean isStaticMethodExpected() {
				return bindingStrategy == InputBindingStrategy.STATIC_FACTORY;
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
			public List<? extends SchemaNode<?>> overriddenNodes() {
				return getOverriddenNodes();
			}
		});
	}

	protected TypeToken<?> getInputTargetForTargetAdapter() {
		if (isChildContextAbstract())
			return null;
		else
			throw new UnsupportedOperationException("Non-abstract base binding node cannot use binding strategy "
					+ InputBindingStrategy.TARGET_ADAPTOR.toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public BindingNodeConfigurator<?, ?, ? extends T> dataType(String dataType) {
		return dataType((TypeToken<? extends T>) parseTypeWithSubstitutedBrackets(dataType, getImports()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> BindingNodeConfigurator<?, ?, V> dataType(TypeToken<? extends V> dataType) {
		this.dataType = (TypeToken<T>) dataType;

		return (BindingNodeConfigurator<?, ?, V>) this;
	}

	@Override
	public TypeToken<T> getDataType() {
		return dataType;
	}

	protected abstract boolean isDataContext();

	@Override
	public S inputBindingType(String bindingType) {
		return inputBindingType(parseTypeWithSubstitutedBrackets(bindingType, getImports()));
	}

	@Override
	public final S inputBindingType(TypeToken<?> bindingClass) {
		this.bindingType = bindingClass;

		return getThis();
	}

	@Override
	public TypeToken<?> getInputBindingType() {
		return bindingType;
	}

	@Override
	public S outputBindingType(String unbindingType) {
		return outputBindingType(parseTypeWithSubstitutedBrackets(unbindingType, getImports()));
	}

	@Override
	public S outputBindingType(TypeToken<?> unbindingClass) {
		this.unbindingType = unbindingClass;

		return getThis();
	}

	@Override
	public TypeToken<?> getOutputBindingType() {
		return unbindingType;
	}

	@Override
	public S outputBindingMethod(String unbindingMethod) {
		this.unbindingMethod = unbindingMethod;

		return getThis();
	}

	@Override
	public String getOutputBindingMethod() {
		return unbindingMethod;
	}

	@Override
	public S outputBindingMethodUnchecked(boolean unchecked) {
		this.unbindingMethodUnchecked = unchecked;

		return getThis();
	}

	@Override
	public Boolean getOutputBindingMethodUnchecked() {
		return unbindingMethodUnchecked;
	}

	@Override
	public final S inputBindingStrategy(InputBindingStrategy strategy) {
		bindingStrategy = strategy;

		return getThis();
	}

	@Override
	public InputBindingStrategy getInputBindingStrategy() {
		return bindingStrategy;
	}

	@Override
	public final S outputBindingStrategy(OutputBindingStrategy strategy) {
		unbindingStrategy = strategy;

		return getThis();
	}

	@Override
	public OutputBindingStrategy getOutputBindingStrategy() {
		return unbindingStrategy;
	}

	@Override
	public S outputBindingFactoryType(String factoryType) {
		return outputBindingFactoryType(TypeToken.fromString(factoryType, getImports()));
	}

	@Override
	public S outputBindingFactoryType(TypeToken<?> factoryClass) {
		unbindingFactoryType = factoryClass;

		return getThis();
	}

	@Override
	public TypeToken<?> getOutputBindingFactoryType() {
		return unbindingFactoryType;
	}

	@Override
	public final S providedOutputBindingMethodParameters(List<QualifiedName> parameterNames) {
		unbindingParameterNames = new ArrayList<>(parameterNames);

		return getThis();
	}

	@Override
	public List<QualifiedName> getProvidedOutputBindingMethodParameters() {
		return unbindingParameterNames;
	}

	@Override
	public S providedOutputBindingMethodParameters(String... parameterNames) {
		return providedOutputBindingMethodParameters(Arrays.asList(parameterNames).stream()
				.map(n -> new QualifiedName(n, getName().getNamespace())).collect(Collectors.toList()));
	}

	public Boolean getExtensible() {
		return false;
	}
}
