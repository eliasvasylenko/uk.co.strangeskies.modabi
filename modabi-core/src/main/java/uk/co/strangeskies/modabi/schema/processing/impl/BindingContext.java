package uk.co.strangeskies.modabi.schema.processing.impl;

import java.util.List;
import java.util.Set;

import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.BindingException;
import uk.co.strangeskies.modabi.schema.processing.BindingFuture;

public interface BindingContext {
	<U> U provide(Class<U> clazz);

	List<SchemaNode.Effective<?, ?>> bindingNodeStack();

	default BindingException exception(String message, Exception cause) {
		return new BindingException(message, bindingNodeStack(), cause);
	}

	default BindingException exception(String message) {
		return new BindingException(message, bindingNodeStack());
	}

	Object bindingObject();

	Model.Effective<?> getModel(QualifiedName nextElement);

	<T> Set<BindingFuture<T>> bindingFutures(Model<T> model);

	StructuredDataSource input();
}
