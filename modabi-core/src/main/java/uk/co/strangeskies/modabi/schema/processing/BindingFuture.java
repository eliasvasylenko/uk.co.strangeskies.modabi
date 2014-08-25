package uk.co.strangeskies.modabi.schema.processing;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.SchemaException;

public interface BindingFuture<T> extends Future<Binding<T>> {
	QualifiedName getName();

	Model<T> getModel();

	Set<BindingFuture<?>> getBlockingBindings();

	default Binding<T> resolve() {
		try {
			return get();
		} catch (InterruptedException e) {
			throw new SchemaException("Unexpected interrupt during binding of '"
					+ getName() + "' with model '" + getModel().getName() + "'.", e);
		} catch (ExecutionException e) {
			throw new SchemaException("Exception during binding of '" + getName()
					+ "' with model '" + getModel().getName() + "'.", e.getCause());
		}
	}
}
