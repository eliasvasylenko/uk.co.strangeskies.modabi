package uk.co.strangeskies.modabi.schema.processing.impl.binding;

import java.util.function.Consumer;

import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.schema.SchemaException;

public class BindingAttempter {
	private final BindingContext context;

	public BindingAttempter(BindingContext context) {
		this.context = context;
	}

	public void attempt(Consumer<BindingContext> bindingMethod) {
		BindingContext context = this.context.withInput(null);

		DataSource dataSource;
		// StructuredDataSource input = context.input().bufferNextChild();
		// mark output! (by redirecting to a new buffer)
		if (context.isProvided(DataSource.class)) {
			dataSource = context.provide(DataSource.class);
			DataSource finalSource = dataSource.copy();
			context = context.withProvision(DataSource.class, () -> finalSource);
		}

		try {
			bindingMethod.accept(context);
		} catch (SchemaException e) {
			// reset output to mark! (by discarding buffer)

			throw e;
		}
	}
}
