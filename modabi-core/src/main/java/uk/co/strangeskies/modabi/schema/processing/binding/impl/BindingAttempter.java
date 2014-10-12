package uk.co.strangeskies.modabi.schema.processing.binding.impl;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.binding.BindingException;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingException;

public class BindingAttempter {
	private final BindingContext context;

	public BindingAttempter(BindingContext context) {
		this.context = context;
	}

	public void attempt(Consumer<BindingContext> bindingMethod) {
		bindingMethod.accept(context);
	}

	public <U> U attempt(Function<BindingContext, U> bindingMethod) {
		return bindingMethod.apply(context);
	}

	public <I extends SchemaNode.Effective<?, ?>> I attemptUntilSuccessful(
			List<I> attemptItems, BiConsumer<BindingContext, I> bindingMethod,
			Function<Set<Exception>, UnbindingException> onFailure) {
		throw new BindingException("attemptUntilSuccessful unimplemented.", context);
	}
}
