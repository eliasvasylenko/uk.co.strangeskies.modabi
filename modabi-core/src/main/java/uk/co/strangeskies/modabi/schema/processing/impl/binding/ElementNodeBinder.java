package uk.co.strangeskies.modabi.schema.processing.impl.binding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.ElementNode;
import uk.co.strangeskies.modabi.schema.node.ElementNode.Effective;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.model.ModelBuilder;
import uk.co.strangeskies.modabi.schema.processing.impl.ElementNodeOverrider;

public class ElementNodeBinder {
	private final BindingContext context;

	public ElementNodeBinder(BindingContext context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <U> List<U> bind(ElementNode.Effective<U> node) {
		Map<QualifiedName, ElementNode.Effective<?>> attemptedOverrideMap = new HashMap<>();

		List<U> result = new ArrayList<>();

		int count = 0;
		ElementNode.Effective<U> inputNode;
		do {
			inputNode = null;

			QualifiedName nextElement = context.input().peekNextChild();
			if (nextElement != null) {
				if (node.isExtensible()) {
					Model.Effective<?> extension = context.getModel(nextElement);

					if (!node.getDataClass().isAssignableFrom(extension.getDataClass()))
						throw context.exception("Named input node '" + nextElement
								+ "' does not match class of extention point.");

					inputNode = (Effective<U>) attemptedOverrideMap.get(nextElement);
					if (inputNode == null) {
						inputNode = new ElementNodeOverrider(
								context.provide(ModelBuilder.class)).override(node,
								(Model.Effective<U>) extension.effective());
						attemptedOverrideMap.put(nextElement, inputNode);
					}
				} else if (Objects.equals(nextElement, node.getName()))
					inputNode = node;

				BindingContext context = this.context;
				if (inputNode != null) {
					context.input().startNextChild();

					U binding = new BindingNodeBinder(context).bind(inputNode);

					if (inputNode.isInMethodChained())
						context = context.withReplacedBindingTarget(binding);

					result.add(binding);
					context.bindings().add(inputNode, binding);
					context.input().endChild();
					count++;
				}
			}
		} while (!node.occurances().isValueAbove(count) && inputNode != null);

		if (!node.occurances().contains(count))
			throw new SchemaException("Node '" + node.getName() + "' occurances '"
					+ count + "' must be within range '" + node.occurances() + "'.");

		return result;
	}
}
