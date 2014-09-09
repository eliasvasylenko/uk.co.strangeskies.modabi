package uk.co.strangeskies.modabi.model.nodes.test;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;

public class SchemaNodeTests {
	@DataProvider(name = "sequences")
	public SequenceNode[][] createEqualSequences() {
		return new SequenceNode[][] {
				{ DummyNodes.sequenceNode("one"), DummyNodes.sequenceNode("one") },

				{ DummyNodes.sequenceNode("one", "two", "three"),
						DummyNodes.sequenceNode("one", "two", "three") },

				{
						DummyNodes.sequenceNode("one",
								DummyNodes.sequenceNode("two", "three")),
						DummyNodes.sequenceNode("one",
								DummyNodes.sequenceNode("two", "three")) },

				{
						DummyNodes.sequenceNode(
								"one",
								DummyNodes.sequenceNode("two", "three", "four"),
								DummyNodes.sequenceNode("five",
										DummyNodes.sequenceNode("six", "seven"),
										DummyNodes.sequenceNode("eight")),
								DummyNodes.sequenceNode("nine")),
						DummyNodes.sequenceNode(
								"one",
								DummyNodes.sequenceNode("two", "three", "four"),
								DummyNodes.sequenceNode("five",
										DummyNodes.sequenceNode("six", "seven"),
										DummyNodes.sequenceNode("eight")),
								DummyNodes.sequenceNode("nine")) } };
	}

	@DataProvider(name = "unequalSequencePairs")
	public SequenceNode[][] createUnequalSequences() {
		return new SequenceNode[][] {
				{ DummyNodes.sequenceNode("one"), DummyNodes.sequenceNode("two") },

				{ DummyNodes.sequenceNode("one", "two", "three"),
						DummyNodes.sequenceNode("one", "two") },

				{
						DummyNodes.sequenceNode("one",
								DummyNodes.sequenceNode("two", "three")),
						DummyNodes.sequenceNode("one", DummyNodes.sequenceNode("two")) },

				{ DummyNodes.sequenceNode("one"), DummyNodes.sequenceNode("two") },

				{ DummyNodes.sequenceNode("one", "two"),
						DummyNodes.sequenceNode("one", "three") } };
	}

	@Test(dataProvider = "sequences")
	public void equalityTest(SequenceNode first, SequenceNode second) {
		Assert.assertEquals(first, second);
		Assert.assertEquals(first.effective(), second.effective());
	}

	@Test(dataProvider = "sequences")
	public void hashCodeTest(SequenceNode first, SequenceNode second) {
		Assert.assertEquals(first.hashCode(), second.hashCode());
		Assert.assertEquals(first.effective().hashCode(), second.effective()
				.hashCode());
	}

	@Test(dataProvider = "unequalSequencePairs")
	public void inequalityTest(SequenceNode first, SequenceNode second) {
		Assert.assertNotEquals(first, second);
		Assert.assertNotEquals(first.effective(), second.effective());
	}
}
