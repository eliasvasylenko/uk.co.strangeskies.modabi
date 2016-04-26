/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.impl.schema.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.ReturningNodeProcessor;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.ChoiceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.impl.schema.ComplexNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.impl.schema.DataNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.impl.schema.InputSequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.impl.schema.SequenceNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Imports;
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
 */
public class SequentialChildrenConfigurator implements ChildrenConfigurator {
	private class MergeGroup {
		private final QualifiedName name;
		private final Set<ChildNode.Effective<?, ?>> children;
		private boolean overridden;

		public MergeGroup(QualifiedName name, int index) {
			this.name = name;
			children = new HashSet<>();

			if (name != null)
				namedMergeGroups.put(name, this);
			mergedChildren.add(index, this);
		}

		public QualifiedName getName() {
			return name;
		}

		public int getIndex() {
			return mergedChildren.indexOf(this);
		}

		public ChildNode.Effective<?, ?> getChild() {
			if (children.size() > 1)
				throw new SchemaException(
						"Node '" + getName() + "' is inherited multiple times and must be explicitly overridden.");

			return children.stream().findAny().get();
		}

		public Set<ChildNode.Effective<?, ?>> getChildren() {
			return Collections.unmodifiableSet(children);
		}

		public boolean addChild(ChildNode.Effective<?, ?> child) {
			if (overridden)
				throw new SchemaException("Cannot add contributing node '" + child + "' to override group, as override '"
						+ children.iterator().next() + "' already found");
			return children.add(child);
		}

		public void override(ChildNode.Effective<?, ?> result) {
			if (overridden)
				throw new SchemaException("Cannot specify override '" + result + "' for override group, as override '"
						+ children.iterator().next() + "' already found");
			children.clear();
			addChild(result);
			overridden = true;
		}
	}

	private boolean blocked;
	private int childIndex;

	private final SchemaNodeConfigurationContext context;

	private final List<ChildNode<?, ?>> children;
	private final List<MergeGroup> mergedChildren;
	private final Map<QualifiedName, MergeGroup> namedMergeGroups;

	private TypeToken<?> inputTarget;
	private boolean constructorExpected;
	private boolean staticMethodExpected;

	public SequentialChildrenConfigurator(SchemaNodeConfigurationContext context) {
		children = new ArrayList<>();
		mergedChildren = new ArrayList<>();
		namedMergeGroups = new HashMap<>();

		List<? extends SchemaNode<?, ?>> reversedNodes = new ArrayList<>(context.overriddenNodes());
		Collections.reverse(reversedNodes);
		for (SchemaNode<?, ?> overriddenNode : reversedNodes) {
			int index = 0;

			for (ChildNode.Effective<?, ?> child : overriddenNode.effective().children()) {
				MergeGroup group = merge(overriddenNode.name(), child.name(), index);
				group.addChild(child);
				index = group.getIndex() + 1;
			}
		}

		/*
		 * Initial state:
		 */
		this.context = context;
		inputTarget = context.inputTargetType();
		constructorExpected = context.isConstructorExpected();
		staticMethodExpected = context.isStaticMethodExpected();

		childIndex = 0;
	}

	private MergeGroup merge(QualifiedName parentName, QualifiedName name, int index) {
		MergeGroup group;

		group = namedMergeGroups.get(name);

		if (group == null) {
			group = new MergeGroup(name, index);
		} else if (group.getIndex() < index) {
			List<String> nodesSoFar = mergedChildren.stream().map(MergeGroup::getName).map(Objects::toString)
					.collect(Collectors.toCollection(ArrayList::new));
			nodesSoFar.add(group.getIndex() + 1, "*");

			String nodesSoFarMessage = "Nodes so far: [" + nodesSoFar.stream().collect(Collectors.joining(", ")) + "]";

			throw new SchemaException("The child node '" + name + "' declared by '" + parentName
					+ "' cannot be merged into the overridden nodes with order preservation. " + nodesSoFarMessage);
		}

		return group;
	}

	private void assertUnblocked() {
		if (blocked)
			throw new SchemaException("Blocked from adding children");
	}

	@Override
	public ChildrenContainer create() {
		if (!mergedChildren.isEmpty()) {
			checkRequiredOverrides(null, mergedChildren.size());
		}

		List<ChildNode.Effective<?, ?>> effectiveChildren = mergedChildren.stream().map(MergeGroup::getChild)
				.collect(Collectors.toList());

		return new ChildrenContainer(children, effectiveChildren);
	}

