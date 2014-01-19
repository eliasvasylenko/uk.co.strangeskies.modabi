package uk.co.strangeskies.modabi.model.impl;

import uk.co.strangeskies.gears.utilities.factory.Configurator;
import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;

public abstract class SchemaNodeConfiguratorImpl<S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode>
		extends Configurator<N> implements SchemaNodeConfigurator<S, N>, SchemaNode {
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
		id = name;

		return getThis();
	}

	@Override
	public String getId() {
		return id;
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
