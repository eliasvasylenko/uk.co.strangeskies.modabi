/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.model.building.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.SchemaNodeConfigurationContext;
import uk.co.strangeskies.modabi.impl.schema.utilities.SequentialChildrenConfigurator;
import uk.co.strangeskies.modabi.model.nodes.test.DummyNodes;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.TypeToken;

@RunWith(Theories.class)
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

	@DataPoints
	public List<MergeTestData> createMergeTestData() {
		return Arrays.asList(

				new MergeTestData(Arrays.asList(
						DummyNodes.sequenceNode("first", "a", "b", "c"),
						DummyNodes.sequenceNode("second", "1", "2", "3")), qualifyNames(),
						qualifyNames("a", "b", "c", "1", "2", "3")),

				new MergeTestData(Arrays.asList(
						DummyNodes.sequenceNode("first", "a", "b", "c"),
						DummyNodes.sequenceNode("second", "1", "2", "3"),
						DummyNodes.sequenceNode("third", "uno", "dos", "tres")),
						qualifyNames(), qualifyNames("a", "b", "c", "1", "2", "3", "uno",
								"dos", "tres")),

				new MergeTestData(Arrays.asList(
						DummyNodes.sequenceNode("first", "a", "b", "c", "d"),
						DummyNodes.sequenceNode("second", "1", "a", "2", "3", "c", "4")),
						qualifyNames("a", "c"), qualifyNames("1", "a", "b", "2", "3", "c",
								"d", "4")));
	}

	@Theory
	public void childrenMergeTest(MergeTestData mergeTestData) {
		BoundSet boundSet = new BoundSet();
		SequentialChildrenConfigurator configurator = new SequentialChildrenConfigurator(
				new SchemaNodeConfigurationContext<ChildNode<?, ?>>() {
					@Override
					public BoundSet boundSet() {
						return boundSet;
					}

					@Override
					public DataLoader dataLoader() {
						return null;
					}

					@Override
					public boolean isAbstract() {
						return false;
					}

					@Override
					public boolean isInputDataOnly() {
						return true;
					}

					@Override
					public boolean isInputExpected() {
						return true;
					}

					@Override
					public boolean isConstructorExpected() {
						return false;
					}

					@Override
					public boolean isStaticMethodExpected() {
						return false;
					}

					@Override
					public Namespace namespace() {
						return Namespace.getDefault();
					}

					@Override
					public TypeToken<?> inputTargetType(QualifiedName node) {
						return TypeToken.over(Object.class);
					}

					@Override
					public TypeToken<?> outputSourceType() {
						return TypeToken.over(Object.class);
					}

					@Override
					public void addChild(ChildNode<?, ?> result) {}

					@Override
					public <U extends ChildNode<?, ?>> List<U> overrideChild(
							QualifiedName id, TypeToken<U> nodeClass) {
						return null;
					}

					@Override
					public List<? extends SchemaNode<?, ?>> overriddenNodes() {
						return mergeTestData.sequences();
					}
				});

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
