package uk.co.strangeskies.modabi.model.nodes.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;

public class DummyNodes {
	private DummyNodes() {
	}

	public static SequenceNode sequenceNode(String name, String... children) {
		return sequenceNode(
				name,
				Arrays.asList(children).stream().map(DummyNodes::sequenceNode)
						.collect(Collectors.toList()));
	}

	public static SequenceNode sequenceNode(String name,
			ChildNode<?, ?>... children) {
		return sequenceNode(name, Arrays.asList(children));
	}

	public static SequenceNode sequenceNode(String name,
			List<? extends ChildNode<?, ?>> children) {
		return new SequenceNode() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<? extends ChildNode<?, ?>> children() {
				return children;
			}

			@Override
			public boolean equals(Object object) {
				return equalsImpl(object);
			}

			@Override
			public int hashCode() {
				return hashCodeImpl();
			}

			@Override
			public Effective effective() {
				SequenceNode thisNode = this;

				return new Effective() {
					@Override
					public String getName() {
						return name;
					}

					@Override
					public boolean equals(Object object) {
						return equalsImpl(object);
					}

					@Override
					public int hashCode() {
						return hashCodeImpl();
					}

					@Override
					public List<ChildNode.Effective<?, ?>> children() {
						/*
						 * TODO Yet another compiler bug to report? Should be able to supply
						 * SchemaNode::effective as the argument to map, and leave out the
						 * explicit parametrisation,but javac gets upset.
						 */
						return children.stream()
								.<ChildNode.Effective<?, ?>> map(c -> c.effective())
								.collect(Collectors.toList());
					}

					@Override
					public SequenceNode source() {
						return thisNode;
					}
				};
			}
		};
	}

	public static SequenceNode sequenceNode(String name) {
		return new SequenceNode.Effective() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<ChildNode.Effective<?, ?>> children() {
				return Collections.emptyList();
			}

			@Override
			public SequenceNode source() {
				return this;
			}

			@Override
			public boolean equals(Object object) {
				return equalsImpl(object);
			}

			@Override
			public int hashCode() {
				return hashCodeImpl();
			}
		};
	}
}
