package uk.co.strangeskies.modabi.impl.processing;

import java.util.function.Consumer;

import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;

public class SequenceNodeBinder
		extends ChildNodeBinder<SequenceNode.Effective> {
	public SequenceNodeBinder(BindingContextImpl context,
			SequenceNode.Effective node) {
		super(context, node);

		Consumer<BindingContextImpl> bind = c -> {
			for (ChildNode.Effective<?, ?> child : node.children())
				bind(context, child);
		};

		repeatNode(count -> {
			if (node.occurrences().isValueBelow(count)) {
				bind.accept(context);
			} else {
				try {
					context.attemptBinding(bind);
				} catch (Exception e) {
					return false;
				}
			}
			return true;
		});
	}
}
