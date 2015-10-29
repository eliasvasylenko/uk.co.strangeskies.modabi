package uk.co.strangeskies.modabi.impl.processing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.BindingContext;
import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.utilities.IdentityProperty;

public class ChildNodeBinder {
	private final BindingContextImpl parentContext;

	public ChildNodeBinder(BindingContextImpl context) {
		this.parentContext = context;
	}

	public Object bind(ChildNode.Effective<?, ?> next) {
		BindingContextImpl context = parentContext.withBindingNode(next);

		IdentityProperty<Object> result = new IdentityProperty<>(
				context.bindingTarget());

		try {
			next.process(new SchemaProcessingContext() {
				@Override
				public <U> void accept(ComplexNode.Effective<U> node) {
					process(node, new ComplexNodeBinder(context).bind(node), context);
				}

				@Override
				public <U> void accept(DataNode.Effective<U> node) {
					process(node, new DataNodeBinder(context).bind(node), context);
				}

				public void process(InputNode.Effective<?, ?> node, List<?> data,
						BindingContext context) {
					for (Object item : data)
						result.set(invokeInMethod(node, context, result.get(), item));
				}

				@Override
				public void accept(InputSequenceNode.Effective node) {
					List<Object> parameters = BindingNodeBinder
							.getSingleBindingSequence(node, context);
					result.set(invokeInMethod(node, context, result.get(),
							parameters.toArray()));
				}

				@Override
				public void accept(SequenceNode.Effective node) {
					for (ChildNode.Effective<?, ?> child : node.children())
						new ChildNodeBinder(context).bind(child);
				}

				@Override
				public void accept(ChoiceNode.Effective node) {
					if (node.children().size() == 1) {
						new ChildNodeBinder(context)
								.bind(node.children().iterator().next());
					} else if (!node.children().isEmpty()) {
						try {
							context.attemptBindingUntilSuccessful(node.children(),
									(c, n) -> new ChildNodeBinder(c).bind(n),
									n -> new BindingException(
											"Option '" + n + "' under choice node '" + node
													+ "' could not be unbound",
											context, n));
						} catch (Exception e) {
							if (!node.occurrences().contains(0))
								throw e;
						}
					}
				}
			});
		} catch (Exception e) {
			throw new BindingException("Failed to bind node '" + next + "'", context,
					e);
		}

		return result.get();
	}

	private static Object invokeInMethod(InputNode.Effective<?, ?> node,
			BindingContext context, Object target, Object... parameters) {
		if (!"null".equals(node.getInMethodName())) {
			Object object;

			try {
				object = ((Method) node.getInMethod()).invoke(target, parameters);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw new BindingException("Unable to call method '"
						+ node.getInMethod() + "' with parameters '"
						+ Arrays.toString(parameters) + "' at node '" + node + "'", context,
						e);
			}

			if (node.isInMethodChained())
				target = object;
		}

		return target;
	}
}
