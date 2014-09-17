package uk.co.strangeskies.modabi.schema.processing.impl.unbinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.DataTarget;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.utilities.MultiException;

public class UnbindingAttempter {
	private final UnbindingContext context;

	public UnbindingAttempter(UnbindingContext context) {
		this.context = context;
	}

	public <I extends SchemaNode.Effective<?, ?>> void tryForEach(
			List<I> unbindingItems, BiConsumer<UnbindingContext, I> unbindingMethod,
			Function<List<SchemaException>, MultiException> onFailure) {
		if (unbindingItems.isEmpty())
			throw new IllegalArgumentException(
					"Must supply items for unbinding attempt.");

		List<SchemaException> failures = new ArrayList<>();

		UnbindingContext context = this.context;

		BufferingDataTarget dataTarget = null;
		BufferingStructuredDataTarget output = new BufferingStructuredDataTarget();
		for (I item : unbindingItems) {
			// mark output! (by redirecting to a new buffer)
			if (context.isProvided(DataTarget.class)) {
				dataTarget = new BufferingDataTarget();
				DataTarget finalTarget = dataTarget;
				context = context.withProvision(DataTarget.class, () -> finalTarget);
			}
			context = context.withOutput(output);

			try {
				unbindingMethod.accept(context, item);
				failures.clear();
				break;
			} catch (SchemaException e) {
				failures.add(e);

				// reset output to mark! (by discarding buffer)
				context = this.context;
				output = new BufferingStructuredDataTarget();
			}
		}

		if (failures.isEmpty()) {
			// remove mark! (by flushing buffer into output)
			if (dataTarget != null)
				dataTarget.buffer().pipe(this.context.provide(DataTarget.class));

			output.buffer().pipeNextChild(this.context.output());
		} else
			throw onFailure.apply(failures);
	}
}
