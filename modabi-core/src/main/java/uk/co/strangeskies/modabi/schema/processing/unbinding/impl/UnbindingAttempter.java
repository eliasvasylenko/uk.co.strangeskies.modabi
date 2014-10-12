package uk.co.strangeskies.modabi.schema.processing.unbinding.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingException;

public class UnbindingAttempter {
	private final UnbindingContext context;

	public UnbindingAttempter(UnbindingContext context) {
		this.context = context;
	}

	public void attempt(Consumer<UnbindingContext> unbindingMethod) {
		UnbindingContext context = this.context;

		BufferingDataTarget dataTarget = null;
		BufferingStructuredDataTarget output = new BufferingStructuredDataTarget();

		/*
		 * Mark output! (by redirecting to a new buffer)
		 */
		if (context.isProvided(DataTarget.class)) {
			dataTarget = new BufferingDataTarget();
			DataTarget finalTarget = dataTarget;
			context = context.withProvision(DataTarget.class, () -> finalTarget);
		}
		context = context.withOutput(output);

		/*
		 * Make unbinding attempt! (Reset output to mark on failutre by discarding
		 * buffer, via exception.)
		 */
		unbindingMethod.accept(context);

		/*
		 * Remove mark! (by flushing buffer into output)
		 */
		if (dataTarget != null)
			dataTarget.buffer().pipe(this.context.provide(DataTarget.class));

		output.buffer().pipeNextChild(this.context.output());
	}

	public <I extends SchemaNode.Effective<?, ?>> I attemptUntilSuccessful(
			List<I> attemptItems, BiConsumer<UnbindingContext, I> unbindingMethod,
			Function<Set<Exception>, UnbindingException> onFailure) {
		if (attemptItems.isEmpty())
			throw new IllegalArgumentException(
					"Must supply items for unbinding attempt.");

		Set<Exception> failures = new HashSet<>();

		for (I item : attemptItems)
			try {
				attempt(c -> unbindingMethod.accept(c, item));

				return item;
			} catch (Exception e) {
				failures.add(e);
			}

		throw onFailure.apply(failures);
	}
}
