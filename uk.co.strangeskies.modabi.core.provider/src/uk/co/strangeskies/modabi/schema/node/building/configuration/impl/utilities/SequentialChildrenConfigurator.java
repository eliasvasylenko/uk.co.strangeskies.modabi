/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.ChoiceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.ComplexNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.DataNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.InputSequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.SequenceNodeConfiguratorImpl;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.TypeToken;

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
public class SequentialChildrenConfigurator implements ChildrenConfigurator {
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

	private final List<ChildNode<?, ?>> children;
	private final List<MergeGroup> mergedChildren;
	private final Map<QualifiedName, MergeGroup> namedMergeGroups;

	private final SchemaNodeConfigurationContext<?> context;
	private TypeToken<?> inputTarget;

	public SequentialChildrenConfigurator(
			SchemaNodeConfigurationContext<?> context) {
		children = new ArrayList<>();
		mergedChildren = new ArrayList<>();
		namedMergeGroups = new HashMap<>();

		List<? extends SchemaNode<?, ?>> reversedNodes = new ArrayList<>(
				context.overriddenNodes());
		Collections.reverse(reversedNodes);
		for (SchemaNode<?, ?> overriddenNode : reversedNodes) {
			int index = 0;

			for (ChildNode<?, ?> child : overriddenNode.children())
				index = merge(overriddenNode.getName(), child.effective(), index, false);
		}

		this.context = context;
		inputTarget = context.inputTargetType(null);

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

				List<String> nodesSoFar = mergedChildren.stream()
						.map(MergeGroup::getName).map(Objects::toString)
						.collect(Collectors.toCollection(ArrayList::new));
				nodesSoFar.add(newIndex, "*");

				String nodesSoFarMessage = "Nodes so far: ["
						+ nodesSoFar.stream().collect(Collectors.joining(", ")) + "]";

				if (newIndex < index)
					if (override)
						throw new SchemaException(
								"The child node '"
										+ name
										+ "' declared by '"
										+ parentName
										+ "' cannot be merged into the overridden nodes with order preservation. "
										+ nodesSoFarMessage);
					else
						throw new SchemaException("The child node '" + name
								+ "' inherited from the overridden node '" + parentName
								+ "' cannot be merged with order preservation. "
								+ nodesSoFarMessage);

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
	private <U extends ChildNode<?, ?>> List<U> overrideChild(QualifiedName id,
			TypeToken<U> nodeClass) {
		List<ChildNode.Effective<?, ?>> overriddenNodes = new ArrayList<>();

		MergeGroup mergeGroup = namedMergeGroups.get(id);
		if (mergeGroup != null) {
			mergeGroup
					.getChildren()
					.stream()
					.filter(n -> !nodeClass.getRawType().isAssignableFrom(n.getClass()))
					.findAny()
					.ifPresent(
							n -> {
								throw new SchemaException(
										"Cannot override with node of class '" + n.getClass()
												+ "' with a node of class '" + nodeClass + "'.");
							});

			overriddenNodes.addAll(mergeGroup.getChildren());
		}

		return (List<U>) overriddenNodes;
	}

	private void addChild(ChildNode<?, ?> result) {
		blocked = false;
		children.add(result);

		ChildNode.Effective<?, ?> effective = result.effective();

		childIndex = merge(new QualifiedName("?", Namespace.getDefault()),
				effective, childIndex, true);

		inputTarget = effective.getPostInputType() == null ? null : TypeToken
				.of(effective.getPostInputType());
	}

	@Override
	public ChildBuilder addChild() {
		assertUnblocked();
		blocked = true;

		SchemaNodeConfigurationContext<ChildNode<?, ?>> context = new SchemaNodeConfigurationContext<ChildNode<?, ?>>() {
			@Override
			public BoundSet boundSet() {
				return SequentialChildrenConfigurator.this.context.boundSet();
			}

			@Override
			public DataLoader dataLoader() {
				return SequentialChildrenConfigurator.this.context.dataLoader();
			}

			@Override
			public Namespace namespace() {
				return SequentialChildrenConfigurator.this.context.namespace();
			}

			@Override
			public boolean isAbstract() {
				return SequentialChildrenConfigurator.this.context.isAbstract();
			}

			@Override
			public boolean isInputDataOnly() {
				return SequentialChildrenConfigurator.this.context.isInputDataOnly();
			}

			@Override
			public <U extends ChildNode<?, ?>> List<U> overrideChild(
					QualifiedName id, TypeToken<U> nodeClass) {
				return SequentialChildrenConfigurator.this.overrideChild(id, nodeClass);
			}

			@Override
			public List<? extends SchemaNode<?, ?>> overriddenNodes() {
				return SequentialChildrenConfigurator.this.context.overriddenNodes();
			}

			@Override
			public boolean isInputExpected() {
				return SequentialChildrenConfigurator.this.context.isInputExpected();
			}

			@Override
			public boolean isConstructorExpected() {
				return SequentialChildrenConfigurator.this.context
						.isConstructorExpected() && children.isEmpty();
			}

			@Override
			public boolean isStaticMethodExpected() {
				return SequentialChildrenConfigurator.this.context
						.isStaticMethodExpected() && children.isEmpty();
			}

			@Override
			public TypeToken<?> inputTargetType(QualifiedName name) {
				if (!isInputExpected())
					return null;

				MergeGroup mergeGroup = namedMergeGroups.get(name);
				if (mergeGroup != null) {
					int index = mergedChildren.indexOf(mergeGroup);
					if (index > 0)
						inputTarget = TypeToken.of(mergedChildren.get(index - 1).getChild()
								.getPostInputType());
				}

				return inputTarget;
			}

			@Override
			public TypeToken<?> outputSourceType() {
				return SequentialChildrenConfigurator.this.context.outputSourceType();
			}

			@Override
			public void addChild(ChildNode<?, ?> result) {
				SequentialChildrenConfigurator.this.addChild(result);
			}
		};

		return new ChildBuilder() {
			@Override
			public InputSequenceNodeConfigurator inputSequence() {
				return new InputSequenceNodeConfiguratorImpl(context);
			}

			@Override
			public DataNodeConfigurator<Object> data() {
				return new DataNodeConfiguratorImpl<Object>(context);
			}

			@Override
			public ChoiceNodeConfigurator choice() {
				return new ChoiceNodeConfiguratorImpl(context);
			}

			@Override
			public SequenceNodeConfigurator sequence() {
				return new SequenceNodeConfiguratorImpl(context);
			}

			@Override
			public ComplexNodeConfigurator<Object> complex() {
				return new ComplexNodeConfiguratorImpl<>(context);
			}
		};
	}
}
