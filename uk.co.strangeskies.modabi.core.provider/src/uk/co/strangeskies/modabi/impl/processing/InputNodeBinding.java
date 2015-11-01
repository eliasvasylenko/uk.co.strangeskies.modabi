package uk.co.strangeskies.modabi.impl.processing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.InputNode;

public class InputNodeBinding<T extends InputNode.Effective<?, ?>> {
	private BindingContextImpl context;
	private final T node;

	public InputNodeBinding(BindingContextImpl context, T node) {
		this.context = context;
		this.node = node;
	}

	public InputNodeBinding(BindingContextImpl context, T node,
			Object... parameters) {
		this(context, node);

		invokeInMethod(parameters);
	}

	protected void setContext(BindingContextImpl context) {
		this.context = context;
	}

	public BindingContextImpl getContext() {
		return context;
	}

	public T getNode() {
		return node;
	}

	protected Object invokeInMethod(Object... parameters) {
		Object target = getContext().bindingTarget();

		Object result;

		if (!"null".equals(getNode().getInMethodName())) {
			try {
				result = ((Method) getNode().getInMethod()).invoke(target, parameters);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw new BindingException("Unable to call method '"
						+ getNode().getInMethod() + "' with parameters '"
						+ Arrays.toString(parameters) + "' at node '" + getNode() + "'",
						getContext(), e);
			}

			if (getNode().isInMethodChained()) {
				setContext(getContext().withReplacementBindingTarget(result));
				target = result;
			}
		} else {
			result = null;
		}

		return target;
	}
}
