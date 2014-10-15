package uk.co.strangeskies.modabi.schema.processing.unbinding.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode.Effective;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.model.ModelBuilder;
import uk.co.strangeskies.modabi.schema.processing.impl.ComplexNodeOverrider;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingException;

public class ComplexNodeUnbinder {
	private final UnbindingContextImpl context;

	public ComplexNodeUnbinder(UnbindingContextImpl context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <U> void unbind(ComplexNode.Effective<U> node, List<U> data) {
		Map<QualifiedName, ComplexNode.Effective<?>> attemptedOverrideMap = new HashMap<>();

		if (node.isExtensible() != null && node.isExtensible()) {
			for (U item : data) {
				List<? extends Model.Effective<? extends U>> nodes = context
						.getMatchingModels(node, (Class<? extends U>) item.getClass());

				if (nodes.isEmpty())
					throw new SchemaException("Unable to find model to satisfy element '"
							+ node.getName()
							+ "' with model '"
							+ node.effective().baseModel().stream()
									.map(m -> m.source().getName().toString())
									.collect(Collectors.joining(", ")) + "' for object '" + item
							+ "' to be unbound.");

				List<? extends Model.Effective<? extends U>> finalNodes = nodes;
				/* Model.Effective<? extends U> success = */
				context
						.attemptUnbindingUntilSuccessful(
								nodes,
								(c, n) -> {
									ComplexNode.Effective<? extends U> overridden = (Effective<? extends U>) attemptedOverrideMap
											.get(n.getName());

									if (overridden == null) {
										overridden = new ComplexNodeOverrider(c.provisions()
												.provide(ModelBuilder.class)).override(node,
												n.effective());
										attemptedOverrideMap.put(n.getName(), overridden);
									}

									castAndUnbind(c, overridden, item);
								},
								l -> new UnbindingException("Unable to unbind element '"
										+ node.getName()
										+ "' with model candidates '"
										+ finalNodes.stream()
												.map(m -> m.source().getName().toString())
												.collect(Collectors.joining(", ")) + "' for object '"
										+ item + "' to be unbound.", context, l));

				/*-
				 * TODO allow optimisation in unambiguous cases? Currently breaks precedence.
				nodes.remove(success);
				((List<Object>) nodes).add(0, success);
				 */
			}
		} else
			for (U item : data)
				castAndUnbind(context, node, item);
	}

	private <U extends V, V> void castAndUnbind(UnbindingContextImpl context,
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
