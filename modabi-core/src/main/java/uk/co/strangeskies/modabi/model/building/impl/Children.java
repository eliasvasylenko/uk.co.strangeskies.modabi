package uk.co.strangeskies.modabi.model.building.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.gears.utilities.collection.HashSetMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.SetMultiMap;
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
import uk.co.strangeskies.modabi.schema.SchemaException;

public class Children<C extends ChildNode<?>, B extends BindingChildNode<?, ?>> {
	private boolean blocked;

	private final List<ChildNode<?>> children;
	private final SetMultiMap<String, ChildNode.Effective<?>> namedInheritedChildren;
	private final List<ChildNode.Effective<?>> inheritedChildren;

	public Children() {
		children = new ArrayList<>();
		inheritedChildren = new ArrayList<>();
		namedInheritedChildren = new HashSetMultiHashMap<>();
	}

	public List<ChildNode<?>> getChildren() {
		return children;
	}

	public List<ChildNode.Effective<?>> getEffectiveChildren() {
		List<ChildNode.Effective<?>> effectiveChildren = new ArrayList<>();
		effectiveChildren.addAll(inheritedChildren);
		effectiveChildren.addAll(children.stream().map(c -> c.effective())
				.collect(Collectors.toList()));
		return effectiveChildren;
	}

	public SetMultiMap<String, ChildNode.Effective<?>> getNamedInheritedChildren() {
		return namedInheritedChildren;
	}

	public void assertUnblocked() {
		if (blocked)
			throw new SchemaException("Blocked from adding children");
	}

	public void addChild(ChildNode<?> result) {
		blocked = false;
		children.add(result);
		if (result.getName() != null) {
			Set<ChildNode.Effective<?>> removed = namedInheritedChildren
					.remove(result.getName());
			if (removed != null)
				inheritedChildren.removeAll(removed);
		}
	}

	public void inheritChildren(List<? extends ChildNode<?>> nodes) {
		inheritChildren(inheritedChildren.size(), nodes);
	}

	public void inheritChildren(int index, List<? extends ChildNode<?>> nodes) {
		List<ChildNode.Effective<?>> effectiveList = nodes.stream()
				.map(n -> n.effective()).collect(Collectors.toList());
		inheritNamedChildren(effectiveList);
		inheritedChildren.addAll(index, effectiveList);
	}

	public void inheritNamedChildren(List<? extends ChildNode.Effective<?>> nodes) {
		nodes.stream().filter(c -> c.getName() != null)
				.forEach(c -> namedInheritedChildren.add(c.getName(), c));
	}

	@SuppressWarnings("unchecked")
	public <U extends ChildNode<?>> Set<U> overrideChild(String id,
			Class<U> nodeClass) {
		Set<ChildNode.Effective<?>> overriddenNodes = namedInheritedChildren
				.get(id);

		if (overriddenNodes != null) {
			if (overriddenNodes.stream().anyMatch(
					n -> !nodeClass.isAssignableFrom(n.getClass())))
				throw new SchemaException(
						"Cannot override with node of a different class");
		} else
			overriddenNodes = new HashSet<>();

		return (Set<U>) Collections.unmodifiableSet(overriddenNodes);
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
			public <U extends ChildNode<?>> Set<U> overrideChild(String id,
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
