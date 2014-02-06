package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;

abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N> {
	protected static abstract class SchemaNodeImpl<E extends SchemaNode>
			implements SchemaNode {
		private final String id;

		SchemaNodeImpl(SchemaNodeConfiguratorImpl<?, ?> configurator) {
			configurator.finaliseProperties();

			id = configurator.id;
		}

		SchemaNodeImpl(SchemaNode node, SchemaNode overriddenNode) {
			if (node.getId() != overriddenNode.getId())
				throw new AssertionError();
			id = node.getId();
		}

		@Override
		public final String getId() {
			return id;
		}

		protected abstract void validateEffectiveModel();

		protected abstract SchemaNodeImpl<E> override(E node);
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
				parent.addChild((SchemaNodeImpl<?>) created);
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
