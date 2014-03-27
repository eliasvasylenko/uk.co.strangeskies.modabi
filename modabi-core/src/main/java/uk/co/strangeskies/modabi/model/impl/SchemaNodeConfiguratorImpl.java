package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.utilities.collection.ArrayListMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.ListMultiMap;
import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N> {
	protected static abstract class SchemaNodeImpl implements SchemaNode {
		private final String id;
		private final List<ChildNode> children;

		SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, ?> configurator) {
			configurator.finaliseProperties();

			id = configurator.getId();

			for (List<ChildNode> namedChildren : configurator.namedInheritedChildren
					.values())
				if (namedChildren.size() > 1)
					throw new SchemaException(
							"Node '"
									+ namedChildren.get(0).getId()
									+ "' is inherited multiple times and must be explicitly overridden.");

			this.children = new ArrayList<>(configurator.children);
		}

		protected SchemaNodeImpl(SchemaNode node,
				Collection<? extends SchemaNode> overriddenNodes,
				List<ChildNode> effectiveChildren) {
			id = getValue(node, overriddenNodes, n -> n.getId(), (v, o) -> true);

			children = effectiveChildren;
		}

		@Override
		public final String getId() {
			return id;
		}

		@Override
		public final List<ChildNode> getChildren() {
			return children;
		}

		protected static <E, T> T getValue(E node,
				Collection<? extends E> overriddenNodes, Function<E, T> valueFunction) {
			return getValue(node, overriddenNodes, valueFunction, (v, o) -> true);
		}

		protected static <E, T> T getValue(E node,
				Collection<? extends E> overriddenNodes, Function<E, T> valueFunction,
				BiPredicate<T, T> validateOverride) {
			T value = valueFunction.apply(node);

			Collection<T> values = overriddenNodes.stream()
					.map(n -> valueFunction.apply(n)).filter(v -> v != null)
					.collect(Collectors.toSet());

			if (values.isEmpty())
				return value;
			else if (values.size() == 1) {
				T overriddenValue = values.iterator().next();
				if (value != null)
					if (!validateOverride.test(value, overriddenValue))
						throw new SchemaException();
					else
						return value;
				return overriddenValue;
			} else if (value == null
					|| !values.stream().allMatch(v -> validateOverride.test(value, v)))
				throw new SchemaException("value: " + value);
			return value;
		}
	}

	private final List<ChildNode> children;
	private final List<ChildNode> effectiveChildren;
	private boolean blocked;
	private final ListMultiMap<String, ChildNode> namedInheritedChildren;
	private final List<ChildNode> inheritedChildren;

	private boolean finalisedProperties;

	private String id;

	public SchemaNodeConfiguratorImpl() {
		finalisedProperties = false;

		children = new ArrayList<>();
		effectiveChildren = new ArrayList<>();
		inheritedChildren = new ArrayList<>();
		namedInheritedChildren = new ArrayListMultiHashMap<>();
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

	protected void inheritChildren(List<ChildNode> nodes) {
		inheritChildren(inheritedChildren.size(), nodes);
	}

	protected void inheritChildren(int index, List<ChildNode> nodes) {
		requireConfigurable();
		inheritNamedChildren(nodes);
		inheritedChildren.addAll(nodes);
	}

	private void inheritNamedChildren(List<? extends ChildNode> nodes) {
		nodes.stream().filter(c -> c.getId() != null)
				.forEach(c -> namedInheritedChildren.add(c.getId(), c));
	}

	@SuppressWarnings("unchecked")
	<T extends ChildNode> List<T> overrideChild(String id, Class<T> nodeClass) {
		List<ChildNode> overriddenNodes = namedInheritedChildren.get(id);

		if (overriddenNodes != null) {
			if (overriddenNodes.stream().anyMatch(
					n -> !nodeClass.isAssignableFrom(n.getClass())))
				throw new InvalidBuildStateException(this);
		} else
			overriddenNodes = new ArrayList<>();

		return (List<T>) Collections.unmodifiableList(overriddenNodes);
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
			List<ChildNode> removed = namedInheritedChildren.remove(result.getId());
			if (removed != null)
				inheritedChildren.removeAll(removed);
		}
	}

	protected void block() {
		blocked = true;
	}

	protected void assertUnblocked() {
		if (blocked)
			throw new InvalidBuildStateException(this);
	}

	protected ChildBuilder addChild() {
		assertUnblocked();
		finaliseProperties();
		block();

		return new ChildBuilder() {
			@Override
			public SequenceNodeConfigurator sequence() {
				return new SequenceNodeConfiguratorImpl(SchemaNodeConfiguratorImpl.this);
			}

			@Override
			public ElementNodeConfigurator<Object> element() {
				return new ElementNodeConfiguratorImpl<>(
						SchemaNodeConfiguratorImpl.this);
			}

			@Override
			public ChoiceNodeConfigurator choice() {
				return new ChoiceNodeConfiguratorImpl(SchemaNodeConfiguratorImpl.this);
			}

			@Override
			public DataNodeConfigurator<Object> data() {
				return new DataNodeConfiguratorImpl<>(SchemaNodeConfiguratorImpl.this);
			}
		};
	}
}
