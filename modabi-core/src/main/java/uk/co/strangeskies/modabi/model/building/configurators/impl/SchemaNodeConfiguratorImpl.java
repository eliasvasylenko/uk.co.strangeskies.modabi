package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ChildrenConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.ChildrenContainer;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<?>, C extends ChildNode<?>, B extends BindingChildNode<?, ?>>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N> {
	public static abstract class SchemaNodeImpl<E extends SchemaNode.Effective<E>>
			implements SchemaNode<E> {
		protected static abstract class Effective<E extends SchemaNode.Effective<E>>
				implements SchemaNode.Effective<E> {
			private final String name;
			private final List<ChildNode.Effective<?>> children;

			protected Effective(
					OverrideMerge<? extends SchemaNode<?>, ? extends SchemaNodeConfiguratorImpl<?, ?, ?, ?>> overrideMerge) {
				name = overrideMerge.getValue(SchemaNode::getName);

				children = overrideMerge.configurator().getChildrenContainer()
						.getEffectiveChildren();
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<? extends ChildNode.Effective<?>> children() {
				return children;
			}

			/*
			 * @Override public final boolean equals(Object obj) { return
			 * equalsImpl(obj); }
			 */

			@Override
			public int hashCode() {
				return hashCodeImpl();
			}
		}

		private final String name;
		private final List<ChildNode<?>> children;

		protected SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, ?, ?, ?> configurator) {
			configurator.finaliseConfiguration();
			configurator.finaliseChildren();

			name = configurator.getId();

			children = Collections.unmodifiableList(new ArrayList<>(configurator
					.getChildrenContainer().getChildren()));
		}

		@Override
		public final String getName() {
			return name;
		}

		@Override
		public final List<? extends ChildNode<?>> children() {
			return children;
		}

		/*
		 * @Override public final boolean equals(Object obj) { return
		 * equalsImpl(obj); }
		 */

		@Override
		public int hashCode() {
			return hashCodeImpl();
		}
	}

	private ChildrenConfigurator<C, B> childrenConfigurator;
	private ChildrenContainer childrenContainer;

	private boolean finalised;

	private String name;

	public SchemaNodeConfiguratorImpl() {
		finalised = false;
	}

	protected final void requireConfigurable(Object object) {
		requireConfigurable();
		if (object != null)
			throw new InvalidBuildStateException(this);
	}

	protected final void requireConfigurable() {
		if (finalised)
			throw new InvalidBuildStateException(this);
	}

	private final void finaliseConfiguration() {
		finalised = true;

		if (childrenConfigurator == null)
			childrenConfigurator = createChildrenConfigurator();
	}

	public void finaliseChildren() {
		if (childrenContainer == null)
			childrenContainer = childrenConfigurator.create();
	}

	public ChildrenContainer getChildrenContainer() {
		return childrenContainer;
	}

	public ChildrenConfigurator<C, B> getChildrenConfigurator() {
		return childrenConfigurator;
	}

	@SuppressWarnings("unchecked")
	protected final S getThis() {
		return (S) this;
	}

	@Override
	public final S name(String name) {
		requireConfigurable(this.name);
		this.name = name;

		return getThis();
	}

	protected abstract Class<N> getNodeClass();

	protected abstract DataLoader getDataLoader();

	protected abstract boolean isAbstract();

	protected abstract LinkedHashSet<N> getOverriddenNodes();

	protected final String getId() {
		return name;
	}

	protected abstract ChildrenConfigurator<C, B> createChildrenConfigurator();

	protected ChildBuilder<C, B> addChild() {
		finaliseConfiguration();

		return childrenConfigurator.addChild();
	}

	protected static <E extends SchemaNode<? extends E>, C extends SchemaNodeConfiguratorImpl<?, ? extends E, ?, ?>> OverrideMerge<E, C> overrideMerge(
			E node, C configurator) {
		return new OverrideMerge<E, C>(node, configurator,
				c -> c.getOverriddenNodes());
	}
}
