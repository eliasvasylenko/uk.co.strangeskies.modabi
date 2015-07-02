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
package uk.co.strangeskies.modabi.impl.processing;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.SchemaProcessingContext;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.processing.UnbindingContext;
import uk.co.strangeskies.modabi.processing.UnbindingException;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

public class BindingNodeUnbinder {
	private final UnbindingContextImpl context;

	public BindingNodeUnbinder(UnbindingContextImpl context) {
		this.context = context;
	}

	public <U> void unbind(BindingNode.Effective<U, ?, ?> node, U data) {
		UnbindingContextImpl context = this.context.withUnbindingNode(node)
				.withProvision(new TypeToken<BindingNode.Effective<?, ?, ?>>() {},
						() -> node);

		Function<Object, Object> supplier = Function.identity();
		if (node.getUnbindingStrategy() != null) {
			switch (node.getUnbindingStrategy()) {
			case SIMPLE:
				break;
			case PASS_TO_PROVIDED:
				supplier = u -> {
					Object o = context.provisions().provide(node.getUnbindingType());
					invokeMethod((Method) node.getUnbindingMethod(), context, o,
							prepareUnbingingParameterList(node, u));
					return o;
				};
				break;
			case ACCEPT_PROVIDED:
				supplier = u -> {
					Object o = context.provisions().provide(node.getUnbindingType());
					invokeMethod((Method) node.getUnbindingMethod(), context, u,
							prepareUnbingingParameterList(node, o));
					return o;
				};
				break;
			case CONSTRUCTOR:
				supplier = u -> invokeConstructor(
						(Constructor<?>) node.getUnbindingMethod(), context,
						prepareUnbingingParameterList(node, u));
				break;
			case STATIC_FACTORY:
				supplier = u -> invokeMethod((Method) node.getUnbindingMethod(),
						context, null, prepareUnbingingParameterList(node, u));
				break;
			case PROVIDED_FACTORY:
				supplier = u -> invokeMethod((Method) node.getUnbindingMethod(),
						context,
						context.provisions().provide(node.getUnbindingFactoryType()),
						prepareUnbingingParameterList(node, u));
				break;
			}
		}

		Consumer<ChildNode.Effective<?, ?>> processingContext = getChildProcessor(context
				.withUnbindingSource(supplier.apply(data)));

		for (ChildNode.Effective<?, ?> child : node.children()) {
			processingContext.accept(child);
		}
	}

	private Consumer<ChildNode.Effective<?, ?>> getChildProcessor(
			UnbindingContextImpl context) {
		SchemaProcessingContext processor = new SchemaProcessingContext() {
			@Override
			public <U> void accept(ComplexNode.Effective<U> node) {
				new ComplexNodeUnbinder(context).unbind(node, getData(node, context));
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				if (node.getOutMethodName() == null
						|| !node.getOutMethodName().equals("null")) {
					List<U> data = null;
					if (!node.isValueProvided()
							|| node.valueResolution() == ValueResolution.REGISTRATION_TIME)
						data = getData(node, context);

					new DataNodeUnbinder(context).unbind(node, data);
				}
			}

			@Override
			public void accept(InputSequenceNode.Effective node) {
				acceptSequence(node);
			}

			@Override
			public void accept(SequenceNode.Effective node) {
				acceptSequence(node);
			}

			public void acceptSequence(SchemaNode.Effective<?, ?> node) {
				Consumer<ChildNode.Effective<?, ?>> childProcessor = getChildProcessor(context
						.withUnbindingNode(node));

				for (ChildNode.Effective<?, ?> child : node.children())
					childProcessor.accept(child);
			}

			@Override
			public void accept(ChoiceNode.Effective node) {
				try {
					context.withUnbindingNode(node).attemptUnbindingUntilSuccessful(
							node.children(), (c, n) -> getChildProcessor(c).accept(n),
							n -> new UnbindingException("???", context, n));
				} catch (Exception e) {
					if (node.isMandatory() == null || node.isMandatory())
						throw e;
				}
			}
		};

		return node -> {
			try {
				node.process(processor);
			} catch (Exception e) {
				throw new UnbindingException("Failed to unbind node '" + node + "'",
						context, e);
			}
		};
	}

