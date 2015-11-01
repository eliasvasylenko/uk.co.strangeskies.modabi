package uk.co.strangeskies.modabi.impl.processing;

import uk.co.strangeskies.modabi.schema.InputSequenceNode;

public class InputSequenceNodeBinding<T extends InputSequenceNode.Effective>
		extends InputNodeBinding<T> {
	public InputSequenceNodeBinding(BindingContextImpl context, T node) {
		super(context, node);

		invokeInMethod(
				BindingNodeBinder.getSingleBindingSequence(node, context).toArray());
	}
}
