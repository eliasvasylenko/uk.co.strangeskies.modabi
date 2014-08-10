package uk.co.strangeskies.modabi.data.io.structured;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.TerminatingDataSource;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;

public class BufferingStructuredDataTarget implements StructuredDataTarget {
	private static class BufferedStructuredDataSourceImpl implements
			BufferedStructuredDataSource {
		private final BufferedStructuredData root;
		private final Deque<BufferedStructuredData> stack;

		public BufferedStructuredDataSourceImpl(BufferedStructuredData root) {
			this(root, new ArrayDeque<>());
		}

		public BufferedStructuredDataSourceImpl(BufferedStructuredData root,
				Deque<BufferedStructuredData> stack) {
			this.root = root;
			this.stack = stack;
			stack.push(root);
		}

		@Override
		public TerminatingDataSource propertyData(String name) {
			return stack.peek().propertyData(name);
		}

		@Override
		public Set<String> properties() {
			return stack.peek().properties();
		}

		@Override
		public String nextChild() {
			BufferedStructuredData child = stack.peek().nextChild();

			if (child == null)
				return null;

			stack.push(child);
			return stack.peek().name();
		}

		@Override
		public void endChild() {
			stack.pop();
		}

		@Override
		public TerminatingDataSource content() {
			BufferedDataSource content = stack.peek().content();
			return content == null ? null : content;
		}

		@Override
		public void reset() {
			BufferedStructuredData root = stack.getFirst();
			stack.clear();
			stack.add(root);
			root.reset();
		}

		@Override
		public BufferedStructuredDataSourceImpl copy() {
			BufferedStructuredDataSourceImpl copy = new BufferedStructuredDataSourceImpl(
					root, stack);
			return copy;
		}

		@Override
		public boolean equals(Object that) {
			if (!(that instanceof BufferedStructuredDataSource))
				return false;

			BufferedStructuredDataSource thatCopy = (BufferedStructuredDataSource) that;
			if (depth() != thatCopy.depth()
					|| indexAtDepth() != thatCopy.indexAtDepth())
				return false;

			thatCopy = thatCopy.copy();
			thatCopy.reset();

			return root.equals(thatCopy.pipeNextChild(
					new BufferingStructuredDataTarget()).bufferImpl().root);
		}

		@Override
		public int hashCode() {
			return root.hashCode() + depth() + indexAtDepth();
		}

		@Override
		public int depth() {
			return stack.size();
		}

		@Override
		public int indexAtDepth() {
			return stack.peek().childIndex;
		}
	}

	private static class BufferedStructuredData {
		private final String name;

		private final Map<String, BufferedDataSource> properties;
		private final BufferedDataSource content;

		private final List<BufferedStructuredData> children;
		private int childIndex;

		public BufferedStructuredData(BufferingStructuredData from) {
			name = from.name;

			children = from.children.stream().map(b -> new BufferedStructuredData(b))
					.collect(Collectors.toList());
			childIndex = 0;

			properties = new HashMap<>();
			for (Map.Entry<String, BufferingDataTarget> property : from.properties
					.entrySet())
				properties.put(property.getKey(), property.getValue().buffer());
			content = from.content == null ? null : from.content.buffer();
		}

		@Override
		public boolean equals(Object obj) {
			// TODO
			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return name.hashCode() + properties.hashCode() + content.hashCode()
					+ children.hashCode() + childIndex;
		}

		public BufferedStructuredData nextChild() {
			if (childIndex == children.size())
				return null;
			return children.get(childIndex++);
		}

		public String name() {
			return name;
		}

		public Set<String> properties() {
			return properties.keySet();
		}

		public BufferedDataSource propertyData(String name) {
			return properties.get(name);
		}

		public BufferedDataSource content() {
			return content;
		}

		public void reset() {
			content.reset();
			for (BufferedDataSource property : properties.values())
				property.reset();

			childIndex = 0;
			for (BufferedStructuredData child : children)
				child.reset();
		}
	}

	private static class BufferingStructuredData {
		private final String name;

		private final Map<String, BufferingDataTarget> properties;
		private BufferingDataTarget content;

		private final List<BufferingStructuredData> children;

		public BufferingStructuredData(String name) {
			children = new ArrayList<>();
			properties = new HashMap<>();
			this.name = name;
		}

		public void addProperty(String name, BufferingDataTarget target) {
			properties.put(name, target);
		}

		public void addChild(BufferingStructuredData element) {
			children.add(element);
		}

		public void addContent(BufferingDataTarget target) {
			content = target;
		}
	}

	private final Deque<BufferingStructuredData> stack = new ArrayDeque<>(
			Arrays.asList(new BufferingStructuredData(null)));

	@Override
	public BufferingStructuredDataTarget property(String name,
			Function<TerminatingDataTarget, TerminatingDataTarget> targetOperation) {
		return (BufferingStructuredDataTarget) StructuredDataTarget.super.property(
				name, targetOperation);
	}

	@Override
	public BufferingStructuredDataTarget content(
			Function<TerminatingDataTarget, TerminatingDataTarget> targetOperation) {
		return (BufferingStructuredDataTarget) StructuredDataTarget.super
				.content(targetOperation);
	}

	@Override
	public TerminatingDataTarget property(String name) {
		BufferingDataTarget target = new BufferingDataTarget();
		stack.peek().addProperty(name, target);

		return target;
	}

	@Override
	public BufferingStructuredDataTarget endChild() {
		BufferingStructuredData element = stack.pop();
		stack.peek().addChild(element);
		return this;
	}

	@Override
	public TerminatingDataTarget content() {
		BufferingDataTarget target = new BufferingDataTarget();
		stack.peek().addContent(target);

		return target;
	}

	@Override
	public BufferingStructuredDataTarget nextChild(String name) {
		stack.push(new BufferingStructuredData(name));
		return this;
	}

	public BufferedStructuredDataSource buffer() {
		return bufferImpl();
	}

	private BufferedStructuredDataSourceImpl bufferImpl() {
		if (stack.size() != 1)
			throw new IllegalStateException();

		return new BufferedStructuredDataSourceImpl(new BufferedStructuredData(
				stack.pop()));
	}
}
