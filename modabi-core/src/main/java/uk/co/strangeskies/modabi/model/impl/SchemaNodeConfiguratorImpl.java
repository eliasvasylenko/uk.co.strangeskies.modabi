package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N> {
	protected abstract class SchemaNodeImpl<N extends SchemaNodeImpl<N>>
			implements SchemaNode {
		private String id;

		public final String getId() {
			return id;
		}
	}

	private NodeBuilderContext context;
	private boolean configured;

	private N node;

	public SchemaNodeConfiguratorImpl(NodeBuilderContext context) {
		this.context = context;

		configured = false;
		context.pushConfigurator(this);

		node = createNode();
	}

	protected abstract N createNode();

	@Override
	protected void prepare() {
		configure();
	}

	public final void configure() {
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
	public final S id(String name) {
		assertConfigurable();
		node.id = name;

		return getThis();
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