	private Object[] prepareUnbingingParameterList(
			BindingNode.Effective<?, ?, ?> node, Object data) {
		List<Object> parameters = new ArrayList<>();

		boolean addedData = false;
		if (node.getProvidedUnbindingMethodParameters() != null)
			for (DataNode.Effective<?> parameter : node
					.getProvidedUnbindingMethodParameters()) {
				if (parameter != null) {
					parameters.add(parameter.providedValues() == null ? null : parameter
							.providedValues().get(0));
				} else {
					parameters.add(data);
					addedData = true;
				}
			}
		if (!addedData)
			parameters.add(0, data);

		return parameters.toArray();
	}

	private static Object invokeMethod(Method method, UnbindingContext context,
			Object receiver, Object... parameters) {
		try {
			return method.invoke(receiver, parameters);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | SecurityException | NullPointerException e) {
			throw new UnbindingException("Cannot invoke method '"
					+ method
					+ "' on '"
					+ receiver
					+ "' with arguments '["
					+ Arrays.asList(parameters).stream().map(Objects::toString)
							.collect(Collectors.joining(", ")) + "]'", context, e);
		}
	}

	private static Object invokeConstructor(Constructor<?> method,
			UnbindingContext context, Object... parameters) {
		try {
			return method.newInstance(parameters);
		} catch (NullPointerException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new UnbindingException("Cannot invoke method '"
					+ method
					+ "' with arguments '["
					+ Arrays.asList(parameters).stream().map(Objects::toString)
							.collect(Collectors.joining(", ")) + "]'", context, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <U> List<U> getData(BindingChildNode.Effective<U, ?, ?> node,
			UnbindingContext context) {
		List<U> itemList;

		Object parent = context.unbindingSource();

		if (node.getDataType() == null)
			throw new UnbindingException("Cannot unbind node '" + node.getName()
					+ "' from object '" + parent + "' with no data class.", context);

		if (node.getOutMethod() == null
				&& (node.getOutMethodName() == null || !node.getOutMethodName().equals(
						"this")))
			throw new UnbindingException("Cannot unbind node '" + node.getName()
					+ "' from object '" + parent + "' with no out method.", context);

		if (node.isOutMethodIterable() != null && node.isOutMethodIterable()) {
			Iterable<U> iterable = null;
			if (node.getOutMethodName() != null
					&& node.getOutMethodName().equals("this"))
				iterable = (Iterable<U>) parent;
			else
				iterable = (Iterable<U>) invokeMethod(node.getOutMethod(), context,
						parent);

			itemList = StreamSupport.stream(iterable.spliterator(), false)
					.filter(Objects::nonNull).collect(Collectors.toList());
			U failedCast = itemList
					.stream()
					.filter(
							o -> !Types.isLooseInvocationContextCompatible(o.getClass(), node
									.getDataType().getRawType())).findAny().orElse(null);
			if (failedCast != null)
				throw new ClassCastException("Cannot cast " + failedCast.getClass()
						+ " to " + node.getDataType());
		} else {
			U item;
			if (node.getOutMethodName() != null
					&& node.getOutMethodName().equals("this"))
				item = (U) parent;
			else
				item = (U) invokeMethod(node.getOutMethod(), context, parent);

			if (item == null)
				itemList = null;
			else {
				if (!Types.isLooseInvocationContextCompatible(item.getClass(), node
						.getDataType().getRawType()))
					throw new UnbindingException("Cannot unbind node '" + node + "'",
							context, new ClassCastException("Cannot cast " + item.getClass()
									+ " to " + node.getDataType()));
				itemList = Arrays.asList(item);
			}
		}

		if (itemList != null && node.occurrences() != null
				&& !node.occurrences().contains(itemList.size()))
			throw new UnbindingException("Output list '" + itemList
					+ "' must contain a number of items within range '"
					+ Range.compose(node.occurrences()) + "' to be unbound by node '"
					+ node + "'", context);

		return itemList;
	}
}
