package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N> {
	protected static abstract class SchemaNodeImpl implements SchemaNode {
		private final String id;

		public SchemaNodeImpl(String id) {
			this.id = id;
		}

		@Override
		public final String getId() {
			return id;
		}
	}

	private final NodeBuilderContext context;
	private boolean configured;

	private String id;

	public SchemaNodeConfiguratorImpl(NodeBuilderContext context) {
		this.context = context;

		configured = false;
		context.pushConfigurator(this);
	}

	@Override
	protected void prepare() {
		if (!configured) {
			assertConfigurable();
			configuration();
			configured = true;
		}
	}

	protected void configuration() {
	}

	@SuppressWarnings("unchecked")
	protected final S getThis() {
		return (S) this;
	}

	protected NodeBuilderContext getContext() {
		return context;
	}

	@Override
	protected void created(N created) {
		getContext().popConfigurator(created);
	}

	@Override
	public final S id(String id) {
		assertConfigurable(this.id);
		this.id = id;

		N override = getContext().getOverriddenNode(id, getNodeClass());

		return getThis();
	}

	protected abstract Class<N> getNodeClass();

	protected final String getId() {
		return id;
	}

	protected void assertConfigurable(Object object) {
		if (object != null)
			throw new NullPointerException();
	}

	protected void assertConfigurable() {
		assertBranchable();
		if (configured)
			throw new InvalidBuildStateException(this);
	}

	protected void assertBranchable() {
		assertNotStale();
		if (!context.isConfiguratorActive(this))
			throw new InvalidBuildStateException(this);
	}

	protected void assertHasId() {
		if (id == null || id == "")
			throw new IllegalArgumentException();
	}

	@Override
	protected boolean assertReady() {
		return super.assertReady() && getContext().isConfiguratorActive(this);
	}
}
