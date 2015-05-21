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
package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.Methods;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SequentialChildrenConfigurator;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.Resolver;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Wildcards;
import uk.co.strangeskies.reflection.Types;

public abstract class BindingNodeConfiguratorImpl<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T, N, ?>, T>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		BindingNodeConfigurator<S, N, T> {
	protected static abstract class BindingNodeImpl<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
			extends SchemaNodeImpl<S, E> implements BindingNode<T, S, E> {
		protected static abstract class Effective<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
				extends SchemaNodeImpl.Effective<S, E> implements
				BindingNode.Effective<T, S, E> {
			private final TypeToken<T> dataType;
			private final TypeToken<T> exactDataType;
			private final Type bindingClass;
			private final Type unbindingClass;
			private final Type unbindingFactoryClass;
			private final BindingStrategy bindingStrategy;
			private final UnbindingStrategy unbindingStrategy;
			private String unbindingMethodName;
			private final Boolean unbindingMethodUnchecked;
			private final Method unbindingMethod;

			private final List<QualifiedName> providedUnbindingParameterNames;
			private final List<DataNode.Effective<?>> providedUnbindingParameters;

			protected Effective(
					OverrideMerge<S, ? extends BindingNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
				super(overrideMerge);

				dataType = overrideMerge.getValue(BindingNode::getDataType,
						TypeToken::isAssignableTo, null);

				exactDataType = inferDataType(overrideMerge);

				bindingClass = overrideMerge.getValue(BindingNode::getBindingType, (v,
						o) -> TypeToken.over(o).isAssignableFrom(v),
						exactDataType == null ? null : exactDataType.getType());

				unbindingClass = overrideMerge.getValue(BindingNode::getUnbindingType,
						(v, o) -> TypeToken.over(o).isAssignableFrom(v),
						exactDataType == null ? null : exactDataType.getType());

				unbindingFactoryClass = overrideMerge.getValue(
						BindingNode::getUnbindingFactoryType, (v, o) -> TypeToken.over(o)
								.isAssignableFrom(v), unbindingClass);

				bindingStrategy = overrideMerge.getValue(
						BindingNode::getBindingStrategy, BindingStrategy.PROVIDED);

				unbindingStrategy = overrideMerge.getValue(
						BindingNode::getUnbindingStrategy, UnbindingStrategy.SIMPLE);

				providedUnbindingParameterNames = overrideMerge.getValue(
						BindingNode::getProvidedUnbindingMethodParameterNames,
						Collections.emptyList());

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

			@SuppressWarnings("unchecked")
			private TypeToken<T> inferDataType(
					OverrideMerge<S, ? extends BindingNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
				TypeToken<T> exactDataType;

				/*
				 * Data type which may include inference variables.
				 */
				exactDataType = (TypeToken<T>) overrideMerge.configurator()
						.getInferenceDataType();

				/*
				 * Incorporate bounds derived from child nodes through their input and
				 * output methods.
				 */
				if (exactDataType != null) {
					Resolver resolver = new Resolver(overrideMerge.configurator()
							.getInferenceBounds());

					if (!exactDataType.isProper())
						exactDataType = (TypeToken<T>) exactDataType.withBounds(
								resolver.getBounds()).resolve();
				}

				return exactDataType;
			}

			protected TypeToken<T> getExactDataType() {
				return exactDataType;
			}

			@Override
			public TypeToken<T> inferExactDataType() {
				return exactDataType == null ? null : exactDataType.deepCopy();
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
			public Type getBindingType() {
				return bindingClass;
			}

			@Override
			public Type getUnbindingType() {
				return unbindingClass;
			}

			@Override
			public Type getUnbindingFactoryType() {
				return unbindingFactoryClass;
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
					Type receiverClass = getUnbindingFactoryType() != null ? getUnbindingFactoryType()
							: getUnbindingType();
					return findUnbindingMethod(
							TypeToken.over(getUnbindingType()),
							TypeToken.over(receiverClass),
							findUnbindingMethodParameterClasses(BindingNodeImpl.Effective::getExactDataType),
							overrideMerge);

				case PASS_TO_PROVIDED:
					return findUnbindingMethod(
							null,
							TypeToken.over(getUnbindingType()),
							findUnbindingMethodParameterClasses(BindingNodeImpl.Effective::getExactDataType),
							overrideMerge);

				case ACCEPT_PROVIDED:
					return findUnbindingMethod(null, getExactDataType(),
							findUnbindingMethodParameterClasses(t -> TypeToken.over(t
									.getUnbindingType())), overrideMerge);
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
													throw new SchemaException(
															"Unbinding parameter node '" + effective
																	+ "' for '" + p + "' is not a data node.");

												DataNode.Effective<?> dataNode = (DataNode.Effective<?>) effective;

												if (dataNode.occurrences() != null
														&& (dataNode.occurrences().getTo() != 1 || dataNode
																.occurrences().getFrom() != 1))
													throw new SchemaException(
															"Unbinding parameter node '" + effective
																	+ "' for '" + p
																	+ "' must occur exactly once.");

												if (!node.isAbstract() && !dataNode.isValueProvided())
													throw new SchemaException(
															"Unbinding parameter node '" + dataNode
																	+ "' for '" + p + "' must provide a value.");

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

				if (isUnbindingMethodUnchecked() != null
						&& isUnbindingMethodUnchecked())
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
					Invokable<U, ?> invokable = (Invokable<U, ?>) Invokable.over(
							overridden).withLooseApplicability(parameters);
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
								+ names + "'.", e);
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
					names.add("is"
							+ InputNodeConfigurationHelper.capitalize(propertyName));

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
		}

		private final TypeToken<T> dataClass;
		private final Type bindingClass;
		private final Type unbindingClass;
		private final Type unbindingFactoryClass;
		private final BindingStrategy bindingStrategy;
		private final UnbindingStrategy unbindingStrategy;
		private final String unbindingMethodName;
		private final Boolean unbindingMethodUnchecked;

		private final List<QualifiedName> unbindingParameterNames;

		public BindingNodeImpl(BindingNodeConfiguratorImpl<?, ?, T> configurator) {
			super(configurator);

			dataClass = configurator.dataType;

			bindingStrategy = configurator.bindingStrategy;
			bindingClass = configurator.bindingClass;

			unbindingStrategy = configurator.unbindingStrategy;
			unbindingClass = configurator.unbindingClass;
			unbindingMethodName = configurator.unbindingMethod;
			unbindingMethodUnchecked = configurator.unbindingMethodUnchecked;
			unbindingFactoryClass = configurator.unbindingFactoryClass;
			unbindingParameterNames = configurator.unbindingParameterNames == null ? null
					: Collections.unmodifiableList(new ArrayList<>(
							configurator.unbindingParameterNames));
		}

		@Override
		public TypeToken<T> getDataType() {
			return dataClass;
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
		public Type getBindingType() {
			return bindingClass;
		}

		@Override
		public Type getUnbindingType() {
			return unbindingClass;
		}

		@Override
		public Type getUnbindingFactoryType() {
			return unbindingFactoryClass;
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

	private TypeToken<T> dataType;
	private TypeToken<T> inferenceDataType;
	private BoundSet inferenceBounds = new BoundSet();

	private BindingStrategy bindingStrategy;
	private Type bindingClass;

	private UnbindingStrategy unbindingStrategy;
	private Type unbindingClass;
	private String unbindingMethod;
	private Boolean unbindingMethodUnchecked;

	private Type unbindingFactoryClass;

	private List<QualifiedName> unbindingParameterNames;

	protected final BoundSet getInferenceBounds() {
		return inferenceBounds;
	}

	public TypeToken<T> getInferenceDataType() {
		return inferenceDataType;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ChildrenConfigurator createChildrenConfigurator() {
		OverrideMerge<? extends BindingNode<?, ?, ?>, ? extends BindingNodeConfigurator<?, ?, ?>> overrideMerge = overrideMerge(
				null, this);

		Type unbindingClass = overrideMerge
				.getValueWithOverride(this.unbindingClass,
						BindingNode::getUnbindingType, Types::isAssignable);
		Type bindingClass = overrideMerge.getValueWithOverride(this.bindingClass,
				BindingNode::getBindingType, Types::isAssignable);

		/*
		 * Get given data type and substitute inference variables for wildcards.
		 */
		TypeToken<?> dataType = overrideMerge.getValueWithOverride(this.dataType,
				BindingNode::getDataType, TypeToken::isAssignableTo);
		if (dataType != null) {
			dataType = TypeToken.over(dataType.getType(), Wildcards.INFERENCE);

			/*
			 * Incorporate bounds from each inherited type.
			 */
			for (TypeToken<?> overriddenType : overrideMerge
					.getOverridenValues(n -> n.effective().inferExactDataType())) {
				dataType = dataType.withUpperBound(overriddenType);
			}
		}

		TypeToken<?> inputTarget = bindingClass != null ? TypeToken.over(
				bindingClass, Wildcards.INFERENCE) : dataType;
		TypeToken<?> outputTarget = unbindingClass != null ? TypeToken.over(
				unbindingClass, Wildcards.INFERENCE) : dataType;

		if (dataType != null)
			inferenceBounds.incorporate(dataType.getResolver().getBounds());
		if (inputTarget != null && inputTarget != dataType)
			inferenceBounds.incorporate(inputTarget.getResolver().getBounds());
		if (outputTarget != null && outputTarget != dataType)
			inferenceBounds.incorporate(outputTarget.getResolver().getBounds());

		inferenceDataType = (TypeToken<T>) dataType;

		/*
		 * TODO make 'hasInput' optional for IMPLEMENT_IN_PLACE
		 */
		return new SequentialChildrenConfigurator(
				new SchemaNodeConfigurationContext<ChildNode<?, ?>>() {
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
						return outputTarget;
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
			TypeToken<V> dataType) {
		assertConfigurable(this.dataType);

		if (this.dataType != null && !this.dataType.isAssignableFrom(dataType))
			throw new IllegalArgumentException();

		this.dataType = (TypeToken<T>) dataType;

		return (BindingNodeConfigurator<?, ?, V>) this;
	}

	protected abstract boolean isDataContext();

	@Override
	public final S bindingType(Type bindingClass) {
		assertConfigurable(this.bindingClass);
		this.bindingClass = bindingClass;

		return getThis();
	}

	@Override
	public S unbindingType(Type unbindingClass) {
		assertConfigurable(this.unbindingClass);
		this.unbindingClass = unbindingClass;

		return getThis();
	}

	@Override
	public S unbindingMethod(String unbindingMethod) {
		assertConfigurable(this.unbindingMethod);
		this.unbindingMethod = unbindingMethod;

		return getThis();
	}

	@Override
	public S unbindingMethodUnchecked(boolean unchecked) {
		assertConfigurable(this.unbindingMethodUnchecked);
		this.unbindingMethodUnchecked = unchecked;

		return getThis();
	}

	@Override
	public final S bindingStrategy(BindingStrategy strategy) {
		assertConfigurable(bindingStrategy);
		bindingStrategy = strategy;

		return getThis();
	}

	@Override
	public final S unbindingStrategy(UnbindingStrategy strategy) {
		assertConfigurable(unbindingStrategy);
		unbindingStrategy = strategy;

		return getThis();
	}

	@Override
	public S unbindingFactoryType(Type factoryClass) {
		assertConfigurable(unbindingFactoryClass);
		unbindingFactoryClass = factoryClass;

		return getThis();
	}

	@Override
	public final S providedUnbindingMethodParameters(
			List<QualifiedName> parameterNames) {
		assertConfigurable(unbindingParameterNames);
		unbindingParameterNames = new ArrayList<>(parameterNames);

		return getThis();
	}

	@Override
	public S providedUnbindingMethodParameters(String... parameterNames) {
		return providedUnbindingMethodParameters(Arrays.asList(parameterNames)
				.stream().map(n -> new QualifiedName(n, getName().getNamespace()))
				.collect(Collectors.toList()));
	}
}
