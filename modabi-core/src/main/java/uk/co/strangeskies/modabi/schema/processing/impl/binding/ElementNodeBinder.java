package uk.co.strangeskies.modabi.schema.processing.impl.binding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.building.impl.ElementNodeOverrider;
import uk.co.strangeskies.modabi.schema.model.building.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;

public class ElementNodeBinder {
	private final BindingContext context;

	public ElementNodeBinder(BindingContext context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <U> List<U> bind(ElementNode.Effective<U> node) {
		List<U> result = new ArrayList<>();

		int count = 0;
		ElementNode.Effective<U> inputNode;
		do {
			inputNode = null;

			QualifiedName nextElement = context.input().peekNextChild();
			if (nextElement != null) {
				if (node.isExtensible()) {
					Model.Effective<?> extension = context.getModel(nextElement);

					if (node.getDataClass().isAssignableFrom(extension.getDataClass()))
						inputNode = new ElementNodeOverrider(new ModelBuilderImpl())
								.override(node, (Model.Effective<U>) extension.effective());
				} else if (Objects.equals(nextElement, node.getName()))
					inputNode = node;

				BindingContext context = this.context;
				if (inputNode != null) {
					context.input().startNextChild();

					U binding = new BindingNodeBinder(context).bind(inputNode);

					if (inputNode.isInMethodChained()) {
						context = context.withReplacedBindingTarget(binding);
						System.out.println(inputNode.getName() + " ? "
								+ context.bindingTargetStack());
					}

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
