package uk.co.strangeskies.modabi.model.building.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.utilities.collection.MultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.MultiMap;
import uk.co.strangeskies.modabi.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.model.building.DataLoader;
import uk.co.strangeskies.modabi.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.configurators.impl.ChoiceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.configurators.impl.DataNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.configurators.impl.ElementNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.configurators.impl.InputSequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.building.configurators.impl.SequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaException;

public class Children<C extends ChildNode<?>, B extends BindingChildNode<?, ?>> {
	private boolean blocked;

	private final List<ChildNode<?>> children;
	private final MultiMap<String, ChildNode.Effective<?>, LinkedHashSet<ChildNode.Effective<?>>> namedInheritedChildren;
	private final List<ChildNode.Effective<?>> inheritedChildren;

	public Children(LinkedHashSet<? extends SchemaNode<?>> overriddenNodes) {
		children = new ArrayList<>();
		inheritedChildren = new ArrayList<>();
		namedInheritedChildren = new MultiHashMap<>(LinkedHashSet::new);

		for (SchemaNode<?> overriddenNode : overriddenNodes) {
			/*
			 * TODO:
			 * 
			 * Starting from the first overridden node, for each one we go through
			 * each child, adding them to a special list.
			 * 
			 * Merge together whilst preserving order!
			 */
		}

		inheritedChildren.stream().filter(c -> c.getName() != null)
				.forEach(c -> namedInheritedChildren.add(c.getName(), c));
	}

	public List<ChildNode<?>> getChildren() {
		return children;
	}

	public List<ChildNode.Effective<?>> getEffectiveChildren() {
		System.out.println(inheritedChildren.stream().map(SchemaNode::getName)
				.collect(Collectors.joining(", ")));
		System.out.println("  "
				+ children.stream().map(SchemaNode::getName)
						.collect(Collectors.joining(", ")));
		System.out.println("  "
				+ namedInheritedChildren
						.keySet()
						.stream()
						.map(
								k -> "(" + k + " @ " + namedInheritedChildren.get(k).size()
										+ ")").collect(Collectors.joining(", ")));

		for (Set<? extends ChildNode.Effective<?>> namedChildren : namedInheritedChildren
				.values())
			if (namedChildren.size() > 1) {
				throw new SchemaException(
						"Node '"
								+ namedChildren.iterator().next().getName()
								+ "' is inherited multiple times and must be explicitly overridden.");
			}

		List<ChildNode.Effective<?>> effectiveChildren = new ArrayList<>();
		effectiveChildren.addAll(inheritedChildren);
		effectiveChildren.addAll(children.stream().map(c -> c.effective())
				.collect(Collectors.toList()));
		return effectiveChildren;
	}

	private void assertUnblocked() {
		if (blocked)
			throw new SchemaException("Blocked from adding children");
	}

	@SuppressWarnings("unchecked")
	private <U extends ChildNode<?>> LinkedHashSet<U> overrideChild(String id,
			Class<U> nodeClass) {
		LinkedHashSet<ChildNode.Effective<?>> overriddenNodes = namedInheritedChildren
				.get(id);

		if (overriddenNodes != null) {
			if (overriddenNodes.stream().anyMatch(
					n -> !nodeClass.isAssignableFrom(n.getClass())))
				throw new SchemaException(
						"Cannot override with node of a different class");
		} else
			overriddenNodes = new LinkedHashSet<>();

		return (LinkedHashSet<U>) overriddenNodes;
	}

	private void addChild(ChildNode<?> result) {
		blocked = false;
		children.add(result);
		if (result.effective().getName() != null) {
			Set<ChildNode.Effective<?>> removed = namedInheritedChildren
					.remove(result.getName());
			if (removed != null)
				inheritedChildren.removeAll(removed);
		}
	}

	public ChildBuilder<C, B> addChild(DataLoader loader, Class<?> inputTarget,
			Class<?> outputtarget, boolean isAbstract) {
		assertUnblocked();
		blocked = true;

		SchemaNodeConfigurationContext<ChildNode<?>> context = new SchemaNodeConfigurationContext<ChildNode<?>>() {
			@Override
			public DataLoader getDataLoader() {
				return loader;
			}

			@Override
			public boolean isAbstract() {
				return isAbstract;
			}

			@Override
			public <U extends ChildNode<?>> LinkedHashSet<U> overrideChild(String id,
					Class<U> nodeClass) {
				return Children.this.overrideChild(id, nodeClass);
			}

			@Override
			public Class<?> getCurrentChildOutputTargetClass() {
				return outputtarget;
			}

			@Override
			public Class<?> getCurrentChildInputTargetClass() {
				return inputTarget;
			}

			@Override
			public void addChild(ChildNode<?> result) {
				Children.this.addChild(result);
			}
		};

		return new ChildBuilder<C, B>() {
			@Override
			public InputSequenceNodeConfigurator<B> inputSequence() {
				return new InputSequenceNodeConfiguratorImpl<>(context);
			}

			@Override
			public DataNodeConfigurator<Object> data() {
				return new DataNodeConfiguratorImpl<Object>(context);
			}

			@Override
			public ChoiceNodeConfigurator<C, B> choice() {
				return new ChoiceNodeConfiguratorImpl<>(context);
			}

			@Override
			public SequenceNodeConfigurator<C, B> sequence() {
				return new SequenceNodeConfiguratorImpl<>(context);
			}

			@Override
			public ElementNodeConfigurator<Object> element() {
				return new ElementNodeConfiguratorImpl<>(context);
			}
		};
	}
}
