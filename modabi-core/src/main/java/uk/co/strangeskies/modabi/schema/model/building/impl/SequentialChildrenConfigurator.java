package uk.co.strangeskies.modabi.schema.model.building.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.impl.ChoiceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.model.building.configurators.impl.DataNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.model.building.configurators.impl.ElementNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.model.building.configurators.impl.InputSequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.model.building.configurators.impl.SequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;

/**
 * Children, both inherited and then subsequently added, are merged with
 * sequence ordering preservation, with higher priority children placed earlier.
 * Nodes added through this configurator are merged into the inherited children
 * with highest priority, so will appear as early as possible in the ultimate
 * effective sequence.
 *
 * Ordering preservation means, for example, we cannot inherit both sequences
 * ["a", "b"] and ["b", "a"], as it is not possible to merge the nodes and
 * preserve ordering.
 *
 * Early priority means, for example, that merging the sequence ["a", "b", "c",
 * "d"] with the lower priority sequence ["1", "a", "2", "3", "c", "4"] will
 * result in the sequence ["1", "a", "b", "2", "3", "c", "d", "4"], as higher
 * priority sequences are prioritised for earlier placement wherever compatible
 * with sequence ordering preservation.
 *
 * @author Elias N Vasylenko
 *
 * @param <C>
 * @param <B>
 */
public class SequentialChildrenConfigurator<C extends ChildNode<?, ?>, B extends BindingChildNode<?, ?, ?>>
		implements ChildrenConfigurator<C, B> {
	private class MergeGroup {
		private final QualifiedName name;
		private final Set<ChildNode.Effective<?, ?>> children;
		private boolean overridden;

		public MergeGroup(ChildNode.Effective<?, ?> node) {
			this.name = node.getName();
			children = new HashSet<>();
			children.add(node);
		}

		public QualifiedName getName() {
			return name;
		}

		public ChildNode.Effective<?, ?> getChild() {
			if (children.size() > 1)
				throw new SchemaException(
						"Node '"
								+ getName()
								+ "' is inherited multiple times and must be explicitly overridden.");

			return children.stream().findAny().get();
		}

		public Set<ChildNode.Effective<?, ?>> getChildren() {
			return Collections.unmodifiableSet(children);
		}

		public boolean addChild(ChildNode.Effective<?, ?> child) {
			if (overridden)
				throw new SchemaException(""); // TODO ################
			return children.add(child);
		}

		public void override(ChildNode.Effective<?, ?> result) {
			if (overridden)
				throw new SchemaException(""); // TODO ################
			children.clear();
			addChild(result);
			overridden = true;
		}
	}

	private boolean blocked;
	private int childIndex;

	private final Namespace namespace;

	private final List<ChildNode<?, ?>> children;
	private final List<MergeGroup> mergedChildren;
	private final Map<QualifiedName, MergeGroup> namedMergeGroups;

	private Class<?> inputTarget;
	private final Class<?> outputTarget;
	private final DataLoader loader;
	private final boolean isAbstract;

	public SequentialChildrenConfigurator(Namespace namespace,
			LinkedHashSet<? extends SchemaNode<?, ?>> overriddenNodes,
			Class<?> inputTarget, Class<?> outputTarget, DataLoader loader,
			boolean isAbstract) {
		children = new ArrayList<>();
		mergedChildren = new ArrayList<>();
		namedMergeGroups = new HashMap<>();

		List<? extends SchemaNode<?, ?>> reversedNodes = new ArrayList<>(
				overriddenNodes);
		Collections.reverse(reversedNodes);
		for (SchemaNode<?, ?> overriddenNode : reversedNodes) {
			int index = 0;

			for (ChildNode<?, ?> child : overriddenNode.children())
				index = merge(overriddenNode.getName(), child.effective(), index, false);
		}

		this.namespace = namespace;

		this.inputTarget = inputTarget;
		this.outputTarget = outputTarget;

		this.loader = loader;
		this.isAbstract = isAbstract;

		childIndex = 0;
	}

	private int merge(QualifiedName parentName, ChildNode.Effective<?, ?> child,
			int index, boolean override) {
		QualifiedName name = child.getName();

		if (name != null) {
			MergeGroup group = namedMergeGroups.get(name);

			if (group == null) {
				group = new MergeGroup(child);
				namedMergeGroups.put(name, group);
				mergedChildren.add(index++, group);
			} else {
				int newIndex = mergedChildren.indexOf(group) + 1;

				if (newIndex < index)
					if (override)
						throw new SchemaException(
								"The child node '"
										+ name
										+ "' declared by '"
										+ parentName
										+ "' cannot be merged into the overridden nodes with order preservation.");
					else
						throw new SchemaException("The child node '" + name
								+ "' inherited from the overridden node '" + parentName
								+ "' cannot be merged with order preservation.");

				if (override)
					group.override(child);
				else
					group.addChild(child);
				index = newIndex;
			}
		} else
			mergedChildren.add(index++, new MergeGroup(child));

		return index;
	}

	@Override
	public ChildrenContainer create() {
		List<ChildNode.Effective<?, ?>> effectiveChildren = mergedChildren.stream()
				.map(MergeGroup::getChild).collect(Collectors.toList());

		return new ChildrenContainer(children, effectiveChildren);
	}

	private void assertUnblocked() {
		if (blocked)
			throw new SchemaException("Blocked from adding children");
	}

	@SuppressWarnings("unchecked")
	private <U extends ChildNode<?, ?>> LinkedHashSet<U> overrideChild(
			QualifiedName id, Class<U> nodeClass) {
		LinkedHashSet<ChildNode.Effective<?, ?>> overriddenNodes = new LinkedHashSet<>();

		MergeGroup mergeGroup = namedMergeGroups.get(id);
		if (mergeGroup != null) {
			if (mergeGroup.getChildren().stream()
					.anyMatch(n -> !nodeClass.isAssignableFrom(n.getClass())))
				throw new SchemaException(
						"Cannot override with node of a different class");

			overriddenNodes.addAll(mergeGroup.getChildren());
		}

		return (LinkedHashSet<U>) overriddenNodes;
	}

	private void addChild(ChildNode<?, ?> result) {
		blocked = false;
		children.add(result);

		ChildNode.Effective<?, ?> effective = result.effective();

		childIndex = merge(new QualifiedName("?", Namespace.getDefault()),
				effective, childIndex, true);

		inputTarget = effective.getPostInputClass();
	}

	@Override
	public ChildBuilder<C, B> addChild() {
		assertUnblocked();
		blocked = true;

		SchemaNodeConfigurationContext<ChildNode<?, ?>> context = new SchemaNodeConfigurationContext<ChildNode<?, ?>>() {
			@Override
			public DataLoader getDataLoader() {
				return loader;
			}

			@Override
			public Namespace getNamespace() {
				return namespace;
			}

			@Override
			public boolean isAbstract() {
				return isAbstract;
			}

			@Override
			public <U extends ChildNode<?, ?>> LinkedHashSet<U> overrideChild(
					QualifiedName id, Class<U> nodeClass) {
				return SequentialChildrenConfigurator.this.overrideChild(id, nodeClass);
			}

			@Override
			public Class<?> getOutputTargetClass() {
				return outputTarget;
			}

			@Override
			public Class<?> getInputTargetClass() {
				/*
				 * TODO inputTarget may change due to ordering if this child's name
				 * would see it merging with an overridden child further along the
				 * chain.
				 */
				return inputTarget;
			}

			@Override
			public void addChild(ChildNode<?, ?> result) {
				SequentialChildrenConfigurator.this.addChild(result);
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
