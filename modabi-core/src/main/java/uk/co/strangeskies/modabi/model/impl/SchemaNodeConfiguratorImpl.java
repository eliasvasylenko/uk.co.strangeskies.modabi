package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;

abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N> {
	protected static abstract class SchemaNodeImpl implements SchemaNode {
		private final String id;

		SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, ?> configurator) {
			configurator.finaliseProperties();

			id = configurator.getId();
		}

		protected SchemaNodeImpl(SchemaNode node,
				Collection<? extends SchemaNode> overriddenNodes) {
			id = getValue(node, overriddenNodes, n -> n.getId(), (v, o) -> true);
		}

		protected static <E extends SchemaNode, T> T getValue(E node,
				Collection<? extends E> overriddenNodes, Function<E, T> valueFunction) {
			return getValue(node, overriddenNodes, valueFunction, (v, o) -> true);
		}

		protected static <E extends SchemaNode, T> T getValue(E node,
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

		@Override
		public final String getId() {
			return id;
		}
	}

	private final BranchingNodeConfiguratorImpl<?, ?> parent;
	private boolean finalisedProperties;

	private String id;

	public SchemaNodeConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
		this.parent = parent;
		finalisedProperties = false;

		if (parent != null)
			addResultListener(result -> parent.addChild((SchemaNodeImpl) result,
					getEffective(result)));
	}

	protected abstract N getEffective(N node);

	protected final BranchingNodeConfiguratorImpl<?, ?> parent() {
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

	protected List<N> getOverriddenNodes() {
		return parent == null || id == null ? new ArrayList<>() : parent
				.overrideChild(id, getNodeClass());
	}

	public abstract Class<N> getNodeClass();

	protected final String getId() {
		return id;
	}
}
