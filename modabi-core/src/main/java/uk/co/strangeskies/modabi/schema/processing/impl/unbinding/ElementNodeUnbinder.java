package uk.co.strangeskies.modabi.schema.processing.impl.unbinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.ElementNode;
import uk.co.strangeskies.modabi.schema.node.ElementNode.Effective;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.model.ModelBuilder;
import uk.co.strangeskies.modabi.schema.processing.impl.ElementNodeOverrider;

public class ElementNodeUnbinder {
	private final UnbindingContext context;

	public ElementNodeUnbinder(UnbindingContext context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <U> void unbind(ElementNode.Effective<U> node, List<U> data) {
		Map<QualifiedName, ElementNode.Effective<?>> attemptedOverrideMap = new HashMap<>();

		if (node.isExtensible() != null && node.isExtensible()) {
			for (U item : data) {
				List<? extends Model.Effective<? extends U>> nodes = context
						.getMatchingModels(node, item.getClass());

				if (nodes.isEmpty())
					throw new SchemaException("Unable to find model to satisfy element '"
							+ node.getName()
							+ "' with model '"
							+ node.effective().baseModel().stream()
									.map(m -> m.source().getName().toString())
									.collect(Collectors.joining(", ")) + "' for object '" + item
							+ "' to be unbound.");

				List<? extends Model.Effective<? extends U>> finalNodes = nodes;
				Model.Effective<? extends U> success = new UnbindingAttempter(context)
						.tryForEach(
								nodes,
								(c, n) -> {
									ElementNode.Effective<? extends U> overridden = (Effective<? extends U>) attemptedOverrideMap
											.get(n.getName());

									if (overridden == null) {
										overridden = new ElementNodeOverrider(context
												.provide(ModelBuilder.class)).override(node,
												n.effective());
										attemptedOverrideMap.put(n.getName(), overridden);
									}

									castAndUnbind(c, overridden, item);
								}, l -> context.exception(
										"Unable to unbind element '"
												+ node.getName()
												+ "' with model candidates '"
												+ finalNodes.stream()
														.map(m -> m.source().getName().toString())
														.collect(Collectors.joining(", "))
												+ "' for object '" + item + "' to be unbound.", l));

				nodes.remove(success);
				((List<Object>) nodes).add(0, success);
			}
		} else
			for (U item : data)
				castAndUnbind(context, node, item);
	}

	private <U extends V, V> void castAndUnbind(UnbindingContext context,
			ElementNode.Effective<U> element, V data) {
		try {
			context.output().nextChild(element.getName());
			new BindingNodeUnbinder(context).unbind(element, element.getDataClass()
					.cast(data));
			context.output().endChild();

			context.bindings().add(element, data);
		} catch (ClassCastException e) {
			throw context.exception("Cannot unbind data at this node.", e);
		}
	}
}
