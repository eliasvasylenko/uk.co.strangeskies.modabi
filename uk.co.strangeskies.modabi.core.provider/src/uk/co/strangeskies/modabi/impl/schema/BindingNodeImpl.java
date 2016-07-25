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
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.Methods;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.processing.InputBindingStrategy;
import uk.co.strangeskies.modabi.processing.OutputBindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeException;
import uk.co.strangeskies.reflection.TypeToken;

abstract class BindingNodeImpl<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		extends SchemaNodeImpl<S, E> implements BindingNode<T, S, E> {
	protected static abstract class Effective<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
			extends SchemaNodeImpl.Effective<S, E> implements BindingNode.Effective<T, S, E> {
		protected TypeToken<T> dataType;
		private final TypeToken<?> bindingType;
		private final TypeToken<?> unbindingType;
		private final TypeToken<?> unbindingFactoryType;

		private final InputBindingStrategy bindingStrategy;
		private final OutputBindingStrategy unbindingStrategy;
		private String unbindingMethodName;
		private final Boolean unbindingMethodUnchecked;
		private final Invokable<?, ?> unbindingMethod;

		private final List<QualifiedName> providedUnbindingParameterNames;
		private final List<DataNode.Effective<?>> providedUnbindingParameters;

		protected Effective(OverrideMerge<S, ? extends BindingNodeConfiguratorImpl<?, S, T>> overrideMerge) {
			super(overrideMerge);

			BoundSet bounds = overrideMerge.configurator().getInferenceBounds();

			bindingStrategy = overrideMerge.getOverride(BindingNode::bindingStrategy).orDefault(InputBindingStrategy.PROVIDED)
					.get();

			unbindingStrategy = overrideMerge.getOverride(BindingNode::unbindingStrategy).orDefault(OutputBindingStrategy.SIMPLE)
					.get();

			providedUnbindingParameterNames = overrideMerge.getOverride(BindingNode::providedUnbindingMethodParameterNames)
					.orDefault(Collections.<QualifiedName>emptyList()).get();

			/*
			 * TODO refactor to make this final.
			 */
			unbindingMethodName = overrideMerge.getOverride(BindingNode::unbindingMethodName).tryGet();

			providedUnbindingParameters = abstractness().isAtLeast(Abstractness.ABSTRACT) ? null
					: findProvidedUnbindingParameters(this);

			unbindingMethodUnchecked = overrideMerge.getOverride(BindingNode::unbindingMethodUnchecked).tryGet();

			TypeToken<T> dataType = overrideMerge.configurator().getEffectiveDataType();
			if (abstractness().isAtMost(Abstractness.CONCRETE)) {
				try {
					dataType = dataType == null ? null
							: dataType.withLooseCompatibilityFrom(
									overrideMerge.configurator().getChildrenConfigurator().getPostInputType());
				} catch (Exception e) {
					/*
					 * 
					 * 
					 * 
					 * 
					 * 
					 * 
					 * TODO THIS IS A TEMPORARY MEASURE!!!!! NEEDS FIXING PROPERLY!!!!
					 * 
					 * 
					 * 
					 * 
					 * 
					 * 
					 * 
					 */
				}
			}
			this.dataType = inferDataType(dataType, bounds);
			bindingType = inferDataType(overrideMerge.configurator().getEffectiveBindingType(), bounds);
			unbindingType = inferDataType(overrideMerge.configurator().getEffectiveUnbindingType(), bounds);
			unbindingFactoryType = inferDataType(overrideMerge.configurator().getEffectiveUnbindingFactoryType(), bounds);

			unbindingMethod = abstractness().isAtLeast(Abstractness.ABSTRACT) ? null : findUnbindingMethod(overrideMerge);

			if (unbindingMethodName == null && abstractness().isLessThan(Abstractness.ABSTRACT)
					&& unbindingStrategy != OutputBindingStrategy.SIMPLE && unbindingStrategy != OutputBindingStrategy.CONSTRUCTOR)
				unbindingMethodName = unbindingMethod.getExecutable().getName();
		}

		protected <U> TypeToken<U> inferDataType(TypeToken<U> exactDataType, BoundSet bounds) {
			/*
			 * Incorporate bounds derived from child nodes through their input and
			 * output methods.
			 */
			if (exactDataType != null && !exactDataType.isProper()) {
				exactDataType = exactDataType.withBounds(bounds).resolve();

				if (abstractness().isLessThan(Abstractness.UNINFERRED)
						&& !((BindingNodeImpl<?, ?, ?>) source()).isExplicitlyExtensible()) {
					try {
						exactDataType = exactDataType.infer();
					} catch (TypeException e) {
						TypeToken<?> exactDataTypeFinal = exactDataType;
						throw new ModabiException(t -> t.cannotInferDataType(this, exactDataTypeFinal), e);
					}
				}
			}

			return exactDataType;
		}

		@Override
		public TypeToken<T> dataType() {
			return dataType;
		}

		@Override
		public InputBindingStrategy bindingStrategy() {
			return bindingStrategy;
		}

		@Override
		public TypeToken<?> bindingType() {
			return bindingType;
		}

		@Override
		public OutputBindingStrategy unbindingStrategy() {
			return unbindingStrategy;
		}

		@Override
		public TypeToken<?> unbindingType() {
			return unbindingType;
		}

		@Override
		public TypeToken<?> unbindingFactoryType() {
			return unbindingFactoryType;
		}

		@Override
		public Invokable<?, ?> unbindingMethod() {
			return unbindingMethod;
		}

		@Override
		public Boolean unbindingMethodUnchecked() {
			return unbindingMethodUnchecked;
		}

		@Override
		public String unbindingMethodName() {
			return unbindingMethodName;
		}

		@Override
		public List<DataNode.Effective<?>> providedUnbindingMethodParameters() {
			return providedUnbindingParameters;
		}

		@Override
		public List<QualifiedName> providedUnbindingMethodParameterNames() {
			return providedUnbindingParameterNames;
		}

		private Invokable<?, ?> findUnbindingMethod(
				OverrideMerge<S, ? extends BindingNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
			OutputBindingStrategy unbindingStrategy = unbindingStrategy();
			if (unbindingStrategy == null)
				unbindingStrategy = OutputBindingStrategy.SIMPLE;

			switch (unbindingStrategy) {
			case SIMPLE:
			case CONSTRUCTOR:
				return null;

			case STATIC_FACTORY:
			case PROVIDED_FACTORY:
				TypeToken<?> receiverClass = unbindingFactoryType() != null ? unbindingFactoryType() : unbindingType();
				return findUnbindingMethod(unbindingType(), receiverClass,
						findUnbindingMethodParameterClasses(BindingNodeImpl.Effective::dataType), overrideMerge);

			case PASS_TO_PROVIDED:
				return findUnbindingMethod(null, unbindingType(),
						findUnbindingMethodParameterClasses(BindingNodeImpl.Effective::dataType), overrideMerge);

			case ACCEPT_PROVIDED:
				return findUnbindingMethod(null, dataType(), findUnbindingMethodParameterClasses(t -> t.unbindingType()),
						overrideMerge);
			}
			throw new AssertionError();
		}

		private static List<DataNode.Effective<?>> findProvidedUnbindingParameters(BindingNode< ?, ?> node) {
			return node.providedUnbindingMethodParameterNames() == null
					? node.unbindingMethodName() == null ? null : new ArrayList<>()
					: node.providedUnbindingMethodParameterNames().stream().map(p -> {
						if (p.getName().equals("this"))
							return null;
						else {
							ChildNode< ?> effective = node.children().stream().filter(c -> c.name().equals(p)).findAny()
									.orElseThrow(() -> new ModabiException(t -> t.cannotFindUnbindingParameter(p)));

							if (!(effective instanceof BindingChildNode.Effective))
								throw new ModabiException(t -> t.unbindingParameterMustBeDataNode(effective, p));

							DataNode.Effective<?> dataNode = (DataNode.Effective<?>) effective;

							if (dataNode.occurrences() != null
									&& (dataNode.occurrences().getTo() != 1 || dataNode.occurrences().getFrom() != 1))
								throw new ModabiException(t -> t.unbindingParameterMustOccurOnce(effective, p));

							if (node.abstractness().isAtMost(Abstractness.ABSTRACT) && !dataNode.isValueProvided())
								throw new ModabiException(t -> t.unbindingParameterMustProvideValue(effective, p));

							return dataNode;
						}
					}).collect(Collectors.toList());
		}

		private List<TypeToken<?>> findUnbindingMethodParameterClasses(
				Function<BindingNodeImpl< ?, ?>, TypeToken<?>> nodeClass) {
			List<TypeToken<?>> classList = new ArrayList<>();

			boolean addedNodeClass = false;
			List<DataNode.Effective<?>> parameters = providedUnbindingMethodParameters();
			if (parameters != null) {
				for (DataNode.Effective<?> parameter : parameters) {
					if (parameter == null) {
						addedNodeClass = true;
						classList.add(nodeClass.apply(this));
					} else {
						classList.add(parameter.dataType());
					}
				}
			}
			if (!addedNodeClass)
				classList.add(0, nodeClass.apply(this));

			if (unbindingMethodUnchecked() != null && unbindingMethodUnchecked())
				classList = classList.stream().map(t -> t == null ? null : (TypeToken<?>) TypeToken.over(t.getRawType()))
						.collect(Collectors.toList());

			return classList;
		}

		@SuppressWarnings("unchecked")
		private <U> Invokable<?, ?> findUnbindingMethod(TypeToken<?> result, TypeToken<U> receiver,
				List<TypeToken<?>> parameters, OverrideMerge<S, ? extends BindingNodeConfiguratorImpl<?, S, ?>> overrideMerge) {
			Invokable<?, ?> overridden = overrideMerge.getOverride(b -> {
				if (b.effective() != null)
					return b.effective().unbindingMethod();
				else
					return null;
			}).tryGet();

			if (overridden != null) {
				Invokable<U, ?> invokable = (Invokable<U, ?>) overridden.withLooseApplicability(parameters);
				if (receiver != null)
					invokable = invokable.withReceiverType(receiver);
				if (result != null)
					invokable = invokable.withTargetType(result);

				return overridden;
			} else {
				if (unbindingMethodUnchecked() != null && unbindingMethodUnchecked()) {
					if (result != null)
						result = TypeToken.over(result.getRawType());
					if (receiver != null)
						receiver = (TypeToken<U>) TypeToken.over(receiver.getRawType());
					parameters = parameters.stream().map(t -> t == null ? null : (TypeToken<?>) TypeToken.over(t.getRawType()))
							.collect(Collectors.toList());
				}

				List<String> names = generateUnbindingMethodNames(result);
				return Methods.findMethod(names, receiver, bindingStrategy() == InputBindingStrategy.STATIC_FACTORY, result, false,
						parameters);
			}
		}

		private List<String> generateUnbindingMethodNames(TypeToken<?> resultClass) {
			List<String> names;

			if (unbindingMethodName() != null)
				names = Arrays.asList(unbindingMethodName());
			else
				names = generateUnbindingMethodNames(name().getName(), false, resultClass.getRawType());

			return names;
		}

		protected static List<String> generateUnbindingMethodNames(String propertyName, boolean isIterable,
				Class<?> resultClass) {
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
			if (resultClass != null && (resultClass.equals(Boolean.class) || resultClass.equals(boolean.class)))
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
	}

	private final TypeToken<T> dataType;
	private final TypeToken<?> bindingClass;
	private final TypeToken<?> unbindingClass;
	private final TypeToken<?> unbindingFactoryType;
	private final InputBindingStrategy bindingStrategy;
	private final OutputBindingStrategy unbindingStrategy;
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
				: Collections.unmodifiableList(new ArrayList<>(configurator.getUnbindingParameterNames()));
	}

	protected Boolean isExplicitlyExtensible() {
		return false;
	}

	@Override
	public TypeToken<T> dataType() {
		return dataType;
	}

	@Override
	public InputBindingStrategy bindingStrategy() {
		return bindingStrategy;
	}

	@Override
	public OutputBindingStrategy unbindingStrategy() {
		return unbindingStrategy;
	}

	@Override
	public TypeToken<?> bindingType() {
		return bindingClass;
	}

	@Override
	public TypeToken<?> unbindingType() {
		return unbindingClass;
	}

	@Override
	public TypeToken<?> unbindingFactoryType() {
		return unbindingFactoryType;
	}

	@Override
	public String unbindingMethodName() {
		return unbindingMethodName;
	}

	@Override
	public Boolean unbindingMethodUnchecked() {
		return unbindingMethodUnchecked;
	}

	@Override
	public List<QualifiedName> providedUnbindingMethodParameterNames() {
		return unbindingParameterNames;
	}
}
