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
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.token.TypeToken;

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
 * priority sequences are prioritized for earlier placement wherever compatible
 * with sequence ordering preservation.
 *
 * @author Elias N Vasylenko
 */
public class ChildrenConfiguratorImpl implements ChildrenConfigurator {
	private class MergeGroup {
		private final QualifiedName name;
		private final Set<ChildBindingPoint<?>> children;
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

		public ChildBindingPoint<?> getChild() {
			if (children.size() > 1)
				throw new ModabiException(t -> t.mustOverrideMultiplyInherited(getName()));

			return children.stream().findAny().get();
		}

		public Set<ChildBindingPoint<?>> getChildren() {
			return Collections.unmodifiableSet(children);
		}

		public boolean addChildResult(ChildBindingPoint<?> child) {
			if (overridden)
				throw new ModabiException(t -> t.cannotAddInheritedNodeWhenOverridden(getName()));
			return children.add(child);
		}

		public void override(ChildBindingPoint<?> result) {
			if (overridden)
				throw new ModabiException(t -> t.cannotAddInheritedNodeWhenOverridden(getName()));
			children.clear();
			addChildResult(result);
			overridden = true;
		}
	}

	private boolean blocked;
	private int childIndex;

	private final SchemaNodeConfigurationContext context;

	private final List<ChildBindingPoint<?>> children;
	private final List<MergeGroup> mergedChildren;
	private final Map<QualifiedName, MergeGroup> namedMergeGroups;

	private TypeToken<?> inputTarget;
	private TypeToken<?> outputType;

	public ChildrenConfiguratorImpl(SchemaNodeConfigurationContext context, TypeToken<?> inputType,
			TypeToken<?> outputType) {
		children = new ArrayList<>();
		mergedChildren = new ArrayList<>();
		namedMergeGroups = new HashMap<>();

		List<? extends SchemaNode> reversedNodes = new ArrayList<>(context.overriddenAndBaseNodes());
		Collections.reverse(reversedNodes);
		for (SchemaNode overriddenNode : reversedNodes) {
			int index = 0;

			for (ChildBindingPoint<?> child : overriddenNode.childBindingPoints()) {
				MergeGroup group = merge(child.name(), index);
				group.addChildResult(child);
				index = group.getIndex() + 1;
			}
		}

		/*
		 * Initial state:
		 */
		this.context = context;
		this.inputTarget = inputType;
		this.outputType = outputType;

		childIndex = 0;
	}

	private MergeGroup merge(QualifiedName name, int index) {
		MergeGroup group;

		group = namedMergeGroups.get(name);

		if (group == null) {
			group = new MergeGroup(name, index);
		} else if (group.getIndex() < index) {
			List<QualifiedName> nodesSoFar = mergedChildren.stream().map(MergeGroup::getName).limit(group.getIndex())
					.collect(Collectors.toCollection(ArrayList::new));

			throw new ModabiException(t -> t.cannotOverrideNodeOutOfOrder(name, nodesSoFar));
		}

		return group;
	}

	private void assertUnblocked() {
		if (blocked)
			throw new ModabiException(t -> t.cannotAddChild());
	}

	@Override
	public List<ChildBindingPoint<?>> create() {
		if (!mergedChildren.isEmpty()) {
			checkRequiredOverrides(null, mergedChildren.size());
		}

		return mergedChildren.stream().map(MergeGroup::getChild).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private <U extends ChildBindingPoint<?>> List<U> overrideChild(QualifiedName id) {
		List<ChildBindingPoint<?>> overriddenNodes = new ArrayList<>();

		MergeGroup mergeGroup = namedMergeGroups.get(id);
		if (mergeGroup != null) {
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
			inputTarget = mergedChildren.get(childIndex - 1).getChild().postInputType();
		}

		for (; childIndex < indexReached; childIndex++) {
			MergeGroup skippedGroup = mergedChildren.get(childIndex);
			ChildBindingPoint<?> skippedChild = skippedGroup.getChild();

			if (context.bindingPoint().concrete() && !skippedChild.concrete() && !skippedChild.extensible()) {
				throw new ModabiException(t -> t.mustOverrideAbstractNode(skippedChild.name(), id));
			}

			inputTarget = skippedChild.postInputType();
		}
	}

	private void addChild(ChildBindingPoint<?> result) {
		blocked = false;
		children.add(result);

		MergeGroup group = merge(result.name(), childIndex);
		group.override(result);
		childIndex = group.getIndex() + 1;

		inputTarget = result.postInputType();
	}

	@Override
	public ChildBindingPointConfigurator<?> addChild() {
		assertUnblocked();
		blocked = true;

		ChildBindingPointConfigurationContext childContext = new ChildBindingPointConfigurationContext() {
			@Override
			public SchemaNode parent() {
				return context.parent();
			}

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
			public List<ChildBindingPoint<?>> overrideChild(QualifiedName id) {
				return ChildrenConfiguratorImpl.this.overrideChild(id);
			}

			@Override
			public TypeToken<?> inputTargetType() {
				return inputTarget;
			}

			@Override
			public TypeToken<?> outputSourceType() {
				return outputType;
			}

			@Override
			public void addChildResult(ChildBindingPoint<?> result) {
				ChildrenConfiguratorImpl.this.addChild(result);
			}
		};

		return null; // TODO
	}
}
