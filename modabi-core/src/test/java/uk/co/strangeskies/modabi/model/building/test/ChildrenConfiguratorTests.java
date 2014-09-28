package uk.co.strangeskies.modabi.model.building.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import uk.co.strangeskies.modabi.model.nodes.test.DummyNodes;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.model.building.impl.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;

public class ChildrenConfiguratorTests {
	private class MergeTestData {
		private List<SequenceNode> sequences;
		private List<QualifiedName> overrides;
		private List<QualifiedName> expected;

		MergeTestData(List<SequenceNode> sequences, List<QualifiedName> overrides,
				List<QualifiedName> expected) {
			this.sequences = new ArrayList<>(sequences);
			this.overrides = overrides;
			this.expected = expected;
		}

		public List<SequenceNode> sequences() {
			return sequences;
		}

		public List<QualifiedName> overrides() {
			return overrides;
		}

		public List<QualifiedName> expected() {
			return expected;
		}
	}

	private static List<QualifiedName> qualifyNames(String... names) {
		return Arrays.asList(names).stream().map(QualifiedName::new)
				.collect(Collectors.toList());
	}

	@DataProvider(name = "mergeData")
	public Object[][] createMergeTestData() {
		return new Object[][] {
				{ new MergeTestData(Arrays.asList(
						DummyNodes.sequenceNode("first", "a", "b", "c"),
						DummyNodes.sequenceNode("second", "1", "2", "3")), qualifyNames(),
						qualifyNames("a", "b", "c", "1", "2", "3")) },

				{ new MergeTestData(Arrays.asList(
						DummyNodes.sequenceNode("first", "a", "b", "c"),
						DummyNodes.sequenceNode("second", "1", "2", "3"),
						DummyNodes.sequenceNode("third", "uno", "dos", "tres")),
						qualifyNames(), qualifyNames("a", "b", "c", "1", "2", "3", "uno",
								"dos", "tres")) },

				{ new MergeTestData(Arrays.asList(
						DummyNodes.sequenceNode("first", "a", "b", "c", "d"),
						DummyNodes.sequenceNode("second", "1", "a", "2", "3", "c", "4")),
						qualifyNames("a", "c"), qualifyNames("1", "a", "b", "2", "3", "c",
								"d", "4")) } };
	}

	@Test(dataProvider = "mergeData")
	public void childrenMergeTest(MergeTestData mergeTestData) {
		SequentialChildrenConfigurator<ChildNode<?, ?>, BindingChildNode<?, ?, ?>> configurator = new SequentialChildrenConfigurator<ChildNode<?, ?>, BindingChildNode<?, ?, ?>>(
				Namespace.getDefault(), mergeTestData.sequences(), true, Object.class,
				Object.class, null, true, false);

		for (QualifiedName override : mergeTestData.overrides())
			configurator.addChild().sequence().name(override).create();

		List<QualifiedName> result = configurator.create().getEffectiveChildren()
				.stream().map(SchemaNode::getName).collect(Collectors.toList());

		for (SequenceNode sequenceNode : mergeTestData.sequences())
			System.out.println("Node '"
					+ sequenceNode.getName()
					+ "': ["
					+ sequenceNode.children().stream().map(s -> s.getName().toString())
							.collect(Collectors.joining(", ")) + "]");
		System.out.println("Expected: " + mergeTestData.expected());
		System.out.println("Result: " + result);
		System.out.println();

		Assert.assertEquals(mergeTestData.expected(), result);
	}
}