	@SuppressWarnings("unchecked")
	private <U extends ChildNode<?, ?>> List<U> overrideChild(QualifiedName id, TypeToken<U> nodeType) {
		List<ChildNode.Effective<?, ?>> overriddenNodes = new ArrayList<>();

		MergeGroup mergeGroup = namedMergeGroups.get(id);
		if (mergeGroup != null) {
			mergeGroup.getChildren().stream().filter(n -> !nodeType.getRawType().isAssignableFrom(n.getClass())).findAny()
					.ifPresent(n -> {
						throw new SchemaException(
								"Cannot override with node of class '" + n.getClass() + "' with a node of class '" + nodeType + "'");
					});

			overriddenNodes.addAll(mergeGroup.getChildren());

			checkRequiredOverrides(id, mergeGroup.getIndex());
		}

		return (List<U>) overriddenNodes;
	}

	@Override
	public TypeToken<?> getPostInputType() {
		return inputTarget;
	}

	private void checkRequiredOverrides(QualifiedName id, int indexReached) {
		if (childIndex > 0) {
			inputTarget = mergedChildren.get(childIndex - 1).getChild().getPostInputType();
		}

		for (; childIndex < indexReached; childIndex++) {
			MergeGroup skippedGroup = mergedChildren.get(childIndex);
			ChildNode.Effective<?, ?> skippedChild = skippedGroup.getChild();

			if (!context.isAbstract() && skippedChild.abstractness().isMoreThan(Abstractness.UNINFERRED)
					&& !(skippedChild instanceof BindingChildNode
							&& Boolean.TRUE.equals(((BindingChildNode<?, ?, ?>) skippedChild).isExtensible()))) {
				String context = (id != null) ? (" before node '" + id + "'") : "";

				throw new SchemaException("Must override abstract node '" + skippedChild.name() + "'" + context);
			}

			inputTarget = skippedChild.getPostInputType();
		}
	}

	private void addChild(ChildNode<?, ?> result) {
		blocked = false;
		children.add(result);

		ChildNode.Effective<?, ?> effective = result.effective();

		MergeGroup group = merge(new QualifiedName("?", Namespace.getDefault()), effective.name(), childIndex);
		group.override(effective);
		childIndex = group.getIndex() + 1;

		inputTarget = effective.getPostInputType();

		if ((constructorExpected || staticMethodExpected)

				&& effective instanceof InputNode

				&& !"null".equals(((InputNode<?, ?>) effective).getInMethodName())) {

			constructorExpected = staticMethodExpected = false;
		}
	}

	@Override
	public ChildBuilder addChild() {
		assertUnblocked();
		blocked = true;

		SchemaNodeConfigurationContext childContext = new SchemaNodeConfigurationContext() {
			@Override
			public BoundSet boundSet() {
				return context.boundSet();
			}

			@Override
			public DataLoader dataLoader() {
				return context.dataLoader();
			}

			@Override
			public Imports imports() {
				return context.imports();
			}

			@Override
			public Namespace namespace() {
				return context.namespace();
			}

			@Override
			public boolean isAbstract() {
				return context.isAbstract();
			}

			@Override
			public boolean isInputDataOnly() {
				return context.isInputDataOnly();
			}

			@Override
			public <U extends ChildNode<?, ?>> List<U> overrideChild(QualifiedName id, TypeToken<U> nodeType) {
				return SequentialChildrenConfigurator.this.overrideChild(id, nodeType);
			}

			@Override
			public List<? extends SchemaNode<?, ?>> overriddenNodes() {
				return context.overriddenNodes();
			}

			@Override
			public boolean isInputExpected() {
				return context.isInputExpected();
			}

			@Override
			public boolean isConstructorExpected() {
				return constructorExpected;
			}

			@Override
			public boolean isStaticMethodExpected() {
				return staticMethodExpected;
			}

			@Override
			public TypeToken<?> inputTargetType() {
				if (!isInputExpected())
					return null;

				return inputTarget;
			}

			@Override
			public TypeToken<?> outputSourceType() {
				return context.outputSourceType();
			}

			@Override
			public void addChild(ChildNode<?, ?> result) {
				SequentialChildrenConfigurator.this.addChild(result);
			}

			@Override
			public SchemaNode<?, ?> parentNodeProxy() {
				return context.parentNodeProxy();
			}
		};

		return new ChildBuilder() {
			@Override
			public InputSequenceNodeConfigurator inputSequence() {
				return new InputSequenceNodeConfiguratorImpl(childContext);
			}

			@Override
			public DataNodeConfigurator<Object> data() {
				return new DataNodeConfiguratorImpl<>(childContext);
			}

			@Override
			public ChoiceNodeConfigurator choice() {
				return new ChoiceNodeConfiguratorImpl(childContext);
			}

			@Override
			public SequenceNodeConfigurator sequence() {
				return new SequenceNodeConfiguratorImpl(childContext);
			}

			@Override
			public ComplexNodeConfigurator<Object> complex() {
				return new ComplexNodeConfiguratorImpl<>(childContext);
			}
		};
	}
}
