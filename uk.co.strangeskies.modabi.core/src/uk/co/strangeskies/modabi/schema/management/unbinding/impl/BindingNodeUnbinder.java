package uk.co.strangeskies.modabi.schema.management.unbinding.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ClassUtils;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.management.ValueResolution;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingContext;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingException;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.ChoiceNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;

public class BindingNodeUnbinder {
	private final UnbindingContextImpl context;

	public BindingNodeUnbinder(UnbindingContextImpl context) {
		this.context = context;
	}

	public <U> void unbind(BindingNode.Effective<U, ?, ?> node, U data) {
		UnbindingContextImpl context = this.context.withUnbindingNode(node)
				.withProvision(BindingNode.Effective.class, () -> node);

		Function<Object, Object> supplier = Function.identity();
		if (node.getUnbindingStrategy() != null) {
			switch (node.getUnbindingStrategy()) {
			case SIMPLE:
				break;
			case PASS_TO_PROVIDED:
				supplier = u -> {
					Object o = context.provisions().provide(node.getUnbindingType());
					invokeMethod(node.getUnbindingMethod(), context, o,
							prepareUnbingingParameterList(node, u));
					return o;
				};
				break;
			case ACCEPT_PROVIDED:
				supplier = u -> {
					Object o = context.provisions().provide(node.getUnbindingType());
					invokeMethod(node.getUnbindingMethod(), context, u,
							prepareUnbingingParameterList(node, o));
					return o;
				};
				break;
			case CONSTRUCTOR:
				supplier = u -> {
					Constructor<?> c = null;
					try {
						c = node.getUnbindingType().getConstructor(u.getClass());
						return c.newInstance(prepareUnbingingParameterList(node, u));
					} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						throw new SchemaException("Cannot invoke constructor " + c + " on "
								+ node.getUnbindingType(), e);
					}
				};
				break;
			case STATIC_FACTORY:
				supplier = u -> invokeMethod(node.getUnbindingMethod(), context, null,
						prepareUnbingingParameterList(node, u));

				break;
			case PROVIDED_FACTORY:
				supplier = u -> invokeMethod(node.getUnbindingMethod(), context,
						context.provisions().provide(node.getUnbindingFactoryType()),
						prepareUnbingingParameterList(node, u));

				break;
			}
		}

		SchemaProcessingContext processingContext = getProcessingContext(context
				.withUnbindingSource(supplier.apply(data)));

		for (ChildNode.Effective<?, ?> child : node.children())
			child.process(processingContext);
	}

	private SchemaProcessingContext getProcessingContext(
			UnbindingContextImpl context) {
		return new SchemaProcessingContext() {
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
				for (ChildNode.Effective<?, ?> child : node.children())
					child.process(getProcessingContext(context.withUnbindingNode(node)));
			}

			@Override
			public void accept(ChoiceNode.Effective node) {
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
				if (parameter != null)
					parameters.add(parameter.providedValues() == null ? null : parameter
							.providedValues().get(0));
				else {
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
							.collect(Collectors.joining(", ")) + "]'.", context, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <U> List<U> getData(BindingChildNode.Effective<U, ?, ?> node,
			UnbindingContext context) {
		List<U> itemList;

		Object parent = context.unbindingSource();

		if (node.getDataType() == null)
			throw new UnbindingException("Cannot unbind node '" + node.getName()
					+ "' with no data class.", context);

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
							o -> !ClassUtils.isAssignable(o.getClass(), node.getDataType()))
					.findAny().orElse(null);
			if (failedCast != null)
				throw new ClassCastException("Cannot cast " + failedCast.getClass()
						+ " to " + node.getDataType());
		} else {
			U item;
			if ("this".equals(node.getOutMethodName()))
				item = (U) parent;
			else
				item = (U) invokeMethod(node.getOutMethod(), context, parent);

			if (item == null)
				itemList = null;
			else {
				if (!ClassUtils.isAssignable(item.getClass(), node.getDataType()))
					throw new ClassCastException("Cannot cast " + item.getClass()
							+ " to " + node.getDataType());
				itemList = Arrays.asList(item);
			}
		}

		if (itemList != null && node.occurrences() != null
				&& !node.occurrences().contains(itemList.size()))
			throw new UnbindingException("Output list '" + itemList
					+ "' must contain a number of items within range '"
					+ Range.compose(node.occurrences()) + "' to be unbound by node '"
					+ node + "'.", context);

		return itemList;
	}
}
