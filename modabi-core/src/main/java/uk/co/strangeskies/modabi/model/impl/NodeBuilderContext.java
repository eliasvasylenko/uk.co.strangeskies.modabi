package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import uk.co.strangeskies.modabi.model.SchemaNode;

class NodeBuilderContext {
	private Deque<List<SchemaNode>> branchStack;
	private Deque<SchemaNodeConfiguratorImpl<?, ?>> configuratorStack;

	public NodeBuilderContext() {
		configuratorStack = new ArrayDeque<>();
		branchStack = new ArrayDeque<>();
	}

	public void pushConfigurator(SchemaNodeConfiguratorImpl<?, ?> configurator) {
		configuratorStack.push(configurator);
	}

	public void popConfigurator(SchemaNode result) {
		configuratorStack.pop();
		if (!branchStack.isEmpty())
			branchStack.peek().add(result);
	}

	public boolean isConfiguratorActive(
			SchemaNodeConfiguratorImpl<?, ?> configurator) {
		return configuratorStack.peek() == configurator;
	}

	public void pushBranch(List<SchemaNode> branch) {
		branchStack.push(branch);
	}

	public void popBranch() {
		branchStack.pop();
	}
}
