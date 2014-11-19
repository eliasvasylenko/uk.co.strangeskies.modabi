package uk.co.strangeskies.modabi.schema.management.unbinding.impl;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingException;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.utilities.collection.computingmap.ComputingMap;

public class ComplexNodeUnbinder {
	private final UnbindingContextImpl context;

	public ComplexNodeUnbinder(UnbindingContextImpl context) {
		this.context = context;
	}

	public <U> void unbind(ComplexNode.Effective<U> node, List<U> data) {
		if (node.isExtensible()) {
			for (U item : data) {
				ComputingMap<Model.Effective<? extends U>, ComplexNode.Effective<? extends U>> overrides = context
						.getComplexNodeOverrides(node);

				if (overrides.isEmpty())
					throw new SchemaException(
							"Unable to find model to satisfy complex node '"
									+ node.getName()
									+ "' with model '"
									+ node.baseModel().stream()
											.map(m -> m.source().getName().toString())
											.collect(Collectors.joining(", ")) + "' for object '"
									+ item + "' to be unbound.");

				context
						.attemptUnbindingUntilSuccessful(
								overrides
										.keySet()
										.stream()
										.filter(
												m -> m.getDataClass().isAssignableFrom(item.getClass()))
										.collect(Collectors.toList()),
								(c, n) -> unbindExactNode(c, overrides.putGet(n), item),
								l -> new UnbindingException("Unable to unbind complex node '"
										+ node.getName()
										+ "' with model candidates '"
										+ overrides.keySet().stream()
												.map(m -> m.source().getName().toString())
												.collect(Collectors.joining(", ")) + "' for object '"
										+ item + "' to be unbound.", context, l));
			}
		} else
			for (U item : data)
				unbindExactNode(context, node, item);
	}

	private <U extends V, V> void unbindExactNode(UnbindingContextImpl context,
			ComplexNode.Effective<U> element, V data) {
		try {
			if (!element.isInline())
				context.output().nextChild(element.getName());

			new BindingNodeUnbinder(context).unbind(element, element.getDataClass()
					.cast(data));

			if (!element.isInline())
				context.output().endChild();

			context.bindings().add(element, data);
		} catch (ClassCastException e) {
			throw new UnbindingException("Cannot unbind data at this node.", context,
					e);
		}
	}
}
