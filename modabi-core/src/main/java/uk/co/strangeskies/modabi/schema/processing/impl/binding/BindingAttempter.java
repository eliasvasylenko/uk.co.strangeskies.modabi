package uk.co.strangeskies.modabi.schema.processing.impl.binding;

import java.util.function.Consumer;

import uk.co.strangeskies.modabi.data.io.DataItem;
import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.data.io.DataSourceDecorator;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Property;

public class BindingAttempter {
	private final BindingContext context;

	public BindingAttempter(BindingContext context) {
		this.context = context;
	}

	public void attempt(Consumer<BindingContext> bindingMethod) {
		BindingContext context = this.context.withInput(null);

		DataSource dataSource = null;
		Property<Integer, Integer> getCount = new IdentityProperty<Integer>(0);
		// StructuredDataSource input = context.input().bufferNextChild();
		// mark output! (by redirecting to a new buffer)
		if (context.isProvided(DataSource.class)) {
			dataSource = context.provide(DataSource.class);
			DataSource finalSource = new DataSourceDecorator(dataSource.copy()) {
				@Override
				public DataItem<?> get() {
					getCount.set(getCount.get() + 1);
					return super.get();
				}
			};
			context = context.withProvision(DataSource.class, () -> finalSource);
		}

		try {
			bindingMethod.accept(context);

			if (dataSource != null)
				for (int i = 0; i < getCount.get(); i++)
					dataSource.get();
		} catch (SchemaException e) {
			// reset output to mark! (by discarding buffer)

			throw e;
		}
	}
}
