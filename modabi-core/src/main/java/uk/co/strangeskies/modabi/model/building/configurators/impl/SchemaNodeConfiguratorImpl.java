package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.Children;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaException;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<?>, C extends ChildNode<?>, B extends BindingChildNode<?, ?>>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N> {
	public static abstract class SchemaNodeImpl<E extends SchemaNode.Effective<E>>
			implements SchemaNode<E> {
		protected static class Effective<E extends SchemaNode.Effective<E>>
				implements SchemaNode.Effective<E> {
			private final String id;
			private final List<ChildNode.Effective<?>> children;

			protected Effective(
					OverrideMerge<? extends SchemaNode<?>, ? extends SchemaNodeConfiguratorImpl<?, ?, ?, ?>> overrideMerge) {
				id = overrideMerge.getValue(n -> n.getId());

				children = overrideMerge.configurator().getChildren()
						.getEffectiveChildren();
			}

			@Override
			public String getId() {
				return id;
			}

			@Override
			public List<? extends ChildNode.Effective<?>> getChildren() {
				return children;
			}
		}

		private final String id;
		private final List<ChildNode<?>> children;

		protected SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, ?, ?, ?> configurator) {
			configurator.finaliseProperties();

			id = configurator.getId();

			for (Set<? extends ChildNode<?>> namedChildren : configurator.children
					.getNamedInheritedChildren().values())
				if (namedChildren.size() > 1) {
					Iterator<? extends ChildNode<?>> i = namedChildren.iterator();
					throw new SchemaException(
							i.next().equals(i.next())
									+ " "
									+ "Node '"
									+ namedChildren.iterator().next().getId()
									+ "' is inherited multiple times and must be explicitly overridden.");
				}

			children = Collections.unmodifiableList(new ArrayList<>(
					configurator.children.getChildren()));
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof SchemaNode))
				return false;

			SchemaNode<?> other = (SchemaNode<?>) obj;
			return Objects.equals(id, other.getId())
					&& Objects.equals(children, other.getChildren());
		}

		@Override
		public final String getId() {
			return id;
		}

		@Override
		public final List<? extends ChildNode<?>> getChildren() {
			return children;
		}
	}

	private final Children<C, B> children;

	private boolean finalisedProperties;

	private String id;

	public SchemaNodeConfiguratorImpl() {
		finalisedProperties = false;

		children = new Children<>();
	}

	public Children<C, B> getChildren() {
		return children;
	}

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

	abstract Set<N> getOverriddenNodes();

	protected final String getId() {
		return id;
	}

	protected abstract Class<?> getCurrentChildInputTargetClass();

	protected abstract Class<?> getCurrentChildOutputTargetClass();

	protected abstract DataLoader getDataLoader();

	protected ChildBuilder<C, B> childBuilder() {
		children.assertUnblocked();
		finaliseProperties();

		return children.addChild(getDataLoader(),
				getCurrentChildInputTargetClass(), getCurrentChildOutputTargetClass());
	}
}
