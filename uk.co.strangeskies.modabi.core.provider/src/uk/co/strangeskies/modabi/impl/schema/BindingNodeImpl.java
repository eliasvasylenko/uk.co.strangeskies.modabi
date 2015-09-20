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

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.utilities.Methods;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.PropertySet;

abstract class BindingNodeImpl<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		extends SchemaNodeImpl<S, E> implements BindingNode<T, S, E> {
	protected static abstract class Effective<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
			extends SchemaNodeImpl.Effective<S, E> implements
			BindingNode.Effective<T, S, E> {
		private final TypeToken<T> dataType;

		private final TypeToken<?> bindingType;

		private final TypeToken<?> unbindingType;

		private final TypeToken<?> unbindingFactoryType;

		private final BindingStrategy bindingStrategy;
		private final UnbindingStrategy unbindingStrategy;
		private String unbindingMethodName;
		private final Boolean unbindingMethodUnchecked;
		private final Method unbindingMethod;

		private final List<QualifiedName> providedUnbindingParameterNames;
		private final List<DataNode.Effective<?>> providedUnbindingParameters;

		@SuppressWarnings("unchecked")
		protected Effective(
				OverrideMerge<S, ? extends BindingNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
			super(overrideMerge);

			BoundSet bounds = overrideMerge.configurator().getInferenceBounds();

			dataType = inferDataType((TypeToken<T>) overrideMerge.configurator()
					.getEffectiveDataType(), bounds);

			bindingType = inferDataType(overrideMerge.configurator()
					.getEffectiveBindingType(), bounds);

			unbindingType = inferDataType(overrideMerge.configurator()
					.getEffectiveUnbindingType(), bounds);

			unbindingFactoryType = inferDataType(overrideMerge.configurator()
					.getEffectiveUnbindingFactoryType(), bounds);

			bindingStrategy = overrideMerge.getValue(BindingNode::getBindingStrategy,
					BindingStrategy.PROVIDED);

			unbindingStrategy = overrideMerge.getValue(
					BindingNode::getUnbindingStrategy, UnbindingStrategy.SIMPLE);

			providedUnbindingParameterNames = overrideMerge.getValue(
					BindingNode::getProvidedUnbindingMethodParameterNames,
					Collections.<QualifiedName> emptyList());

			unbindingMethodName = overrideMerge
					.tryGetValue(BindingNode::getUnbindingMethodName);

			providedUnbindingParameters = isAbstract() ? null
					: findProvidedUnbindingParameters(this);

			unbindingMethodUnchecked = overrideMerge
					.tryGetValue(BindingNode::isUnbindingMethodUnchecked);

			unbindingMethod = isAbstract() ? null
					: findUnbindingMethod(overrideMerge);

			if (unbindingMethodName == null && !isAbstract()
					&& unbindingStrategy != UnbindingStrategy.SIMPLE
					&& unbindingStrategy != UnbindingStrategy.CONSTRUCTOR)
				unbindingMethodName = unbindingMethod.getName();
		}

		private <U> TypeToken<U> inferDataType(TypeToken<U> exactDataType,
				BoundSet bounds) {
			/*
			 * Incorporate bounds derived from child nodes through their input and
			 * output methods.
			 */
			if (exactDataType != null && !exactDataType.isProper()) {
				exactDataType = exactDataType.withBounds(bounds).resolve();

				if (!isAbstract())
					exactDataType = exactDataType.infer();
			}

			return exactDataType;
		}

		@Override
		public TypeToken<T> getDataType() {
			return dataType;
		}

		@Override
		public BindingStrategy getBindingStrategy() {
			return bindingStrategy;
		}

		@Override
		public TypeToken<?> getBindingType() {
			return bindingType;
		}

		@Override
		public UnbindingStrategy getUnbindingStrategy() {
			return unbindingStrategy;
		}

		@Override
		public TypeToken<?> getUnbindingType() {
			return unbindingType;
		}

		@Override
		public TypeToken<?> getUnbindingFactoryType() {
			return unbindingFactoryType;
		}

		@Override
		public Method getUnbindingMethod() {
			return unbindingMethod;
		}

		@Override
		public Boolean isUnbindingMethodUnchecked() {
			return unbindingMethodUnchecked;
		}

		@Override
		public String getUnbindingMethodName() {
			return unbindingMethodName;
		}

		@Override
		public List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters() {
			return providedUnbindingParameters;
		}

		@Override
		public List<QualifiedName> getProvidedUnbindingMethodParameterNames() {
			return providedUnbindingParameterNames;
		}

		private Method findUnbindingMethod(
				OverrideMerge<S, ? extends BindingNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
			UnbindingStrategy unbindingStrategy = getUnbindingStrategy();
			if (unbindingStrategy == null)
				unbindingStrategy = UnbindingStrategy.SIMPLE;

			switch (unbindingStrategy) {
			case SIMPLE:
			case CONSTRUCTOR:
				return null;

			case STATIC_FACTORY:
			case PROVIDED_FACTORY:
				TypeToken<?> receiverClass = getUnbindingFactoryType() != null ? getUnbindingFactoryType()
						: getUnbindingType();
				return findUnbindingMethod(
						getUnbindingType(),
						receiverClass,
						findUnbindingMethodParameterClasses(BindingNodeImpl.Effective::getDataType),
						overrideMerge);

			case PASS_TO_PROVIDED:
				return findUnbindingMethod(
						null,
						getUnbindingType(),
						findUnbindingMethodParameterClasses(BindingNodeImpl.Effective::getDataType),
						overrideMerge);

			case ACCEPT_PROVIDED:
				return findUnbindingMethod(null, getDataType(),
						findUnbindingMethodParameterClasses(t -> t.getUnbindingType()),
						overrideMerge);
			}
			throw new AssertionError();
		}

		private static List<DataNode.Effective<?>> findProvidedUnbindingParameters(
				BindingNode.Effective<?, ?, ?> node) {
			return node.getProvidedUnbindingMethodParameterNames() == null ? node
					.getUnbindingMethodName() == null ? null : new ArrayList<>()
					: node
							.getProvidedUnbindingMethodParameterNames()
							.stream()
							.map(
									p -> {
										if (p.getName().equals("this"))
											return null;
										else {
											ChildNode.Effective<?, ?> effective = node
													.children()
													.stream()
													.filter(c -> c.getName().equals(p))
													.findAny()
													.orElseThrow(
															() -> new SchemaException(
																	"Cannot find node for unbinding parameter: '"
																			+ p + "'"));

											if (!(effective instanceof DataNode.Effective))
												throw new SchemaException("Unbinding parameter node '"
														+ effective + "' for '" + p
														+ "' is not a data node.");

											DataNode.Effective<?> dataNode = (DataNode.Effective<?>) effective;

											if (dataNode.occurrences() != null
													&& (dataNode.occurrences().getTo() != 1 || dataNode
															.occurrences().getFrom() != 1))
												throw new SchemaException("Unbinding parameter node '"
														+ effective + "' for '" + p
														+ "' must occur exactly once.");

											if (!node.isAbstract() && !dataNode.isValueProvided())
												throw new SchemaException("Unbinding parameter node '"
														+ dataNode + "' for '" + p
														+ "' must provide a value.");

											return dataNode;
										}
									}).collect(Collectors.toList());
		}

		private List<TypeToken<?>> findUnbindingMethodParameterClasses(
				Function<BindingNodeImpl.Effective<?, ?, ?>, TypeToken<?>> nodeClass) {
			List<TypeToken<?>> classList = new ArrayList<>();

			boolean addedNodeClass = false;
			List<DataNode.Effective<?>> parameters = getProvidedUnbindingMethodParameters();
			if (parameters != null) {
				for (DataNode.Effective<?> parameter : parameters) {
					if (parameter == null)
						if (addedNodeClass)
							throw new SchemaException();
						else {
							addedNodeClass = true;
							classList.add(nodeClass.apply(this));
						}
					else {
						classList.add(parameter.getDataType());
					}
				}
			}
			if (!addedNodeClass)
				classList.add(0, nodeClass.apply(this));

			if (isUnbindingMethodUnchecked() != null && isUnbindingMethodUnchecked())
				classList = classList
						.stream()
						.map(
								t -> t == null ? null : (TypeToken<?>) TypeToken.over(t
										.getRawType())).collect(Collectors.toList());

			return classList;
		}

		@SuppressWarnings("unchecked")
		private <U> Method findUnbindingMethod(
				TypeToken<?> result,
				TypeToken<U> receiver,
				List<TypeToken<?>> parameters,
				OverrideMerge<S, ? extends BindingNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
			Executable overridden = overrideMerge.tryGetValue(b -> {
				if (b.effective() != null)
					return b.effective().getUnbindingMethod();
				else
					return null;
			});

			if (overridden != null) {
				Invokable<U, ?> invokable = (Invokable<U, ?>) Invokable
						.over(overridden).withLooseApplicability(parameters);
				if (receiver != null)
					invokable = invokable.withReceiverType(receiver);
				if (result != null)
					invokable = invokable.withTargetType(result);

				return (Method) overridden;
			} else {
				if (isUnbindingMethodUnchecked() != null
						&& isUnbindingMethodUnchecked()) {
					if (result != null)
						result = TypeToken.over(result.getRawType());
					if (receiver != null)
						receiver = (TypeToken<U>) TypeToken.over(receiver.getRawType());
					parameters = parameters
							.stream()
							.map(
									t -> t == null ? null : (TypeToken<?>) TypeToken.over(t
											.getRawType())).collect(Collectors.toList());
				}

				List<String> names = generateUnbindingMethodNames(result);
				try {
					return (Method) Methods.findMethod(names, receiver,
							getBindingStrategy() == BindingStrategy.STATIC_FACTORY, result,
							false, parameters).getExecutable();
				} catch (NoSuchMethodException | SchemaException | SecurityException e) {
					throw new SchemaException("Cannot find unbinding method for node '"
							+ this + "' of class '" + result + "', reveiver '" + receiver
							+ "', and parameters '" + parameters + "' with any name of '"
							+ names + "'", e);
				}
			}
		}

		private List<String> generateUnbindingMethodNames(TypeToken<?> resultClass) {
			List<String> names;

			if (getUnbindingMethodName() != null)
				names = Arrays.asList(getUnbindingMethodName());
			else
				names = generateUnbindingMethodNames(getName().getName(), false,
						resultClass.getRawType());

			return names;
		}

		protected static List<String> generateUnbindingMethodNames(
				String propertyName, boolean isIterable, Class<?> resultClass) {
			List<String> names = new ArrayList<>();

			names.add(propertyName);
			names.add(propertyName + "Value");
			if (isIterable) {
				for (String name : new ArrayList<>(names)) {
					names.add(name + "s");
					names.add(name + "List");
					names.add(name + "Set");
					names.add(name + "Collection");
					names.add(name + "Array");
				}
			}
			if (resultClass != null
					&& (resultClass.equals(Boolean.class) || resultClass
							.equals(boolean.class)))
				names.add("is" + InputNodeConfigurationHelper.capitalize(propertyName));

			List<String> namesAndBlank = new ArrayList<>(names);
			namesAndBlank.add("");

			for (String name : namesAndBlank) {
				names.add("get" + InputNodeConfigurationHelper.capitalize(name));
				names.add("to" + InputNodeConfigurationHelper.capitalize(name));
				names.add("compose" + InputNodeConfigurationHelper.capitalize(name));
				names.add("create" + InputNodeConfigurationHelper.capitalize(name));
			}

			return names;
		}

		@SuppressWarnings("rawtypes")
		static final PropertySet<BindingNode.Effective> PROPERTY_SET = new PropertySet<>(
				BindingNode.Effective.class).add(BindingNodeImpl.PROPERTY_SET)
				.add(SchemaNodeImpl.Effective.PROPERTY_SET)
				.add(BindingNode.Effective::getUnbindingMethod)
				.add(BindingNode.Effective::getProvidedUnbindingMethodParameters);

		@Override
		protected PropertySet<? super E> effectivePropertySet() {
			return PROPERTY_SET;
		}
	}

