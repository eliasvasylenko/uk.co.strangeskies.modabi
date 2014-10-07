package uk.co.strangeskies.modabi.schema.processing.unbinding.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingException;

public class UnbindingAttempter {
	private final UnbindingContext context;

	public UnbindingAttempter(UnbindingContext context) {
		this.context = context;
	}

	public <I extends SchemaNode.Effective<?, ?>> I tryForEach(
			List<I> unbindingItems, BiConsumer<UnbindingContext, I> unbindingMethod,
			Function<List<SchemaException>, UnbindingException> onFailure) {
		I success = null;

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
				success = item;
				break;
			} catch (SchemaException e) {
				failures.add(e);

				// reset output to mark! (by discarding buffer)
				context = this.context;
				output = new BufferingStructuredDataTarget();
			}
		}

		if (success != null) {
			// remove mark! (by flushing buffer into output)
			if (dataTarget != null)
				dataTarget.buffer().pipe(this.context.provide(DataTarget.class));

			output.buffer().pipeNextChild(this.context.output());

			return success;
		} else
			throw onFailure.apply(failures);
	}
}
