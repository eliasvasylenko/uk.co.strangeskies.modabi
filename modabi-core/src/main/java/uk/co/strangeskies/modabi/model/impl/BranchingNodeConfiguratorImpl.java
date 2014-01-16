package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.factory.InvalidBuildStateException;
import uk.co.strangeskies.modabi.model.BranchingNode;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.building.BranchingNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ContentNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.NodeBuilder;
import uk.co.strangeskies.modabi.model.building.PropertyNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.model.building.SimpleElementNodeConfigurator;

public abstract class BranchingNodeConfiguratorImpl<S extends BranchingNodeConfigurator<S, N>, N extends BranchingNode>
		extends SchemaNodeConfiguratorImpl<S, N> implements
		BranchingNodeConfigurator<S, N>, BranchingNode {
	protected static abstract class BranchingNodeImpl extends SchemaNodeImpl
			implements BranchingNode {
		private final List<SchemaNode> children;

		public BranchingNodeImpl(String id, List<SchemaNode> children) {
			super(id);

			this.children = new ArrayList<>(children);
		}

		@Override
		public final List<SchemaNode> getChildren() {
			return children;
		}
	}

	private final List<SchemaNode> children;

	public BranchingNodeConfiguratorImpl(NodeBuilderContext context) {
		super(context);

		children = new ArrayList<>();
		getContext().pushBranch(children);
	}

	@Override
	protected void created(N created) {
		getContext().popBranch();
		super.created(created);
	}

	@Override
	public NodeBuilder addChild() {
		super.assertBranchable();

		NodeBuilder builder = childBuilder();

		return builder;
	}

	@Override
	public BranchingNodeConfigurator<S, N> addChild(
			Function<NodeBuilder, SchemaNodeConfigurator<?, ?>> builder) {
		super.assertBranchable();

		builder.apply(childBuilder()).create();

		return this;
	}

	private NodeBuilder childBuilder() {
		return new NodeBuilder() {
			@Override
			public SimpleElementNodeConfigurator<Object> simpleElement() {
				return new SimpleElementNodeConfiguratorImpl<>(getContext());
			}

			@Override
			public SequenceNodeConfigurator sequence() {
				return new SequenceNodeConfiguratorImpl(getContext());
			}

			@Override
			public PropertyNodeConfigurator<Object> property() {
				return new PropertyNodeConfiguratorImpl<>(getContext());
			}

			@Override
			public ElementNodeConfigurator<Object> element() {
				return new ElementNodeConfiguratorImpl<>(getContext());
			}

			@Override
			public ContentNodeConfigurator<Object> content() {
				return new ContentNodeConfiguratorImpl<>(getContext());
			}

			@Override
			public ChoiceNodeConfigurator choice() {
				return new ChoiceNodeConfiguratorImpl(getContext());
			}
		};
	}

	@Override
	protected void assertConfigurable() {
		super.assertConfigurable();
		if (!children.isEmpty() || !getContext().isConfiguratorActive(this))
			throw new InvalidBuildStateException(this);
	}

	@Override
	public final List<SchemaNode> getChildren() {
		return children;
	}
}
