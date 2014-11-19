package uk.co.strangeskies.modabi.schema.processing.binding;

import java.util.List;

import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public interface BindingState {
	List<SchemaNode.Effective<?, ?>> bindingNodeStack();

	default SchemaNode.Effective<?, ?> bindingNode() {
		return bindingNode(0);
	}

	default SchemaNode.Effective<?, ?> bindingNode(int parent) {
		return bindingNodeStack().get(bindingNodeStack().size() - (1 + parent));
	}

	List<Object> bindingTargetStack();

	default Object bindingTarget() {
		return bindingTarget(0);
	}

	default Object bindingTarget(int parent) {
		return bindingTargetStack().get(bindingTargetStack().size() - (1 + parent));
	}

	Bindings bindings();
}
