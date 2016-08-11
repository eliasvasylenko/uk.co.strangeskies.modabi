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

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingChildNode.OutputMemberType;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;
import uk.co.strangeskies.reflection.Types;

public class BindingNodeUnbinder {
	private final ProcessingContext context;

	public BindingNodeUnbinder(ProcessingContext context) {
		this.context = context;
	}

	public <U> void unbind(BindingNode<U, ?> node, U data) {
		ProcessingContextImpl context = new ProcessingContextImpl(this.context).withBindingNode(node)
				.withNestedProvisionScope();
		context.provisions().add(Provider.over(new TypeToken<BindingNode<?, ?>>() {}, () -> node));

		TypeToken<?> unbindingType = node.outputBindingType() != null ? node.outputBindingType() : node.dataType();
		TypeToken<?> unbindingFactoryType = node.outputBindingFactoryType() != null ? node.outputBindingFactoryType()
				: unbindingType;

		Function<Object, TypedObject<?>> supplier = u -> TypedObject.castInto(unbindingType, u);
		if (node.outputBindingStrategy() != null) {
			switch (node.outputBindingStrategy()) {
			case SIMPLE:
				break;
			case PASS_TO_PROVIDED:
				supplier = u -> {
					TypedObject<?> o = context.provide(unbindingType);
					invokeMethod((Method) node.outputBindingMethod().getMember(), context, o.getObject(),
							prepareUnbingingParameterList(node, u));
					return o;
				};
				break;
			case ACCEPT_PROVIDED:
				supplier = u -> {
					TypedObject<?> o = context.provide(unbindingType);
					invokeMethod((Method) node.outputBindingMethod().getMember(), context, u,
							prepareUnbingingParameterList(node, o.getObject()));
					return o;
				};
				break;
			case CONSTRUCTOR:
				supplier = u -> TypedObject.castInto(unbindingType, invokeConstructor(
						(Constructor<?>) node.outputBindingMethod().getMember(), context, prepareUnbingingParameterList(node, u)));
				break;
			case STATIC_FACTORY:
				supplier = u -> TypedObject.castInto(unbindingFactoryType, invokeMethod(
						(Method) node.outputBindingMethod().getMember(), context, null, prepareUnbingingParameterList(node, u)));
				break;
			case PROVIDED_FACTORY:
				supplier = u -> TypedObject.castInto(unbindingFactoryType,
						invokeMethod((Method) node.outputBindingMethod().getMember(), context,
								context.provide(unbindingFactoryType).getObject(), prepareUnbingingParameterList(node, u)));
				break;
			default:
				throw new AssertionError();
			}
		}

		Consumer<ChildNode<?>> processingContext = getChildProcessor(context.withBindingObject(supplier.apply(data)));

		for (ChildNode<?> child : node.children()) {
			processingContext.accept(child);
		}
	}

	private Consumer<ChildNode<?>> getChildProcessor(ProcessingContextImpl context) {
		NodeProcessor processor = new NodeProcessor() {
			private <U> boolean checkData(BindingChildNode<U, ?> node, List<U> data) {
				if (data == null) {
					if (!node.occurrences().contains(0) && !node.nullIfOmitted()) {
						throw new ProcessingException(t -> t.mustHaveData(node.name()), context);
					} else {
						return false;
					}
				} else {
					return true;
				}
			}

			@Override
			public <U> void accept(ComplexNode<U> node) {
				List<U> data = getData(node, context);

				if (checkData(node, data)) {
					new ComplexNodeUnbinder(context).unbind(node, data);
				}
			}

			@Override
			public <U> void accept(DataNode<U> node) {
				if (node.outputMemberType() != OutputMemberType.NONE) {
					List<U> data = null;
					if (!node.isValueProvided() || node.valueResolution() == ValueResolution.REGISTRATION_TIME
							|| node.valueResolution() == ValueResolution.POST_REGISTRATION)
						data = getData(node, context);

					if (checkData(node, data)) {
						new DataNodeUnbinder(context).unbind(node, data);
					}
				}
			}

			@Override
			public void accept(InputSequenceNode node) {
				acceptSequence(node);
			}

			@Override
			public void accept(SequenceNode node) {
				acceptSequence(node);
			}

			public void acceptSequence(SchemaNode<?> node) {
				Consumer<ChildNode<?>> childProcessor = getChildProcessor(context.withBindingNode(node));

				for (ChildNode<?> child : node.children())
					childProcessor.accept(child);
			}

			@Override
			public void accept(ChoiceNode node) {
				try {
					context.withBindingNode(node).attemptUnbindingUntilSuccessful(node.children(),
							(c, n) -> getChildProcessor(c).accept(n),
							n -> new ProcessingException("Option '" + n + "' under choice node '" + node + "' could not be unbound",
									context, n));
				} catch (Exception e) {
					if (!node.occurrences().contains(0))
						throw e;
				}
			}
		};

		return node -> {
			node.process(processor);
		};
	}

