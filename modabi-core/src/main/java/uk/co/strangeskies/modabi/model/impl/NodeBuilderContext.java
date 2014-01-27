package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ContentNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.NodeBuilder;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SimpleElementNodeConfigurator;

class NodeBuilderContext {
	private final Deque<NodeBuilderContextStackItem> configuratorStack;

	public NodeBuilderContext() {
		configuratorStack = new ArrayDeque<>();
	}

	public void pushConfigurator(SchemaNodeConfiguratorImpl<?, ?> configurator) {
		configuratorStack.push(new NodeBuilderContextStackItem(configurator));
	}

	public List<SchemaNode> popConfigurator(SchemaNode result) {
		List<SchemaNode> children = configuratorStack.pop().getChildren();
		if (!configuratorStack.isEmpty())
			configuratorStack.peek().addChild(result);
		return children;
	}

	public boolean isConfiguratorActive(
			SchemaNodeConfiguratorImpl<?, ?> configurator) {
		return configuratorStack.peek().getConfigurator() == configurator;
	}

	public <N extends SchemaNode> N getOverriddenNode(String id,
			Class<N> nodeClass) {
		return null;
	}

	public NodeBuilder childBuilder() {
		return new NodeBuilder() {
			@Override
			public SimpleElementNodeConfigurator<Object> simpleElement() {
				return new SimpleElementNodeConfiguratorImpl<>(NodeBuilderContext.this);
			}

			@Override
			public SequenceNodeConfigurator sequence() {
				return new SequenceNodeConfiguratorImpl(NodeBuilderContext.this);
			}

			@Override
			public PropertyNodeConfigurator<Object> property() {
				return new PropertyNodeConfiguratorImpl<>(NodeBuilderContext.this);
			}

			@Override
			public ElementNodeConfigurator<Object> element() {
				return new ElementNodeConfiguratorImpl<>(NodeBuilderContext.this);
			}

			@Override
			public ContentNodeConfigurator<Object> content() {
				return new ContentNodeConfiguratorImpl<>(NodeBuilderContext.this);
			}

			@Override
			public ChoiceNodeConfigurator choice() {
				return new ChoiceNodeConfiguratorImpl(NodeBuilderContext.this);
			}
		};
	}
}
