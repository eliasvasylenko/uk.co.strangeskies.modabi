package uk.co.strangeskies.modabi.schema.processing;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;

public interface SchemaBinder {
	<T> void registerProvider(Class<T> providedClass, Supplier<T> provider);

	void registerProvider(Function<Class<?>, ?> provider);

	void registerSchema(Schema schema);

	// So we can import from manually added data.
	void registerBinding(Binding<?> binding);

	// So we can import from manually added data.
	default <T> void registerBinding(Model<T> model, T data) {
		registerBinding(new Binding<T>(model, data));
	}

	default <T> T bind(Model<T> model, StructuredDataSource input) {
		BindingFuture<T> future = bindFuture(model, input);
		// TODO check if completed, if not throw exception based on blocking binding
		// futures. then the resolve will be guaranteed to return instantly:
		return future.resolve().getData();
	}

	// Blocks until all possible processing is done other than waiting imports:
	<T> BindingFuture<T> bindFuture(Model<T> model, StructuredDataSource input);

	default Binding<?> bind(StructuredDataSource input) {
		BindingFuture<?> future = bindFuture(input);
		// TODO check if completed, if not throw exception based on blocking binding
		// futures. then the resolve will be guaranteed to return instantly:
		return future.resolve();
	}

	BindingFuture<?> bindFuture(StructuredDataSource input);

	default Schema registerSchemaBinding(StructuredDataSource input) {
		Schema schema = bind(getMetaSchema().getSchemaModel(), input);

		registerSchema(schema);

		return schema;
	}

	default BindingFuture<Schema> registerSchemaBindingFuture(
			StructuredDataSource input) {
		BindingFuture<Schema> schema = bindFuture(getMetaSchema().getSchemaModel(),
				input);

		new Thread(() -> {
			try {
				registerSchema(schema.get().getData());
			} catch (InterruptedException | ExecutionException
					| CancellationException e) {
			}
		});

		return schema;
	}

	<T> void unbind(Model<T> model, StructuredDataTarget output, T data);

	MetaSchema getMetaSchema();

	BaseSchema getBaseSchema();
}