	private Object[] prepareUnbingingParameterList(BindingNode<?, ?> node, Object data) {
		List<Object> parameters = new ArrayList<>();

		if (node.providedOutputBindingMethodParameters() != null)
			for (BindingNode<?, ?> parameter : node.providedOutputBindingMethodParameters()) {
				if (parameter == node) {
					parameters.add(data);
				} else {
					List<?> values = ((DataNode<?>) parameter).providedValues();
					parameters.add(values == null ? null : values.get(0));

					System.out.println("?"+parameter.name());
					System.out.println("?"+parameter.getClass());
				}
			}

		return parameters.toArray();
	}

	private static Object invokeMethod(Method method, ProcessingContext context, Object receiver, Object... parameters) {
		try {
			return method.invoke(receiver, parameters);
		} catch (IllegalAccessException | ModabiException | InvocationTargetException | SecurityException
				| NullPointerException e) {
			throw new ProcessingException(
					"Cannot invoke method '" + method + "' on '" + receiver + "' with arguments '["
							+ Arrays.asList(parameters).stream().map(Objects::toString).collect(Collectors.joining(", ")) + "]'",
					context, e);
		}
	}

	private static Object invokeConstructor(Constructor<?> method, ProcessingContext context, Object... parameters) {
		try {
			return method.newInstance(parameters);
		} catch (NullPointerException | InstantiationException | IllegalAccessException | ModabiException
				| InvocationTargetException e) {
			throw new ProcessingException(
					"Cannot invoke method '" + method + "' with arguments '["
							+ Arrays.asList(parameters).stream().map(Objects::toString).collect(Collectors.joining(", ")) + "]'",
					context, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <U> List<U> getData(BindingChildNode<U, ?> node, ProcessingContext context) {
		List<U> itemList;

		Object parent = context.getBindingObject().getObject();

		if (node.dataType() == null)
			throw new ProcessingException(
					"Cannot unbind node '" + node.name() + "' from object '" + parent + "' with no data class.", context);

		if ((node.outputMethod() == null && node.outputMemberType() == OutputMemberType.METHOD)
				|| (node.outputField() == null && node.outputMemberType() == OutputMemberType.FIELD))
			throw new ProcessingException(
					"Cannot unbind node '" + node.name() + "' from object '" + parent + "' with no output member.", context);

		if (node.iterableOutput() != null && node.iterableOutput()) {
			Iterable<U> iterable = null;
			if (node.outputMemberType() == OutputMemberType.SELF)
				iterable = (Iterable<U>) parent;
			else
				iterable = (Iterable<U>) invokeMethod((Method) node.outputMethod().getMember(), context, parent);

			itemList = StreamSupport.stream(iterable.spliterator(), false).filter(Objects::nonNull)
					.collect(Collectors.toList());
			U failedCast = itemList.stream()
					.filter(o -> !Types.isLooseInvocationContextCompatible(o.getClass(), node.dataType().getRawType())).findAny()
					.orElse(null);
			if (failedCast != null)
				throw new ClassCastException("Cannot cast " + failedCast.getClass() + " to " + node.dataType());
		} else {
			U item;
			if (node.outputMemberType() == OutputMemberType.SELF)
				item = (U) parent;
			else
				item = (U) invokeMethod((Method) node.outputMethod().getMember(), context, parent);

			if (item == null)
				itemList = null;
			else {
				if (!Types.isLooseInvocationContextCompatible(item.getClass(), node.dataType().getRawType()))
					throw new ProcessingException("Cannot unbind node '" + node + "'", context,
							new ClassCastException("Cannot cast " + item.getClass() + " to " + node.dataType()));
				itemList = Arrays.asList(item);
			}
		}

		if (itemList != null && node.occurrences() != null && !node.occurrences().contains(itemList.size()))
			throw new ProcessingException(t -> t.mustHaveDataWithinRange(node), context);

		return itemList;
	}
}
