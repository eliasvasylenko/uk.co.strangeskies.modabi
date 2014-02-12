package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.collection.ArrayListMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.ListMultiMap;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.BranchingNode;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.building.BranchingNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ContentNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.NodeBuilder;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SimpleElementNodeConfigurator;

abstract class BranchingNodeConfiguratorImpl<S extends BranchingNodeConfigurator<S, N>, N extends BranchingNode>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		BranchingNodeConfigurator<S, N> {
	protected static abstract class BranchingNodeImpl extends SchemaNodeImpl
			implements BranchingNode {
		private final List<SchemaNode> children;

		public BranchingNodeImpl(BranchingNodeConfiguratorImpl<?, ?> configurator) {
			super(configurator);
			configurator.assertUnblocked();

			this.children = new ArrayList<>(configurator.children);
		}

		public <E extends BranchingNode> BranchingNodeImpl(E node,
				List<? extends E> overriddenNodes) {
			super(node, overriddenNodes);

			children = new ArrayList<>();
			overriddenNodes.forEach(n -> children.addAll(n.getChildren()));
			children.addAll(node.getChildren());
		}

		@Override
		public final List<SchemaNode> getChildren() {
			return children;
		}
	}

	private final List<SchemaNodeImpl> children;
	private boolean blocked;
	private final ListMultiMap<String, SchemaNode> namedInheritedChildren;
	private final List<SchemaNode> inheritedChildren;

	public BranchingNodeConfiguratorImpl(
			BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
		children = new ArrayList<>();
		inheritedChildren = new ArrayList<>();
		namedInheritedChildren = new ArrayListMultiHashMap<>();
	}

	@Override
	protected void finaliseProperties() {
		super.finaliseProperties();

		List<SchemaNode> newInheritedChildren = new ArrayList<>();
		getOverriddenNodes().forEach(n -> {
			inheritNamedChildren(n.getChildren());
			newInheritedChildren.addAll(n.getChildren());
		});
		inheritedChildren.addAll(0, newInheritedChildren);
	}

	protected void inheritChildren(List<SchemaNode> nodes) {
		requireConfigurable();
		inheritNamedChildren(nodes);
		inheritedChildren.addAll(nodes);
	}

	private void inheritNamedChildren(List<? extends SchemaNode> nodes) {
		nodes.stream().filter(c -> c.getId() != null)
				.forEach(c -> namedInheritedChildren.add(c.getId(), c));
	}

	@SuppressWarnings("unchecked")
	<T extends SchemaNode> List<T> overrideChild(String id, Class<T> nodeClass) {
		List<SchemaNode> overriddenNodes = Collections
				.unmodifiableList(getOverriddenNodes());

		if (overriddenNodes != null) {
			if (overriddenNodes.stream().anyMatch(
					n -> !nodeClass.isAssignableFrom(n.getClass())))
				throw new InvalidBuildStateException(this);
		} else
			overriddenNodes = new ArrayList<>();

		return (List<T>) overriddenNodes;
	}

	protected final List<SchemaNodeImpl> getChildren() {
		return children;
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
		if (created.getId() != null)
			inheritedChildren
					.removeAll(namedInheritedChildren.remove(created.getId()));

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
}
