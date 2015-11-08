package uk.co.strangeskies.modabi.impl.processing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

public abstract class InputNodeBinder<T extends InputNode.Effective<?, ?>>
		extends ChildNodeBinder<T> {
	public InputNodeBinder(BindingContextImpl context, T node) {
		super(context, node);
	}

	protected Object invokeInMethod(Object... parameters) {
		TypedObject<?> target = getContext().bindingTarget();

		TypedObject<?> result;

		if (!"null".equals(getNode().getInMethodName())) {
			try {
				result = newTypedObject(getNode().getPostInputType(),
						((Method) getNode().getInMethod()).invoke(target.getObject(),
								parameters));
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

	@SuppressWarnings("unchecked")
	private <U> TypedObject<U> newTypedObject(TypeToken<U> type, Object object) {
		return new TypedObject<>(type, (U) object);
	}
}
