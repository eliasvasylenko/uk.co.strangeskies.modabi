package uk.co.strangeskies.modabi.schema.model.building.configurators.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.ChildrenContainer;
import uk.co.strangeskies.modabi.schema.model.building.impl.OverrideMerge;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.utilities.PropertySet;
import uk.co.strangeskies.utilities.factory.Configurator;
import uk.co.strangeskies.utilities.factory.InvalidBuildStateException;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N, C, B>, N extends SchemaNode<?, ?>, C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N, C, B> {
	public static abstract class SchemaNodeImpl<S extends SchemaNode<S, E>, E extends SchemaNode.Effective<S, E>>
			implements SchemaNode<S, E> {
		protected static abstract class Effective<S extends SchemaNode<S, E>, E extends SchemaNode.Effective<S, E>>
				implements SchemaNode.Effective<S, E> {
			private final S source;

			private final QualifiedName name;
			private final boolean isAbstract;
			private final List<ChildNode.Effective<?, ?>> children;

			private PropertySet<E> combinedPropertySet;

			protected Effective(
					OverrideMerge<S, ? extends SchemaNodeConfiguratorImpl<?, ?, ?, ?>> overrideMerge) {
				source = overrideMerge.node().source();

				name = overrideMerge.getValue(SchemaNode::getName, (n, o) -> true,
						defaultName());

				isAbstract = overrideMerge.node().isAbstract() == null ? false
						: overrideMerge.node().isAbstract();

				children = overrideMerge.configurator().getChildrenContainer()
						.getEffectiveChildren();

				if (!overrideMerge.configurator().isChildContextAbstract())
					requireNonAbstractDescendents(new ArrayDeque<>(Arrays.asList(this)));
			}

			protected QualifiedName defaultName() {
				return null;
			}

			// TODO more sensible error message & get rid of 'instanceof' temp hack
			protected void requireNonAbstractDescendents(
					Deque<SchemaNode.Effective<?, ?>> nodeStack) {
				for (ChildNode.Effective<?, ?> child : nodeStack.peek().children()) {
					nodeStack.push(child);

					if (child.isAbstract())
						throw new SchemaException("Inherited descendent '"
								+ nodeStack.stream().map(n -> n.getName().toString())
										.collect(Collectors.joining(" < "))
								+ "' cannot be abstract.");

					child.process(new SchemaProcessingContext() {
						@Override
						public void accept(ChoiceNode.Effective node) {
							requireNonAbstractDescendents(nodeStack);
						}

						@Override
						public void accept(SequenceNode.Effective node) {
							requireNonAbstractDescendents(nodeStack);
						}

						@Override
						public void accept(InputSequenceNode.Effective node) {
							requireNonAbstractDescendents(nodeStack);
						}

						@Override
						public <U> void accept(DataNode.Effective<U> node) {
							if (!node.isExtensible())
								requireNonAbstractDescendents(nodeStack);
						}

						@Override
						public <U> void accept(ElementNode.Effective<U> node) {
							if (!node.isExtensible())
								requireNonAbstractDescendents(nodeStack);
						}
					});

					nodeStack.pop();
				}
			}

			@Override
			public S source() {
				return source;
			}

			@Override
			public QualifiedName getName() {
				return name;
			}

			@Override
			public Boolean isAbstract() {
				return isAbstract;
			}

			@Override
			public List<ChildNode.Effective<?, ?>> children() {
				return children;
			}

			@Override
			public final boolean equals(Object object) {
				return combinedPropertySet().testEquality(object);
			}

			@Override
			public final int hashCode() {
				return combinedPropertySet().generateHashCode();
			}

			private final PropertySet<E> combinedPropertySet() {
				if (combinedPropertySet == null)
					combinedPropertySet = new PropertySet<>(getEffectiveClass(),
							propertySet(), true).add(effectivePropertySet());
				return combinedPropertySet;
			}

			@Override
			public String toString() {
				return getName() != null ? getName().toString() : "[Unnamed Node]";
			}
		}

		private final QualifiedName name;
		private final Boolean isAbstract;
		private final List<ChildNode<?, ?>> children;

		private PropertySet<S> propertySet;

		protected SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, ?, ?, ?> configurator) {
			configurator.finaliseConfiguration();
			configurator.finaliseChildren();

			name = configurator.name;

			isAbstract = configurator.isAbstract;

			children = Collections.unmodifiableList(new ArrayList<>(configurator
					.getChildrenContainer().getChildren()));
		}

		@Override
		public final QualifiedName getName() {
			return name;
		}

		@Override
		public Boolean isAbstract() {
			return isAbstract;
		}

		@Override
		public final List<? extends ChildNode<?, ?>> children() {
			return children;
		}

		@Override
		public final boolean equals(Object object) {
			if (propertySet == null)
				propertySet = propertySet();
			return propertySet.testEquality(object)
					&& effective().equals(((SchemaNode<?, ?>) object).effective());
		}

		@Override
		public final int hashCode() {
			if (propertySet == null)
				propertySet = propertySet();
			return propertySet.generateHashCode();
		}

		@Override
		public String toString() {
			return getName().toString();
		}
	}

	private ChildrenConfigurator<C, B> childrenConfigurator;
	private ChildrenContainer childrenContainer;

	private boolean finalised;

	private QualifiedName name;
	private Boolean isAbstract;

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
	public final S name(QualifiedName name) {
		requireConfigurable(this.name);
		this.name = name;

		return getThis();
	}

	@Override
	public final S isAbstract(boolean isAbstract) {
		requireConfigurable(this.isAbstract);
		this.isAbstract = isAbstract;

		return getThis();
	}

	protected abstract Class<N> getNodeClass();

	protected abstract DataLoader getDataLoader();

	protected abstract Namespace getNamespace();

	public abstract LinkedHashSet<N> getOverriddenNodes();

	protected final QualifiedName getName() {
		return name;
	}

	protected abstract ChildrenConfigurator<C, B> createChildrenConfigurator();

	@Override
	public ChildBuilder<C, B> addChild() {
		finaliseConfiguration();

		return childrenConfigurator.addChild();
	}

	protected static <S extends SchemaNode<S, ?>, C extends SchemaNodeConfiguratorImpl<?, ? extends S, ?, ?>> OverrideMerge<S, C> overrideMerge(
			S node, C configurator) {
		return new OverrideMerge<S, C>(node, configurator);
	}

	protected boolean isChildContextAbstract() {
		return isAbstract != null && isAbstract;
	}
}
