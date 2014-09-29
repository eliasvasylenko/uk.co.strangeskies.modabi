package uk.co.strangeskies.modabi.schema.processing;

import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.BaseSchema;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.MetaSchema;
import uk.co.strangeskies.modabi.schema.Schema;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.model.Models;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingTypes;

public interface SchemaManager {
	<T> void registerProvider(Class<T> providedClass, Supplier<T> provider);

	void registerProvider(Function<Class<?>, ?> provider);

	void registerSchema(Schema schema);

	// So we can import from manually added data.
	void registerBinding(Binding<?> binding);

	default <T> void registerBinding(Model<T> model, T data) {
		registerBinding(new Binding<T>(model, data));
	}

	default <T> T bind(Model<T> model, StructuredDataSource input) {
		return bindFuture(model, input).resolveNow().getData();
	}

	default Binding<?> bind(StructuredDataSource input) {
		return bindFuture(input).resolveNow();
	}

	// Blocks until all possible processing is done other than waiting imports:
	<T> BindingFuture<T> bindFuture(Model<T> model, StructuredDataSource input);

	BindingFuture<?> bindFuture(StructuredDataSource input);

	<T> Set<BindingFuture<T>> bindingFutures(Model<T> model);

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

	/*-
	 * TODO Best effort at unbinding, outputting comments on errors instead of
	 * throwing exceptions
	 *
	 * <T> Set<Exception> unbind(Model<T> model, StructuredDataTarget output, T
	 * data);
	 */
	MetaSchema getMetaSchema();

	BaseSchema getBaseSchema();

	<U> U provide(Class<U> clazz);

	boolean isProvided(Class<?> clazz);

	Schemata registeredSchemata();

	Models registeredModels();

	DataBindingTypes registeredTypes();
}
