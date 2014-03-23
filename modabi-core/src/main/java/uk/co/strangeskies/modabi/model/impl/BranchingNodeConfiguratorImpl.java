package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.gears.utilities.collection.ArrayListMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.ListMultiMap;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.model.building.BranchingNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ContentNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.NodeBuilder;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SimpleElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.BranchingNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public abstract class BranchingNodeConfiguratorImpl<S extends BranchingNodeConfigurator<S, N>, N extends BranchingNode>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		BranchingNodeConfigurator<S, N> {
	protected static abstract class BranchingNodeImpl extends SchemaNodeImpl
			implements BranchingNode {
		private final List<SchemaNode> children;

		public BranchingNodeImpl(BranchingNodeConfiguratorImpl<?, ?> configurator) {
			super(configurator);
			configurator.assertUnblocked();

			for (List<SchemaNode> namedChildren : configurator.namedInheritedChildren
					.values())
				if (namedChildren.size() > 1)
					throw new SchemaException(
							"Node '"
									+ namedChildren.get(0).getId()
									+ "' is inherited multiple times and must be explicitly overridden.");

			this.children = new ArrayList<>(configurator.children);
		}

		public BranchingNodeImpl(BranchingNode node,
				Collection<? extends BranchingNode> overriddenNodes,
				List<SchemaNode> effectiveChildren) {
			super(node, overriddenNodes);

			children = effectiveChildren;
		}

		@Override
		public final List<SchemaNode> getChildren() {
			return children;
		}
	}

	private final List<SchemaNode> children;
	private final List<SchemaNode> effectiveChildren;
	private boolean blocked;
	private final ListMultiMap<String, SchemaNode> namedInheritedChildren;
	private final List<SchemaNode> inheritedChildren;

	private final Class<?> preInputClass;
	private final Class<?> parentClass;

	public BranchingNodeConfiguratorImpl(
			BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
		children = new ArrayList<>();
		effectiveChildren = new ArrayList<>();
		inheritedChildren = new ArrayList<>();
		namedInheritedChildren = new ArrayListMultiHashMap<>();
		preInputClass = parent == null ? null : parent
				.getCurrentChildPreInputClass();
		parentClass = parent == null ? null : parent.getDataClass();
	}

	protected Class<?> getParentClass() {
		return parentClass;
	}

	protected Class<?> getPreInputClass() {
		return preInputClass;
	}

	protected abstract Class<?> getCurrentChildPreInputClass();

	protected Class<?> getDataClass() {
		return parentClass;
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
		List<SchemaNode> overriddenNodes = namedInheritedChildren.get(id);

		if (overriddenNodes != null) {
			if (overriddenNodes.stream().anyMatch(
					n -> !nodeClass.isAssignableFrom(n.getClass())))
				throw new InvalidBuildStateException(this);
		} else
			overriddenNodes = new ArrayList<>();

		return (List<T>) Collections.unmodifiableList(overriddenNodes);
	}

	protected final List<SchemaNode> getChildren() {
		return children;
	}

	protected final List<SchemaNode> getEffectiveChildren() {
		List<SchemaNode> effectiveChildren = new ArrayList<>();
		effectiveChildren.addAll(inheritedChildren);
		effectiveChildren.addAll(this.effectiveChildren);
		return effectiveChildren;
	}

	void addChild(SchemaNode result, SchemaNode effective) {
		blocked = false;
		children.add(result);
		effectiveChildren.add(effective);
		if (result.getId() != null) {
			List<SchemaNode> removed = namedInheritedChildren.remove(result.getId());
			if (removed != null)
				inheritedChildren.removeAll(removed);
		}
	}

	@Override
	public NodeBuilder addChild() {
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
