package uk.co.strangeskies.modabi.impl.processing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.InputNode;

public class ChildNodeBinder {
	private final BindingContextImpl parentContext;

	public ChildNodeBinder(BindingContextImpl context) {
		this.parentContext = context;
	}

	public BindingContextImpl getParentContext() {
		return parentContext;
	}

	protected Object invokeInMethod(InputNode.Effective<?, ?> node, Object target,
			Object... parameters) {
		if (!"null".equals(node.getInMethodName())) {
			Object object;

			try {
				object = ((Method) node.getInMethod()).invoke(target, parameters);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw new BindingException("Unable to call method '"
						+ node.getInMethod() + "' with parameters '"
						+ Arrays.toString(parameters) + "' at node '" + node + "'",
						getParentContext(), e);
			}

			if (node.isInMethodChained())
				target = object;
		}

		return target;
	}
}
