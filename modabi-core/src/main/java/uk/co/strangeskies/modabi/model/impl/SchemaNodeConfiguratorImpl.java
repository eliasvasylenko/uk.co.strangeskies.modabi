package uk.co.strangeskies.modabi.model.impl;

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

		SchemaNodeImpl(SchemaNodeImpl node, SchemaNode overriddenNode) {
			if (node.id != overriddenNode.getId())
				throw new AssertionError();
			id = node.id;
		}

		@Override
		public final String getId() {
			return id;
		}

		protected abstract SchemaNodeImpl effectiveModel();
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

	protected final void assertConfigurable(Object object) {
		if (object != null || finalisedProperties)
			throw new InvalidBuildStateException(this);
	}

	protected void assertHasId() {
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
		assertConfigurable(this.id);
		this.id = id;

		BranchingNodeConfiguratorImpl<?, ?> parent = getParent();
		if (parent != null) {
			N override = parent.getOverriddenChild(id, getNodeClass());

			if (override != null)
				overriddenNode = override;
		}

		return getThis();
	}

	public N getOverriddenNode() {
		return overriddenNode;
	}

	public abstract Class<N> getNodeClass();

	protected final String getId() {
		return id;
	}
}
