/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.model.nodes.test;

import org.junit.Assert;

import uk.co.strangeskies.modabi.schema.SequenceNode;

public class SchemaNodeTest {
	// @DataProvider(name = "sequences")
	public SequenceNode[][] createEqualSequences() {
		return new SequenceNode[][] { { DummyNodes.sequenceNode("one"), DummyNodes.sequenceNode("one") },

				{ DummyNodes.sequenceNode("one", "two", "three"), DummyNodes.sequenceNode("one", "two", "three") },

				{ DummyNodes.sequenceNode("one", DummyNodes.sequenceNode("two", "three")),
						DummyNodes.sequenceNode("one", DummyNodes.sequenceNode("two", "three")) },

				{ DummyNodes.sequenceNode("one", DummyNodes.sequenceNode("two", "three", "four"),
						DummyNodes.sequenceNode("five", DummyNodes.sequenceNode("six", "seven"), DummyNodes.sequenceNode("eight")),
						DummyNodes.sequenceNode("nine")),
						DummyNodes.sequenceNode("one",
								DummyNodes.sequenceNode("two", "three", "four"), DummyNodes.sequenceNode("five",
										DummyNodes.sequenceNode("six", "seven"), DummyNodes.sequenceNode("eight")),
								DummyNodes.sequenceNode("nine")) } };
	}

	// @DataProvider(name = "unequalSequencePairs")
	public SequenceNode[][] createUnequalSequences() {
		return new SequenceNode[][] { { DummyNodes.sequenceNode("one"), DummyNodes.sequenceNode("two") },

				{ DummyNodes.sequenceNode("one", "two", "three"), DummyNodes.sequenceNode("one", "two") },

				{ DummyNodes.sequenceNode("one", DummyNodes.sequenceNode("two", "three")),
						DummyNodes.sequenceNode("one", DummyNodes.sequenceNode("two")) },

				{ DummyNodes.sequenceNode("one"), DummyNodes.sequenceNode("two") },

				{ DummyNodes.sequenceNode("one", "two"), DummyNodes.sequenceNode("one", "three") } };
	}

	// @Test(dataProvider = "sequences")
	public void equalityTest(SequenceNode first, SequenceNode second) {
		Assert.assertEquals(first, second);
		Assert.assertEquals(first, second);
	}

	// @Test(dataProvider = "sequences")
	public void hashCodeTest(SequenceNode first, SequenceNode second) {
		Assert.assertEquals(first.hashCode(), second.hashCode());
		Assert.assertEquals(first.hashCode(), second.hashCode());
	}

	// @Test(dataProvider = "unequalSequencePairs")
	public void inequalityTest(SequenceNode first, SequenceNode second) {
		Assert.assertNotEquals(first, second);
		Assert.assertNotEquals(first, second);
	}
}
