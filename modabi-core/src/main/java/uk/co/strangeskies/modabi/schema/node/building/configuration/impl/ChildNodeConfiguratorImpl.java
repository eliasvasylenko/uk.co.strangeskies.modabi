package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;

public abstract class ChildNodeConfiguratorImpl<S extends ChildNodeConfigurator<S, N>, N extends ChildNode<?, ?>>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		ChildNodeConfigurator<S, N> {
	private final SchemaNodeConfigurationContext<? super N> context;

	private Class<?> postInputClass;

	public ChildNodeConfiguratorImpl(
			SchemaNodeConfigurationContext<? super N> parent) {
		this.context = parent;

		addResultListener(result -> parent.addChild(result));
	}

	protected SchemaNodeConfigurationContext<? super N> getContext() {
		return context;
	}

	@Override
	public List<N> getOverriddenNodes() {
		return getName() == null ? Collections.emptyList() : getContext()
				.overrideChild(getName(), getNodeClass());
	}

	@Override
	protected DataLoader getDataLoader() {
		return getContext().dataLoader();
	}

	@Override
	protected Namespace getNamespace() {
		return getName() != null ? getName().getNamespace() : getContext()
				.namespace();
	}

	@Override
	public S name(String name) {
		return name(new QualifiedName(name, getContext().namespace()));
	}

	@Override
	public S postInputClass(Class<?> postInputClass) {
		assertConfigurable(this.postInputClass);
		this.postInputClass = postInputClass;

		return getThis();
	}

	protected Class<?> getPostInputClass() {
		return postInputClass;
	}
}
