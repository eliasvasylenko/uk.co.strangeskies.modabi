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
package uk.co.strangeskies.modabi.impl.schema.old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.declarative.InputBindingStrategy;
import uk.co.strangeskies.modabi.declarative.OutputBindingStrategy;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildrenConfiguratorImpl;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

public abstract class BindingNodeConfiguratorImpl<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T, N>, T>
		extends SchemaNodeConfiguratorImpl<S, N> implements ChildBindingPointConfigurator<S, N, T> {
	private TypeToken<T> dataType;
	private String dataTypeString;
	private TypeToken<T> effectiveDataType;
	private BoundSet inferenceBounds = new BoundSet();

	private InputBindingStrategy bindingStrategy;
	private TypeToken<?> inputBindingType;
	private String inputBindingTypeString;
	private TypeToken<?> effectiveBindingType;

	private OutputBindingStrategy unbindingStrategy;
	private TypeToken<?> outputBindingType;
	private String outputBindingTypeString;
	private TypeToken<?> effectiveUnbindingType;
	private String unbindingMethod;
	private Boolean unbindingMethodUnchecked;

	private TypeToken<?> outputBindingFactoryType;
	private String outputBindingFactoryTypeString;
	private TypeToken<?> effectiveUnbindingFactoryType;

	private List<QualifiedName> unbindingParameterNames;

	public BindingNodeConfiguratorImpl() {}

	public BindingNodeConfiguratorImpl(BindingNodeConfiguratorImpl<S, N, T> copy) {
		super(copy);

		this.dataType = copy.dataType;
		this.effectiveDataType = copy.effectiveDataType;
		this.inferenceBounds = copy.inferenceBounds;

		this.bindingStrategy = copy.bindingStrategy;
		this.inputBindingType = copy.inputBindingType;
		this.effectiveBindingType = copy.effectiveBindingType;

		this.unbindingStrategy = copy.unbindingStrategy;
		this.outputBindingType = copy.outputBindingType;
		this.effectiveUnbindingType = copy.effectiveUnbindingType;
		this.unbindingMethod = copy.unbindingMethod;
		this.unbindingMethodUnchecked = copy.unbindingMethodUnchecked;

		this.outputBindingFactoryType = copy.outputBindingFactoryType;
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

	private static <T> TypeToken<T> mergeOverriddenTypes(TypeToken<T> override, TypeToken<?> base) {
		if (!base.isProper() || !Types.isAssignable(override.getType(), base.getType())) {
			return override.withUpperBound(base.deepCopy());
		} else {
			return override;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ChildrenConfigurator createChildrenConfigurator() {
		/*
		 * Get declared data types, or overridden types thereof.
		 */
		effectiveDataType = (TypeToken<T>) getOverrideWithBase(BindingNode::dataType, BindingNodeConfigurator::getDataType)
				.orMerged((o, n) -> o.withEquality(n)).mergeOverride((o, b) -> mergeOverriddenTypes(o, b)).tryGet();

		effectiveBindingType = getOverrideWithBase(BindingNode::inputBindingType,
				BindingNodeConfigurator::getInputBindingType).orMerged((o, n) -> o.withEquality(n))
						.mergeOverride((o, b) -> mergeOverriddenTypes(o, b)).tryGet();

		effectiveUnbindingType = getOverrideWithBase(BindingNode::outputBindingType,
				BindingNodeConfigurator::getOutputBindingType).orMerged((o, n) -> o.withEquality(n))
						.mergeOverride((o, b) -> mergeOverriddenTypes(o, b)).tryGet();

		effectiveUnbindingFactoryType = getOverrideWithBase(BindingNode::outputBindingFactoryType,
				BindingNodeConfigurator::getOutputBindingFactoryType).orMerged((o, n) -> o.withEquality(n))
						.mergeOverride((o, b) -> mergeOverriddenTypes(o, b)).tryGet();

		/*
		 * Incorporate bounds from inherited types.
		 */
		if (effectiveDataType != null) {
			effectiveDataType = effectiveDataType.deepCopy();
			effectiveDataType.incorporateInto(inferenceBounds);
		}

		if (effectiveBindingType != null) {
			effectiveBindingType = effectiveBindingType.deepCopy();
			effectiveBindingType.incorporateInto(inferenceBounds);
		}

		if (effectiveUnbindingType != null) {
			effectiveUnbindingType = effectiveUnbindingType.deepCopy();
			effectiveUnbindingType.incorporateInto(inferenceBounds);
		}

		if (effectiveUnbindingFactoryType != null) {
			effectiveUnbindingFactoryType = effectiveUnbindingFactoryType.deepCopy();
			effectiveUnbindingFactoryType.incorporateInto(inferenceBounds);
		}

		/*
		 * Effective binding and unbinding types.
		 */

		InputBindingStrategy bindingStrategy = getOverrideWithBase(BindingNode::inputBindingStrategy,
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

		OutputBindingStrategy unbindingStrategy = getOverrideWithBase(BindingNode::outputBindingStrategy,
				BindingNodeConfigurator::getOutputBindingStrategy).orDefault(OutputBindingStrategy.SIMPLE).get();
		TypeToken<?> outputSource;
		if (effectiveUnbindingType != null)
			outputSource = effectiveUnbindingType;
		else if (unbindingStrategy != null || !isChildContextAbstract())
			outputSource = effectiveDataType;
		else
			outputSource = null;

		return new ChildrenConfiguratorImpl(new SchemaNodeConfigurationContext() {
			@Override
			public void addChildConfigurator(ChildNodeConfigurator<?, ?> configurator) {
				BindingNodeConfiguratorImpl.this.addChildConfigurator(configurator);
			}

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
			public List<? extends SchemaNode<?>> overriddenAndBaseNodes() {
				return getOverriddenAndBaseNodes();
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

	@Override
	public String getDataTypeString() {
		return dataTypeString;
	}

	protected abstract boolean isDataContext();

	@Override
	public S inputBindingType(String inputBindingType) {
		this.inputBindingType = parseTypeWithSubstitutedBrackets(inputBindingType, getImports());
		inputBindingTypeString = inputBindingType;

		return getThis();
	}

	@Override
	public final S inputBindingType(TypeToken<?> inputBindingType) {
		this.inputBindingType = inputBindingType;
		inputBindingTypeString = inputBindingType.toString();

		return getThis();
	}

	@Override
	public TypeToken<?> getInputBindingType() {
		return inputBindingType;
	}

	@Override
	public String getInputBindingTypeString() {
		return inputBindingTypeString;
	}

	@Override
	public S outputBindingType(String outputBindingType) {
		this.outputBindingType = parseTypeWithSubstitutedBrackets(outputBindingType, getImports());
		outputBindingTypeString = outputBindingType;

		return getThis();
	}

	@Override
	public S outputBindingType(TypeToken<?> outputBindingType) {
		this.outputBindingType = outputBindingType;
		outputBindingTypeString = outputBindingType.toString();

		return getThis();
	}

	@Override
	public TypeToken<?> getOutputBindingType() {
		return outputBindingType;
	}

	@Override
	public String getOutputBindingTypeString() {
		return outputBindingTypeString;
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
		outputBindingFactoryType = parseTypeWithSubstitutedBrackets(factoryType, getImports());
		outputBindingFactoryTypeString = factoryType;

		return getThis();
	}

	@Override
	public S outputBindingFactoryType(TypeToken<?> factoryType) {
		outputBindingFactoryType = factoryType;
		outputBindingFactoryTypeString = factoryType.toString();

		return getThis();
	}

	@Override
	public TypeToken<?> getOutputBindingFactoryType() {
		return outputBindingFactoryType;
	}

	@Override
	public String getOutputBindingFactoryTypeString() {
		return outputBindingFactoryTypeString;
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

	protected <U> OverrideBuilder<U, ?, ?> getOverrideWithBase(Function<BindingNode<? super T, ?>, U> valueFunction,
			Function<BindingNodeConfigurator<?, ?, ? super T>, U> givenValueFunction) {
		return new OverrideBuilder<U, BindingNodeConfiguratorImpl<? extends S, ? extends BindingNode<? super T, ?>, ? super T>, BindingNode<? super T, ?>>(
				this, getResult(), BindingNodeConfiguratorImpl::getOverriddenAndBaseNodes, valueFunction, givenValueFunction);
	}

	@Override
	protected abstract List<? extends BindingNode<? super T, ?>> getOverriddenAndBaseNodes();
}
