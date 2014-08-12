package uk.co.strangeskies.modabi.model.building.test;

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
				Arrays.asList(children).stream().map(DummyNodes::schemaNode)
						.collect(Collectors.toList()));
	}

	public static SequenceNode sequenceNode(String name,
			List<? extends ChildNode<?>> children) {
		return new SequenceNode() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<? extends ChildNode<?>> children() {
				return children;
			}

			@Override
			public Effective effective() {
				return new Effective() {
					@Override
					public String getName() {
						return name;
					}

					@Override
					public List<ChildNode.Effective<?>> children() {
						/*
						 * TODO Yet another compiler bug to report? Should be able to supply
						 * SchemaNode::effective as the argument to map, and leave out the
						 * explicit parametrisation,but javac gets upset.
						 */
						return children.stream()
								.<ChildNode.Effective<?>> map(c -> c.effective())
								.collect(Collectors.toList());
					}
				};
			}
		};
	}

	public static SequenceNode schemaNode(String name) {
		return new SequenceNode.Effective() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<ChildNode.Effective<?>> children() {
				return Collections.emptyList();
			}
		};
	}
}
