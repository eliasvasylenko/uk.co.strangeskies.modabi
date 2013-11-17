package uk.co.strangeskies.modabi.schema.node.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.BranchingSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public abstract class BranchingSchemaNodeImpl<T extends SchemaProcessingContext<? extends T>>
		implements BranchingSchemaNode<T> {
	private final List<SchemaNode<? super T>> children;

	private final String inMethod;

	public BranchingSchemaNodeImpl(
			Collection<? extends SchemaNode<? super T>> children, String inMethod) {
		this.children = new ArrayList<>(children);
		this.inMethod = inMethod;
	}

	@Override
	public List<SchemaNode<? super T>> getChildren() {
		return children;
	}

	@Override
	public String getInMethod() {
		return inMethod;
	}
}
