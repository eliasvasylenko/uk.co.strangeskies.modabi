package uk.co.strangeskies.modabi.model.impl;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;

abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N> {
	protected static abstract class SchemaNodeImpl implements SchemaNode {
		private final String id;

		SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, ?> configurator) {
			configurator.finaliseProperties();

			id = configurator.id;
		}

		SchemaNodeImpl(SchemaNode node,
				Collection<? extends SchemaNode> overriddenNodes) {
			if (!overriddenNodes.stream().allMatch(
					n -> node.getId().equals(n.getId())))
				throw new AssertionError();
			id = node.getId();
		}

		protected static final <E extends SchemaNode, T> T getValue(E node,
				Collection<? extends E> overriddenNodes, Function<E, T> valueFunction) {
			return getValue(node, overriddenNodes, valueFunction, (v, o) -> true);
		}

		protected static final <E extends SchemaNode, T> T getValue(E node,
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
				if (value != null && !validateOverride.test(value, overriddenValue))
					throw new SchemaException();
				return overriddenValue;
			} else if (value == null
					|| !values.stream().allMatch(v -> validateOverride.test(value, v)))
				throw new SchemaException();
			return value;
		}

		@Override
		public final String getId() {
			return id;
		}

		protected abstract void validateEffectiveModel();
	}

	private final BranchingNodeConfiguratorImpl<?, ?> parent;
	private boolean finalisedProperties;

	private String id;
	private N overriddenNode;

	public SchemaNodeConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
		this.parent = parent;
		finalisedProperties = false;

		addResultListener(created -> {
			if (parent != null)
				parent.addChild((SchemaNodeImpl) created);
		});
	}

	protected final BranchingNodeConfiguratorImpl<?, ?> getParent() {
		return parent;
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

	protected void requireHasId() {
		if (id == null || id == "")
			throw new IllegalArgumentException();
	}

	protected final void finaliseProperties() {
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

		if (parent != null)
			setOverriddenNode(parent.overrideChild(id, getNodeClass()));

		return getThis();
	}

	protected void setOverriddenNode(N node) {
		overriddenNode = node;
	}

	public N getOverriddenNode() {
		return overriddenNode;
	}

	public abstract Class<N> getNodeClass();

	protected final String getId() {
		return id;
	}
}
