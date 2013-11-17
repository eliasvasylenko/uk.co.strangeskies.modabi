package uk.co.strangeskies.modabi.schema.node.impl;

import java.util.Collection;

import uk.co.strangeskies.modabi.schema.node.BranchSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class BranchSchemaNodeImpl<T extends SchemaProcessingContext<T>> extends
		BranchingSchemaNodeImpl<T> implements BranchSchemaNode<T> {
	private final boolean choice;

	public BranchSchemaNodeImpl(
			Collection<? extends SchemaNode<? super T>> children, boolean choice,
			String inMethod) {
		super(children, inMethod);
		this.choice = choice;
	}

	@Override
	public boolean isChoice() {
		return choice;
	}

	@Override
	public void process(T context) {
		context.branch(this);
	}
}
