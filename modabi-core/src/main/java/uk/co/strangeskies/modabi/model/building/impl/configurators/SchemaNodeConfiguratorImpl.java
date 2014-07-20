package uk.co.strangeskies.modabi.model.building.impl.configurators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import uk.co.strangeskies.gears.utilities.collection.HashSetMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.SetMultiMap;
import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaException;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode, C extends ChildNode, B extends BindingChildNode<?>>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N> {
	public static abstract class SchemaNodeImpl implements SchemaNode {
		private final String id;
		private final List<ChildNode> children;

		protected SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, ?, ?, ?> configurator) {
			configurator.finaliseProperties();

			id = configurator.getId();

			for (Set<? extends ChildNode> namedChildren : configurator.namedInheritedChildren
					.values())
				if (namedChildren.size() > 1) {
					Iterator<? extends ChildNode> i = namedChildren.iterator();
					throw new SchemaException(
							i.next().equals(i.next())
									+ " "
									+ "Node '"
									+ namedChildren.iterator().next().getId()
									+ "' is inherited multiple times and must be explicitly overridden.");
				}

			children = new ArrayList<>(configurator.children);
		}

		protected SchemaNodeImpl(SchemaNode node,
				Collection<? extends SchemaNode> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			OverrideMerge<SchemaNode> overrideMerge = new OverrideMerge<>(node,
					overriddenNodes);

			id = overrideMerge.getValue(n -> n.getId());

			children = effectiveChildren;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof SchemaNode))
				return false;

			SchemaNode other = (SchemaNode) obj;
			return Objects.equals(id, other.getId())
					&& Objects.equals(children, other.getChildren());
		}

		@Override
		public final String getId() {
			return id;
		}

		@Override
		public final List<ChildNode> getChildren() {
			return children;
		}
	}

	private final List<ChildNode> children;
	private final List<ChildNode> effectiveChildren;
	private boolean blocked;
	private final SetMultiMap<String, ChildNode> namedInheritedChildren;
	private final List<ChildNode> inheritedChildren;

	private boolean finalisedProperties;

	private String id;

	public SchemaNodeConfiguratorImpl() {
		finalisedProperties = false;

		children = new ArrayList<>();
		effectiveChildren = new ArrayList<>();
		inheritedChildren = new ArrayList<>();
		namedInheritedChildren = new HashSetMultiHashMap<>();
	}

	protected abstract N getEffective(N node);

	protected final void requireConfigurable(Object object) {
		requireConfigurable();
		if (object != null)
			throw new InvalidBuildStateException(this);
	}

	protected final void requireConfigurable() {
		if (finalisedProperties)
			throw new InvalidBuildStateException(this);
	}

	protected void finaliseProperties() {
		finalisedProperties = true;
	}

	public boolean isFinalisedProperties() {
		return finalisedProperties;
	}

	@SuppressWarnings("unchecked")
	protected final S getThis() {
		return (S) this;
	}

	@Override
	public final S id(String id) {
		requireConfigurable(this.id);
		this.id = id;

		return getThis();
	}

	protected abstract Class<N> getNodeClass();

	protected final String getId() {
		return id;
	}

	protected abstract Class<?> getCurrentChildInputTargetClass();

	protected abstract Class<?> getCurrentChildOutputTargetClass();

	protected void inheritChildren(List<? extends ChildNode> nodes) {
		inheritChildren(inheritedChildren.size(), nodes);
	}

	protected void inheritChildren(int index, List<? extends ChildNode> nodes) {
		requireConfigurable();
		inheritNamedChildren(nodes);
		inheritedChildren.addAll(index, nodes);
	}

	private void inheritNamedChildren(List<? extends ChildNode> nodes) {
		nodes.stream().filter(c -> c.getId() != null)
				.forEach(c -> namedInheritedChildren.add(c.getId(), c));
	}

	@SuppressWarnings("unchecked")
	<T extends ChildNode> Set<T> overrideChild(String id, Class<T> nodeClass) {
		Set<ChildNode> overriddenNodes = namedInheritedChildren.get(id);

		if (overriddenNodes != null) {
			if (overriddenNodes.stream().anyMatch(
					n -> !nodeClass.isAssignableFrom(n.getClass())))
				throw new InvalidBuildStateException(this);
		} else
			overriddenNodes = new HashSet<>();

		return (Set<T>) Collections.unmodifiableSet(overriddenNodes);
	}

	protected final List<ChildNode> getChildren() {
		return children;
	}

	protected final List<ChildNode> getEffectiveChildren() {
		List<ChildNode> effectiveChildren = new ArrayList<>();
		effectiveChildren.addAll(inheritedChildren);
		effectiveChildren.addAll(this.effectiveChildren);
		return effectiveChildren;
	}

	void addChild(ChildNode result, ChildNode effective) {
		blocked = false;
		children.add(result);
		effectiveChildren.add(effective);
		if (result.getId() != null) {
			Set<ChildNode> removed = namedInheritedChildren.remove(result.getId());
			if (removed != null)
				inheritedChildren.removeAll(removed);
		}
	}

	protected void assertUnblocked() {
		if (blocked)
			throw new InvalidBuildStateException(this);
	}

	protected abstract DataLoader getDataLoader();

	protected ChildBuilder<C, B> childBuilder() {
		assertUnblocked();
		finaliseProperties();
		blocked = true;

		SchemaNodeConfigurationContext<ChildNode> context = new SchemaNodeConfigurationContext<ChildNode>() {
			@Override
			public DataLoader getDataLoader() {
				return SchemaNodeConfiguratorImpl.this.getDataLoader();
			}

			@Override
			public <T extends ChildNode> Set<T> overrideChild(String id,
					Class<T> nodeClass) {
				return SchemaNodeConfiguratorImpl.this.overrideChild(id, nodeClass);
			}

			@Override
			public Class<?> getCurrentChildOutputTargetClass() {
				return SchemaNodeConfiguratorImpl.this
						.getCurrentChildOutputTargetClass();
			}

			@Override
			public Class<?> getCurrentChildInputTargetClass() {
				return SchemaNodeConfiguratorImpl.this
						.getCurrentChildInputTargetClass();
			}

			@Override
			public void addChild(ChildNode result, ChildNode effective) {
				SchemaNodeConfiguratorImpl.this.addChild(result, effective);
			}
		};

		return new ChildBuilder<C, B>() {
			@Override
			public SequenceNodeConfigurator<C, B> sequence() {
				return new SequenceNodeConfiguratorImpl<C, B>(context);
			}

			@Override
			public ChoiceNodeConfigurator<C, B> choice() {
				return new ChoiceNodeConfiguratorImpl<>(context);
			}

			@Override
			public ElementNodeConfigurator<Object> element() {
				return new ElementNodeConfiguratorImpl<>(context);
			}

			@Override
			public DataNodeConfigurator<Object> data() {
				return new DataNodeConfiguratorImpl<>(context);
			}

			@Override
			public InputSequenceNodeConfigurator<B> inputSequence() {
				return new InputSequenceNodeConfiguratorImpl<>(context);
			}
		};
	}
}
