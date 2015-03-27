/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.ChoiceNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.ChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.ChildrenContainer;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.utilities.PropertySet;
import uk.co.strangeskies.utilities.factory.Configurator;
import uk.co.strangeskies.utilities.factory.InvalidBuildStateException;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<?, ?>>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N> {
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
					OverrideMerge<S, ? extends SchemaNodeConfiguratorImpl<?, S>> overrideMerge) {
				source = overrideMerge.node().source();

				name = overrideMerge.getValue(SchemaNode::getName, (n, o) -> true,
						defaultName(overrideMerge));

				isAbstract = overrideMerge.node().isAbstract() == null ? false
						: overrideMerge.node().isAbstract();

				children = overrideMerge.configurator().getChildrenContainer()
						.getEffectiveChildren();

				if (!overrideMerge.configurator().isChildContextAbstract())
					requireNonAbstractDescendents(new ArrayDeque<>(Arrays.asList(this)));
			}

			protected QualifiedName defaultName(
					OverrideMerge<S, ? extends SchemaNodeConfiguratorImpl<?, S>> overrideMerge) {
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
						public <U> void accept(ComplexNode.Effective<U> node) {
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

		protected SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, ?> configurator) {
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
			return effective().getName().toString();
		}
	}

	private ChildrenConfigurator childrenConfigurator;
	private ChildrenContainer childrenContainer;

	private boolean finalised;

	private QualifiedName name;
	private Boolean isAbstract;

	public SchemaNodeConfiguratorImpl() {
		finalised = false;
	}

	protected final void assertConfigurable(Object object) {
		assertConfigurable();
		if (object != null)
			throw new InvalidBuildStateException(this);
	}

	protected final void assertConfigurable() {
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

	public ChildrenConfigurator getChildrenConfigurator() {
		return childrenConfigurator;
	}

	@SuppressWarnings("unchecked")
	protected final S getThis() {
		return (S) this;
	}

	@Override
	public final S name(QualifiedName name) {
		assertConfigurable(this.name);
		this.name = name;

		return getThis();
	}

	@Override
	public final S isAbstract(boolean isAbstract) {
		assertConfigurable(this.isAbstract);
		this.isAbstract = isAbstract;

		return getThis();
	}

	protected abstract TypeLiteral<N> getNodeClass();

	protected abstract DataLoader getDataLoader();

	protected abstract Namespace getNamespace();

	public abstract List<N> getOverriddenNodes();

	protected final QualifiedName getName() {
		return name;
	}

	protected abstract ChildrenConfigurator createChildrenConfigurator();

	@Override
	public ChildBuilder addChild() {
		finaliseConfiguration();

		return childrenConfigurator.addChild();
	}

	protected static <S extends SchemaNode<S, ?>, C extends SchemaNodeConfiguratorImpl<?, ? extends S>> OverrideMerge<S, C> overrideMerge(
			S node, C configurator) {
		return new OverrideMerge<S, C>(node, configurator);
	}

	protected boolean isChildContextAbstract() {
		return isAbstract != null && isAbstract;
	}

	@Override
	public String toString() {
		return getNodeClass().getRawType().getSimpleName() + " configurator: "
				+ getName();
	}
}
