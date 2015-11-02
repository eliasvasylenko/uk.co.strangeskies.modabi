package uk.co.strangeskies.modabi.impl.processing;

import java.util.function.Consumer;

import uk.co.strangeskies.modabi.schema.InputSequenceNode;

public class InputSequenceNodeBinder
		extends InputNodeBinder<InputSequenceNode.Effective> {
	public InputSequenceNodeBinder(BindingContextImpl context,
			InputSequenceNode.Effective node) {
		super(context, node);

		Consumer<BindingContextImpl> bind = c -> invokeInMethod(
				BindingNodeBinder.getSingleBindingSequence(node, c).toArray());

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
