package uk.co.strangeskies.modabi.schema.processing.impl.unbinding;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.building.impl.ElementNodeOverrider;
import uk.co.strangeskies.modabi.schema.model.building.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.utilities.MultiException;

public class ElementNodeUnbinder {
	private final UnbindingContext context;

	public ElementNodeUnbinder(UnbindingContext context) {
		this.context = context;
	}

	public <U> void unbind(ElementNode.Effective<U> node, List<U> data) {
		for (U item : data) {
			if (node.isExtensible() != null && node.isExtensible()) {
				List<Model.Effective<? extends U>> nodes = context
						.getMatchingModels(node, item.getClass()).stream()
						.map(n -> n.effective())
						.collect(Collectors.toCollection(ArrayList::new));

				if (nodes.isEmpty())
					throw new SchemaException("Unable to find model to satisfy element '"
							+ node.getName()
							+ "' with model '"
							+ node.effective().baseModel().stream()
									.map(m -> m.source().getName().toString())
									.collect(Collectors.joining(", ")) + "' for object '" + item
							+ "' to be unbound.");

				new UnbindingAttempter(context)
						.tryForEach(
								nodes,
								(c, n) -> {
									ElementNode.Effective<? extends U> overridden = new ElementNodeOverrider(
											new ModelBuilderImpl()).override(node, n.effective());

									castAndUnbind(c, overridden, item);
								},
								l -> new MultiException("Unable to unbind element '"
										+ node.getName()
										+ "' with model candidates '"
										+ nodes.stream().map(m -> m.source().getName().toString())
												.collect(Collectors.joining(", ")) + "' for object '"
										+ item + "' to be unbound.", l));
			} else {
				castAndUnbind(context, node, item);
			}
		}
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