	private final TypeToken<T> dataType;
	private final TypeToken<?> bindingClass;
	private final TypeToken<?> unbindingClass;
	private final TypeToken<?> unbindingFactoryType;
	private final BindingStrategy bindingStrategy;
	private final UnbindingStrategy unbindingStrategy;
	private final String unbindingMethodName;
	private final Boolean unbindingMethodUnchecked;

	private final List<QualifiedName> unbindingParameterNames;

	public BindingNodeImpl(BindingNodeConfiguratorImpl<?, ?, T> configurator) {
		super(configurator);

		dataType = configurator.getDataType();

		bindingStrategy = configurator.getBindingStrategy();
		bindingClass = configurator.getBindingType();

		unbindingStrategy = configurator.getUnbindingStrategy();
		unbindingClass = configurator.getUnbindingType();
		unbindingMethodName = configurator.getUnbindingMethod();
		unbindingMethodUnchecked = configurator.getUnbindingMethodUnchecked();
		unbindingFactoryType = configurator.getUnbindingFactoryType();
		unbindingParameterNames = configurator.getUnbindingParameterNames() == null ? null
				: Collections.unmodifiableList(new ArrayList<>(configurator
						.getUnbindingParameterNames()));
	}

	@SuppressWarnings("rawtypes")
	protected static final PropertySet<BindingNode> PROPERTY_SET = new PropertySet<>(
			BindingNode.class)
			.add(SchemaNodeImpl.PROPERTY_SET)
			.add(
					n -> Optional.ofNullable(n.getDataType())
							.map(TypeToken::getAnnotatedDeclaration).orElse(null))
			.add(BindingNode::isAbstract)
			.add(BindingNode::getBindingStrategy)
			.add(
					n -> Optional.ofNullable(n.getBindingType())
							.map(TypeToken::getAnnotatedDeclaration).orElse(null))
			.add(BindingNode::getUnbindingStrategy)
			.add(
					n -> Optional.ofNullable(n.getUnbindingType())
							.map(TypeToken::getAnnotatedDeclaration).orElse(null))
			.add(BindingNode::isUnbindingMethodUnchecked)
			.add(BindingNode::getUnbindingMethodName)
			.add(
					n -> Optional.ofNullable(n.getUnbindingFactoryType())
							.map(TypeToken::getAnnotatedDeclaration).orElse(null))
			.add(BindingNode::getProvidedUnbindingMethodParameterNames);

	@Override
	protected PropertySet<? super S> propertySet() {
		return PROPERTY_SET;
	}

	@Override
	public TypeToken<T> getDataType() {
		return dataType;
	}

	@Override
	public BindingStrategy getBindingStrategy() {
		return bindingStrategy;
	}

	@Override
	public UnbindingStrategy getUnbindingStrategy() {
		return unbindingStrategy;
	}

	@Override
	public TypeToken<?> getBindingType() {
		return bindingClass;
	}

	@Override
	public TypeToken<?> getUnbindingType() {
		return unbindingClass;
	}

	@Override
	public TypeToken<?> getUnbindingFactoryType() {
		return unbindingFactoryType;
	}

	@Override
	public String getUnbindingMethodName() {
		return unbindingMethodName;
	}

	@Override
	public Boolean isUnbindingMethodUnchecked() {
		return unbindingMethodUnchecked;
	}

	@Override
	public List<QualifiedName> getProvidedUnbindingMethodParameterNames() {
		return unbindingParameterNames;
	}
}
