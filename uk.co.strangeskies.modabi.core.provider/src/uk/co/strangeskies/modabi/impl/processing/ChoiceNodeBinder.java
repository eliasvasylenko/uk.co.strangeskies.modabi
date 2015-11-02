package uk.co.strangeskies.modabi.impl.processing;

import java.util.function.Consumer;

import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.ChoiceNode;

public class ChoiceNodeBinder extends ChildNodeBinder<ChoiceNode.Effective> {
	public ChoiceNodeBinder(BindingContextImpl parentContext,
			ChoiceNode.Effective node) {
		super(parentContext, node);

		Consumer<BindingContextImpl> bind = context -> {
			if (node.children().size() == 1) {
				bind(context, node.children().iterator().next());
			} else if (!node.children().isEmpty()) {
				context
						.attemptBindingUntilSuccessful(node.children(),
								(c, n) -> bind(c, n),
								n -> new BindingException("Option '" + n
										+ "' under choice node '" + node + "' could not be unbound",
								context, n));
			}
		};

		repeatNode(count -> {
			if (node.occurrences().isValueBelow(count)) {
				bind.accept(parentContext);
			} else {
				try {
					parentContext.attemptBinding(bind);
				} catch (Exception e) {
					return false;
				}
			}
			return true;
		});
	}
}
