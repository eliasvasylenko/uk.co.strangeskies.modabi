package uk.co.strangeskies.modabi.model.nodes.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;

public class DummyNodes {
	private DummyNodes() {
	}

	public static SequenceNode sequenceNode(String name, String... children) {
		return sequenceNode(new QualifiedName(name), Arrays.asList(children)
				.stream().map(DummyNodes::sequenceNode).collect(Collectors.toList()));
	}

	public static SequenceNode sequenceNode(QualifiedName name,
			QualifiedName... children) {
		return sequenceNode(
				name,
				Arrays.asList(children).stream().map(DummyNodes::sequenceNode)
						.collect(Collectors.toList()));
	}

	public static SequenceNode sequenceNode(String name,
			ChildNode<?, ?>... children) {
		return sequenceNode(new QualifiedName(name), children);
	}

	public static SequenceNode sequenceNode(QualifiedName name,
			ChildNode<?, ?>... children) {
		return sequenceNode(name, Arrays.asList(children));
	}

	public static SequenceNode sequenceNode(QualifiedName name,
			List<? extends ChildNode<?, ?>> children) {
		return new SequenceNode() {
			@Override
			public QualifiedName getName() {
				return name;
			}

			@Override
			public boolean isAbstract() {
				return false;
			}

			@Override
			public List<? extends ChildNode<?, ?>> children() {
				return children;
			}

			@Override
			public boolean equals(Object object) {
				return propertySet().testEquality(object)
						&& effective().equals(((SchemaNode<?, ?>) object).effective());
			}

			@Override
			public int hashCode() {
				return propertySet().generateHashCode();
			}

			@Override
			public Effective effective() {
				SequenceNode thisNode = this;

				return new Effective() {
					@Override
					public QualifiedName getName() {
						return name;
					}

					@Override
					public boolean equals(Object object) {
						return propertySet().testEquality(object)
								&& effectivePropertySet().testEquality(object);
					}

					@Override
					public boolean isAbstract() {
						return false;
					}

					@Override
					public int hashCode() {
						return propertySet().generateHashCode()
								^ effectivePropertySet().generateHashCode();
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
		return sequenceNode(new QualifiedName(name));
	}

	public static SequenceNode sequenceNode(QualifiedName name) {
		return new SequenceNode.Effective() {
			@Override
			public QualifiedName getName() {
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
			public boolean isAbstract() {
				return false;
			}

			@Override
			public boolean equals(Object object) {
				return propertySet().testEquality(object)
						&& effectivePropertySet().testEquality(object);
			}

			@Override
			public int hashCode() {
				return propertySet().generateHashCode()
						^ effectivePropertySet().generateHashCode();
			}
		};
	}
}
