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
import uk.co.strangeskies.modabi.processing.InputBindingStrategy;
import uk.co.strangeskies.modabi.processing.OutputBindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeException;
import uk.co.strangeskies.reflection.TypeToken;

abstract class BindingNodeImpl<T, S extends BindingNode<T, S>> extends SchemaNodeImpl<S> implements BindingNode<T, S> {
	protected TypeToken<T> dataType;
	private final TypeToken<?> bindingType;
	private final TypeToken<?> unbindingType;
	private final TypeToken<?> unbindingFactoryType;

	private final InputBindingStrategy bindingStrategy;
	private final OutputBindingStrategy unbindingStrategy;
	private String unbindingMethodName;
	private final Boolean unbindingMethodUnchecked;
	private final Invokable<?, ?> unbindingMethod;

	private final List<DataNode<?>> providedUnbindingParameters;

	protected BindingNodeImpl(BindingNodeConfiguratorImpl<?, S, T> configurator) {
		super(configurator);

		BoundSet bounds = configurator.getInferenceBounds();

		bindingStrategy = configurator
				.getOverride(BindingNode::inputBindingStrategy, BindingNodeConfigurator::getInputBindingStrategy)
				.orDefault(InputBindingStrategy.PROVIDED).get();

		unbindingStrategy = configurator
				.getOverride(BindingNode::outputBindingStrategy, BindingNodeConfigurator::getOutputBindingStrategy)
				.orDefault(OutputBindingStrategy.SIMPLE).get();

		/*
		 * TODO refactor to make this final.
		 */
		unbindingMethodName = configurator
				.getOverride(b -> b.outputBindingMethod().getName(), BindingNodeConfigurator::getOutputBindingMethod).tryGet();

		providedUnbindingParameters = abstractness().isAtLeast(Abstractness.ABSTRACT) ? null
				: findProvidedUnbindingParameters(this,
						configurator.getOverride(BindingNodeConfigurator::getProvidedOutputBindingMethodParameters)
								.orDefault(Collections.<QualifiedName> emptyList()).get());

		unbindingMethodUnchecked = configurator.getOverride(BindingNode::outputBindingMethodUnchecked,
				BindingNodeConfigurator::getOutputBindingMethodUnchecked).tryGet();

		TypeToken<T> dataType = configurator.getEffectiveDataType();
		if (abstractness().isAtMost(Abstractness.CONCRETE)) {
			try {
				dataType = dataType == null ? null
						: dataType.withLooseCompatibilityFrom(configurator.getChildrenConfigurator().getPostInputType());
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
		bindingType = inferDataType(configurator.getEffectiveBindingType(), bounds);
		unbindingType = inferDataType(configurator.getEffectiveUnbindingType(), bounds);
		unbindingFactoryType = inferDataType(configurator.getEffectiveUnbindingFactoryType(), bounds);

		unbindingMethod = abstractness().isAtLeast(Abstractness.ABSTRACT) ? null : findUnbindingMethod(configurator);

		if (unbindingMethodName == null && abstractness().isLessThan(Abstractness.ABSTRACT)
				&& unbindingStrategy != OutputBindingStrategy.SIMPLE && unbindingStrategy != OutputBindingStrategy.CONSTRUCTOR)
			unbindingMethodName = unbindingMethod.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public BindingNodeConfigurator<?, S, T> configurator() {
		return (BindingNodeConfigurator<?, S, T>) super.configurator();
	}

	protected <U> TypeToken<U> inferDataType(TypeToken<U> exactDataType, BoundSet bounds) {
		/*
		 * Incorporate bounds derived from child nodes through their input and
		 * output methods.
		 */
		if (exactDataType != null && !exactDataType.isProper()) {
			exactDataType = exactDataType.withBounds(bounds).resolve();

			if (abstractness().isLessThan(Abstractness.UNINFERRED)
					&& !((BindingNodeConfiguratorImpl<?, S, T>) configurator()).getExtensible()) {
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
	public InputBindingStrategy inputBindingStrategy() {
		return bindingStrategy;
	}

	@Override
	public TypeToken<?> inputBindingType() {
		return bindingType;
	}

	@Override
	public OutputBindingStrategy outputBindingStrategy() {
		return unbindingStrategy;
	}

	@Override
	public TypeToken<?> outputBindingType() {
		return unbindingType;
	}

	@Override
	public TypeToken<?> outputBindingFactoryType() {
		return unbindingFactoryType;
	}

	@Override
	public Invokable<?, ?> outputBindingMethod() {
		return unbindingMethod;
	}

	@Override
	public Boolean outputBindingMethodUnchecked() {
		return unbindingMethodUnchecked;
	}

	@Override
	public List<DataNode<?>> providedOutputBindingMethodParameters() {
		return providedUnbindingParameters;
	}

	private Invokable<?, ?> findUnbindingMethod(BindingNodeConfiguratorImpl<?, S, T> configurator) {
		OutputBindingStrategy unbindingStrategy = outputBindingStrategy();
		if (unbindingStrategy == null)
			unbindingStrategy = OutputBindingStrategy.SIMPLE;

		switch (unbindingStrategy) {
		case SIMPLE:
		case CONSTRUCTOR:
			return null;

		case STATIC_FACTORY:
		case PROVIDED_FACTORY:
			TypeToken<?> receiverClass = outputBindingFactoryType() != null ? outputBindingFactoryType()
					: outputBindingType();
			return findUnbindingMethod(outputBindingType(), receiverClass,
					findUnbindingMethodParameterClasses(BindingNodeImpl::dataType), configurator);

		case PASS_TO_PROVIDED:
			return findUnbindingMethod(null, outputBindingType(),
					findUnbindingMethodParameterClasses(BindingNodeImpl::dataType), configurator);

		case ACCEPT_PROVIDED:
			return findUnbindingMethod(null, dataType(), findUnbindingMethodParameterClasses(t -> t.outputBindingType()),
					configurator);
		}
		throw new AssertionError();
	}

	private static List<DataNode<?>> findProvidedUnbindingParameters(BindingNode<?, ?> node,
			List<QualifiedName> providedOutputBindingMethodParameterNames) {
		return providedOutputBindingMethodParameterNames == null
				? node.configurator().getOutputBindingMethod() == null ? null : new ArrayList<>()
				: providedOutputBindingMethodParameterNames.stream().map(p -> {
					if (p.getName().equals("this"))
						return null;
					else {
						ChildNode<?> effective = node.children().stream().filter(c -> c.name().equals(p)).findAny()
								.orElseThrow(() -> new ModabiException(t -> t.cannotFindUnbindingParameter(p)));

						if (!(effective instanceof DataNode))
							throw new ModabiException(t -> t.unbindingParameterMustBeDataNode(effective, p));

						DataNode<?> dataNode = (DataNode<?>) effective;

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
			Function<BindingNodeImpl<?, ?>, TypeToken<?>> nodeClass) {
		List<TypeToken<?>> classList = new ArrayList<>();

		boolean addedNodeClass = false;
		List<DataNode<?>> parameters = providedOutputBindingMethodParameters();
		if (parameters != null) {
			for (DataNode<?> parameter : parameters) {
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

		if (outputBindingMethodUnchecked() != null && outputBindingMethodUnchecked())
			classList = classList.stream().map(t -> t == null ? null : (TypeToken<?>) TypeToken.over(t.getRawType()))
					.collect(Collectors.toList());

		return classList;
	}

	@SuppressWarnings("unchecked")
	private <U> Invokable<?, ?> findUnbindingMethod(TypeToken<?> result, TypeToken<U> receiver,
			List<TypeToken<?>> parameters, BindingNodeConfiguratorImpl<?, S, T> configurator) {
		Invokable<?, ?> overridden = configurator.getOverride(BindingNode::outputBindingMethod, c -> null).tryGet();

		if (overridden != null) {
			Invokable<U, ?> invokable = (Invokable<U, ?>) overridden.withLooseApplicability(parameters);
			if (receiver != null)
				invokable = invokable.withReceiverType(receiver);
			if (result != null)
				invokable = invokable.withTargetType(result);

			return overridden;
		} else {
			if (outputBindingMethodUnchecked() != null && outputBindingMethodUnchecked()) {
				if (result != null)
					result = TypeToken.over(result.getRawType());
				if (receiver != null)
					receiver = (TypeToken<U>) TypeToken.over(receiver.getRawType());
				parameters = parameters.stream().map(t -> t == null ? null : (TypeToken<?>) TypeToken.over(t.getRawType()))
						.collect(Collectors.toList());
			}

			List<String> names = generateUnbindingMethodNames(result);
			return Methods.findMethod(names, receiver, inputBindingStrategy() == InputBindingStrategy.STATIC_FACTORY, result,
					false, parameters);
		}
	}

	private List<String> generateUnbindingMethodNames(TypeToken<?> resultClass) {
		List<String> names;

		if (configurator().getOutputBindingMethod() != null)
			names = Arrays.asList(configurator().getOutputBindingMethod());
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
