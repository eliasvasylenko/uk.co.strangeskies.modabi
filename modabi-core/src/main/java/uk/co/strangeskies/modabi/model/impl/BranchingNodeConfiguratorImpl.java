package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.BranchingNode;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.SequenceNode;
import uk.co.strangeskies.modabi.model.building.BranchingNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ContentNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.NodeBuilder;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SimpleElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.impl.SequenceNodeConfiguratorImpl.SequenceNodeImpl;

abstract class BranchingNodeConfiguratorImpl<S extends BranchingNodeConfigurator<S, N>, N extends BranchingNode>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		BranchingNodeConfigurator<S, N> {
	protected static abstract class BranchingNodeImpl extends SchemaNodeImpl
			implements BranchingNode {
		private final List<SchemaNode> children;

		public BranchingNodeImpl(BranchingNodeConfiguratorImpl<?, ?> configurator) {
			super(configurator);
			configurator.assertUnblocked();

			this.children = configurator.children;
		}

		public BranchingNodeImpl(SequenceNodeImpl node, SequenceNode overriddenNode) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public final List<SchemaNode> getChildren() {
			return children;
		}
	}

	private final List<SchemaNode> children;
	private boolean blocked;
	private final List<SchemaNode> overriddenChildren;
	private final List<SchemaNode> inheritedChildren;

	public BranchingNodeConfiguratorImpl(
			BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
		children = new ArrayList<>();
		overriddenChildren = new ArrayList<>();
		inheritedChildren = new ArrayList<>();
	}

	@Override
	protected void finaliseProperties() {
		super.finaliseProperties();

		// TODO merge overriddenChildren && inheritedChildren
	}

	protected void inheritChildren(List<SchemaNode> children) {
		requireConfigurable();
		inheritedChildren.addAll(children);
	}

	@Override
	public NodeBuilder addChild() {
		return childBuilder();
	}

	@Override
	public BranchingNodeConfigurator<S, N> addChild(
			Function<NodeBuilder, SchemaNodeConfigurator<?, ?>> builder) {
		builder.apply(childBuilder()).create();

		return this;
	}

	void addChild(SchemaNodeImpl created) {
		blocked = false;
		children.add(created);
	}

	public NodeBuilder childBuilder() {
		assertUnblocked();
		finaliseProperties();
		blocked = true;

		return new NodeBuilder() {
			@Override
			public SimpleElementNodeConfigurator<Object> simpleElement() {
				return new SimpleElementNodeConfiguratorImpl<>(
						BranchingNodeConfiguratorImpl.this);
			}

			@Override
			public SequenceNodeConfigurator sequence() {
				return new SequenceNodeConfiguratorImpl(
						BranchingNodeConfiguratorImpl.this);
			}

			@Override
			public PropertyNodeConfigurator<Object> property() {
				return new PropertyNodeConfiguratorImpl<>(
						BranchingNodeConfiguratorImpl.this);
			}

			@Override
			public ElementNodeConfigurator<Object> element() {
				return new ElementNodeConfiguratorImpl<>(
						BranchingNodeConfiguratorImpl.this);
			}

			@Override
			public ContentNodeConfigurator<Object> content() {
				return new ContentNodeConfiguratorImpl<>(
						BranchingNodeConfiguratorImpl.this);
			}

			@Override
			public ChoiceNodeConfigurator choice() {
				return new ChoiceNodeConfiguratorImpl(
						BranchingNodeConfiguratorImpl.this);
			}
		};
	}

	private void assertUnblocked() {
		if (blocked)
			throw new InvalidBuildStateException(this);
	}

	@SuppressWarnings("unchecked")
	public <T extends SchemaNode> T overrideChild(String id, Class<T> nodeClass) {
		SchemaNode overriddenNode = inheritedChildren.remove(id);
		if (overriddenNode != null
				&& !nodeClass.isAssignableFrom(overriddenNode.getClass()))
			throw new InvalidBuildStateException(this);

		return (T) overriddenNode;
	}

	protected final List<SchemaNode> getChildren() {
		return children;
	}
}
