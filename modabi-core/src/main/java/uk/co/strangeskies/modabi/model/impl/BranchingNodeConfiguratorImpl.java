package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.BranchingNode;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.building.BranchingNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.NodeBuilder;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;

public abstract class BranchingNodeConfiguratorImpl<S extends BranchingNodeConfigurator<S, N>, N extends BranchingNode>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		BranchingNodeConfigurator<S, N> {
	protected static abstract class BranchingNodeImpl extends SchemaNodeImpl
			implements BranchingNode {
		private final List<SchemaNode> children;

		public BranchingNodeImpl(String id, List<SchemaNode> children) {
			super(id);
			this.children = Collections.unmodifiableList(children);
		}

		@Override
		public final List<SchemaNode> getChildren() {
			return children;
		}
	}

	private final List<SchemaNode> children;

	public BranchingNodeConfiguratorImpl(NodeBuilderContext context) {
		super(context);
		children = new ArrayList<>();
	}

	@Override
	public NodeBuilder addChild() {
		super.assertBranchable();
		prepare();

		return getContext().childBuilder();
	}

	@Override
	public BranchingNodeConfigurator<S, N> addChild(
			Function<NodeBuilder, SchemaNodeConfigurator<?, ?>> builder) {
		super.assertBranchable();
		prepare();

		builder.apply(getContext().childBuilder()).create();

		return this;
	}

	@Override
	protected void assertConfigurable() {
		super.assertConfigurable();
		if (!children.isEmpty() || !getContext().isConfiguratorActive(this))
			throw new InvalidBuildStateException(this);
	}

	protected final List<SchemaNode> getChildren() {
		return Collections.unmodifiableList(children);
	}
}
