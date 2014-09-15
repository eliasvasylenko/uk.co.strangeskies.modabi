package uk.co.strangeskies.modabi.schema.processing.impl;

import java.util.List;

import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.BindingException;

public interface BindingContext {
	<U> U provide(Class<U> clazz);

	List<SchemaNode.Effective<?, ?>> bindingNodeStack();

	default BindingException exception(String message, Exception cause) {
		return new BindingException(message, bindingNodeStack(), cause);
	}

	Object bindingObject();
}
