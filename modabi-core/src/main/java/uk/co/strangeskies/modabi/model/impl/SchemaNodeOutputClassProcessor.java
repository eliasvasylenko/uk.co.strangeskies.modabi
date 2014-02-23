package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.modabi.model.ChoiceNode;
import uk.co.strangeskies.modabi.model.ContentNode;
import uk.co.strangeskies.modabi.model.ElementNode;
import uk.co.strangeskies.modabi.model.PropertyNode;
import uk.co.strangeskies.modabi.model.SequenceNode;
import uk.co.strangeskies.modabi.model.SimpleElementNode;
import uk.co.strangeskies.modabi.model.TypedDataNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class SchemaNodeOutputClassProcessor implements
		SchemaProcessingContext<Class<?>> {
	protected static Class<?> getPostInputClass(TypedDataNode<?> node) {
		return node.isInMethodChained() ? SchemaNodeInputClassProcessor
				.getInputClass(node) : node.getInMethod() == null ? null : node
				.getInMethod().getReturnType();
	}

	@Override
	public <U> Class<?> accept(ContentNode<U> node) {
		return getPostInputClass(node);
	}

	@Override
	public <U> Class<?> accept(PropertyNode<U> node) {
		return getPostInputClass(node);
	}

	@Override
	public <U> Class<?> accept(SimpleElementNode<U> node) {
		return getPostInputClass(node);
	}

	@Override
	public Class<?> accept(ChoiceNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> accept(SequenceNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> Class<?> accept(ElementNode<U> node) {
		// TODO Auto-generated method stub
		return null;
	}

}
