package uk.co.strangeskies.modabi.impl.processing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;

public abstract class ChildNodeBinder<T extends SchemaNode.Effective<?, ?>> {
	private BindingContextImpl context;
	private final T node;

	public ChildNodeBinder(BindingContextImpl context, T node) {
		this.context = context;
		this.node = node;
	}
	
	public BindingContextImpl getContext() {
		return context;
	}

	public T getNode() {
		return node;
	}
	
	protected Object invokeInMethod(InputNode.Effective<?, ?> node, Object target,
			Object... parameters) {
		Object object;

		if (!"null".equals(node.getInMethodName())) {
			try {
				object = ((Method) node.getInMethod()).invoke(target, parameters);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw new BindingException("Unable to call method '"
						+ node.getInMethod() + "' with parameters '"
						+ Arrays.toString(parameters) + "' at node '" + node + "'",
						getContext(), e);
			}

			if (node.isInMethodChained()) {
				context = context.withReplacementBindingTarget(object);
			}
		} else {
			object = null;
		}

		return object;
	}
}
