package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.impl.Children;
import uk.co.strangeskies.modabi.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaException;

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

				children = overrideMerge.configurator().getChildren()
						.getEffectiveChildren();

				Set<String> names = new HashSet<>();
				for (ChildNode<?> child : children)
					if (!names.add(child.effective().getName())) {
						throw new SchemaException("Node '" + child.effective().getName()
								+ "' is present multiple times in '" + name + "'.");
					}
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<? extends ChildNode.Effective<?>> children() {
				return children;
			}

			/*@Override
			public final boolean equals(Object obj) {
				return equalsImpl(obj);
			}*/

			@Override
			public int hashCode() {
				return hashCodeImpl();
			}
		}

		private final String name;
		private final List<ChildNode<?>> children;

		protected SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, ?, ?, ?> configurator) {
			configurator.finaliseProperties();

			name = configurator.getId();

			children = Collections.unmodifiableList(new ArrayList<>(
					configurator.children.getChildren()));
		}

		@Override
		public final String getName() {
			return name;
		}

		@Override
		public final List<? extends ChildNode<?>> children() {
			return children;
		}

		/*@Override
		public final boolean equals(Object obj) {
			return equalsImpl(obj);
		}*/

		@Override
		public int hashCode() {
			return hashCodeImpl();
		}
	}

	private final Children<C, B> children;

	private boolean finalisedProperties;

	private String name;

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

	public final boolean isFinalisedProperties() {
		return finalisedProperties;
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

	protected abstract Set<N> getOverriddenNodes();

	protected final String getId() {
		return name;
	}

	protected abstract Class<?> getCurrentChildInputTargetClass();

	protected abstract Class<?> getCurrentChildOutputTargetClass();

	protected abstract DataLoader getDataLoader();

	protected ChildBuilder<C, B> addChild() {
		children.assertUnblocked();
		finaliseProperties();

		return children.addChild(getDataLoader(),
				getCurrentChildInputTargetClass(), getCurrentChildOutputTargetClass(),
				isAbstract());
	}

	protected abstract boolean isAbstract();

	protected static <E extends SchemaNode<? extends E>, C extends SchemaNodeConfiguratorImpl<?, ? extends E, ?, ?>> OverrideMerge<E, C> overrideMerge(
			E node, C configurator) {
		return new OverrideMerge<E, C>(node, configurator,
				c -> c.getOverriddenNodes());
	}
}
