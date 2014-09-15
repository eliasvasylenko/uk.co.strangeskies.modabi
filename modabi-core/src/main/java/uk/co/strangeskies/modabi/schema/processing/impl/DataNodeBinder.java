package uk.co.strangeskies.modabi.schema.processing.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.processing.ValueResolution;

public class DataNodeBinder {
	private DataSource dataSource;

	public <U> List<U> bindDataNode(DataNode.Effective<U> node) {
		nodeStack.push(node);
		bindingChildNodeStack.add(node);

		DataSource previousDataSource = dataSource;

		List<U> result = new ArrayList<>();

		if (node.isValueProvided()) {
			if (node.valueResolution() == ValueResolution.REGISTRATION_TIME)
				result.addAll(node.providedValues());
			else {
				dataSource = node.providedValueBuffer();
				result.add(bindNode(node));
			}
		} else if (node.format() != null) {
			switch (node.format()) {
			case CONTENT:
				dataSource = input.readContent();

				if (dataSource != null)
					result.add(bindNode(node));
				break;
			case PROPERTY:
				dataSource = input.readProperty(node.getName());

				if (dataSource != null)
					result.add(bindNode(node));
				break;
			case SIMPLE_ELEMENT:
				while (node.getName().equals(input.peekNextChild())) {
					input.startNextChild(node.getName());

					dataSource = input.readContent();

					result.add(bindNode(node));
					input.endChild();
				}
			}
		} else
			result.add(bindNode(node));

		dataSource = previousDataSource;

		bindingChildNodeStack.remove(bindingChildNodeStack.size() - 1);
		nodeStack.pop();

		return result;
	}
}
