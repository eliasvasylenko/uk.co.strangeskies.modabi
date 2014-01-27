package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.modabi.model.SchemaNode;

public class NodeBuilderContextStackItem {
	private final SchemaNodeConfiguratorImpl<?, ?> configurator;
	private final List<SchemaNode> children;

	public NodeBuilderContextStackItem(
			SchemaNodeConfiguratorImpl<?, ?> configurator) {
		this.configurator = configurator;
		children = new ArrayList<>();
	}

	public SchemaNodeConfiguratorImpl<?, ?> getConfigurator() {
		return configurator;
	}

	public List<SchemaNode> getChildren() {
		return children;
	}

	public void addChild(SchemaNode child) {
		children.add(child);
	}
}
