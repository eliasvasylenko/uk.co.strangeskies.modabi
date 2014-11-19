package uk.co.strangeskies.modabi.schema.management.unbinding;

import java.util.List;

import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public interface UnbindingState {
	List<SchemaNode.Effective<?, ?>> bindingNodeStack();

	default SchemaNode.Effective<?, ?> unbindingNode() {
		return unbindingNode(0);
	}

	default SchemaNode.Effective<?, ?> unbindingNode(int parent) {
		return bindingNodeStack().get(bindingNodeStack().size() - (1 + parent));
	}

	List<Object> unbindingSourceStack();

	default Object unbindingSource() {
		return unbindingSource(0);
	}

	default Object unbindingSource(int parent) {
		return unbindingSourceStack().get(
				unbindingSourceStack().size() - (1 + parent));
	}

	Bindings bindings();
}
