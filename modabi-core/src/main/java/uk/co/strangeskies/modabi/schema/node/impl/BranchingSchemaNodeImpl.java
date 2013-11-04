package uk.co.strangeskies.modabi.schema.node.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.BranchingSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public class BranchingSchemaNodeImpl implements BranchingSchemaNode {
	private final List<SchemaNode> children;

	private final boolean choice;

	private final String inMethod;

	public BranchingSchemaNodeImpl(Collection<? extends SchemaNode> children,
			boolean choice, String inMethod) {
		this.children = new ArrayList<>(children);
		this.choice = choice;
		this.inMethod = inMethod;
	}

	@Override
	public List<SchemaNode> getChildren() {
		return children;
	}

	@Override
	public boolean isChoice() {
		return choice;
	}

	@Override
	public String getInMethod() {
		return inMethod;
	}
}
