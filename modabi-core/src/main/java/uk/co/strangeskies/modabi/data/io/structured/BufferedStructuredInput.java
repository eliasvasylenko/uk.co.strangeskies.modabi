package uk.co.strangeskies.modabi.data.io.structured;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.TerminatingDataSource;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;

public interface BufferedStructuredInput extends StructuredInput {
	void reset();

	static BufferingStructuredOutput from() {
		return new BufferingStructuredOutput() {
			class BufferedStructuredData {
				private final String name;

				private final List<BufferedStructuredData> children;
				private int childIndex;

				private final Map<String, BufferingDataTarget> properties;
				private BufferingDataTarget content;

				public BufferedStructuredData(String name) {
					children = new ArrayList<>();
					properties = new HashMap<>();
					childIndex = 0;
					this.name = name;
				}

				public BufferedStructuredData nextChild() {
					return children.get(childIndex++);
				}

				public String getName() {
					return name;
				}

				public Set<String> properties() {
					return properties.keySet();
				}

				public BufferingDataTarget propertyData(String name) {
					return properties.get(name);
				}

				public BufferingDataTarget content() {
					return content;
				}

				public void reset() {
					for (BufferedStructuredData child : children)
						child.reset();
				}

				public void addProperty(String name, BufferingDataTarget target) {
					properties.put(name, target);
				}

				public void addChild(BufferedStructuredData element) {
					children.add(element);
				}

				public void addContent(BufferingDataTarget target) {
					content = target;
				}
			}

			private final Deque<BufferedStructuredData> outputStack = new ArrayDeque<>(
					Arrays.asList(new BufferedStructuredData(null)));

			@Override
			public TerminatingDataTarget property(String name) {
				BufferingDataTarget target = BufferedDataSource.from();
				outputStack.peek().addProperty(name, target);

				return target;
			}

			@Override
			public void endElement() {
				BufferedStructuredData element = outputStack.pop();
				outputStack.peek().addChild(element);
			}

			@Override
			public TerminatingDataTarget content() {
				BufferingDataTarget target = BufferedDataSource.from();
				outputStack.peek().addContent(target);

				return target;
			}

			@Override
			public void childElement(String name) {
				outputStack.push(new BufferedStructuredData(name));
			}

			@Override
			public BufferedStructuredInput buffer() {
				if (outputStack.size() != 1)
					throw new IllegalStateException();

				Deque<BufferedStructuredData> inputStack = new ArrayDeque<>();
				inputStack.push(outputStack.pop());

				Map<String, TerminatingDataSource> bufferedProperties = new HashMap<>();

				return new BufferedStructuredInput() {
					@Override
					public TerminatingDataSource propertyData(String name) {
						TerminatingDataSource property = bufferedProperties.get(name);

						if (property == null)
							bufferedProperties.put(name, property = inputStack.peek()
									.propertyData(name).buffer());

						return property;
					}

					@Override
					public Set<String> properties() {
						return inputStack.peek().properties();
					}

					@Override
					public String nextChild() {
						bufferedProperties.clear();

						BufferedStructuredData child = inputStack.peek().nextChild();

						if (child == null)
							return null;

						inputStack.push(child);
						return inputStack.peek().getName();
					}

					@Override
					public void endElement() {
						inputStack.pop();
					}

					@Override
					public TerminatingDataSource content() {
						return inputStack.peek().content().buffer();
					}

					@Override
					public void reset() {
						BufferedStructuredData root = inputStack.getFirst();
						inputStack.clear();
						inputStack.add(root);
						root.reset();
					}
				};
			}
		};
	}
}
