package uk.co.strangeskies.modabi.schema.model.building.configurators.impl;

import java.util.LinkedHashSet;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.impl.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;

public abstract class ChildNodeConfiguratorImpl<S extends ChildNodeConfigurator<S, N, C, B>, N extends ChildNode<?, ?>, C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		extends SchemaNodeConfiguratorImpl<S, N, C, B> implements
		ChildNodeConfigurator<S, N, C, B> {
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
	public LinkedHashSet<N> getOverriddenNodes() {
		return getName() == null ? new LinkedHashSet<>() : getContext()
				.overrideChild(getName(), getNodeClass());
	}

	@Override
	protected DataLoader getDataLoader() {
		return getContext().getDataLoader();
	}

	@Override
	protected Namespace getNamespace() {
		return getName() != null ? getName().getNamespace() : getContext()
				.getNamespace();
	}

	@Override
	public S name(String name) {
		return name(new QualifiedName(name, getContext().getNamespace()));
	}

	@Override
	public S postInputClass(Class<?> postInputClass) {
		requireConfigurable(this.postInputClass);
		this.postInputClass = postInputClass;

		return getThis();
	}

	protected Class<?> getPostInputClass() {
		return postInputClass;
	}
}
