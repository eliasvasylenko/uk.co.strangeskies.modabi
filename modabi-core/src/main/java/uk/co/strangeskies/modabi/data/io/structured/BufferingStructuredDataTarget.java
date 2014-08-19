package uk.co.strangeskies.modabi.data.io.structured;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.TerminatingDataSource;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

/**
 * It shouldn't matter what order attributes are added to a child, or whether
 * they are added before, after, or between other children. Because of this,
 * {@link BufferingStructuredDataTarget} does not produce a
 * {@link BufferedStructuredDataSource} which tries to match input order.
 * Instead, in an effort to make it easier for consumers to deal with stream
 * order, it actually adds a guarantee that attributes will appear before any
 * other children types when piped. Similarly, it guarantees that all global
 * namespace hints will be piped before the rest of the document begins, and
 * non-global hints will be piped before any children of the child they occur
 * in.
 * 
 * @author eli
 *
 */
public class BufferingStructuredDataTarget implements StructuredDataTarget {
	private static class BufferedStructuredDataSourceImpl implements
			BufferedStructuredDataSource {
		private final Namespace defaultNamespaceHint;
		private final Set<Namespace> namespaceHints;

		private final BufferedStructuredData root;
		private final Deque<BufferedStructuredData> stack;

		public BufferedStructuredDataSourceImpl(BufferedStructuredData root,
				Namespace defaultNamespaceHint, Set<Namespace> namespaceHints) {
			this(root, new ArrayDeque<>(Arrays.asList(root)), defaultNamespaceHint,
					namespaceHints);
		}

		public BufferedStructuredDataSourceImpl(BufferedStructuredData root,
				Deque<BufferedStructuredData> stack, Namespace defaultNamespaceHint,
				Set<Namespace> namespaceHints) {
			this.root = root;
			this.stack = stack;
			this.defaultNamespaceHint = defaultNamespaceHint;
			this.namespaceHints = namespaceHints;
		}

		@Override
		public Namespace globalDefaultNamespaceHint() {
			return defaultNamespaceHint;
		}

		@Override
		public Set<Namespace> globalNamespaceHints() {
			return namespaceHints;
		}

		@Override
		public Namespace defaultNamespaceHint() {
			return stack.peek().defaultNamespaceHint();
		}

		@Override
		public Set<Namespace> namespaceHints() {
			return stack.peek().namespaceHints();
		}

		@Override
		public TerminatingDataSource propertyData(QualifiedName name) {
			return stack.peek().propertyData(name);
		}

		@Override
		public Set<QualifiedName> properties() {
			return stack.peek().properties();
		}

		@Override
		public QualifiedName nextChild() {
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
					root, stack, defaultNamespaceHint, namespaceHints);
			return copy;
		}

		@Override
		public boolean equals(Object that) {
			if (!(that instanceof BufferedStructuredDataSource))
				return false;

			BufferedStructuredDataSource thatCopy = (BufferedStructuredDataSource) that;

			if (!Objects.equals(globalDefaultNamespaceHint(),
					thatCopy.globalDefaultNamespaceHint()))
				return false;
			if (!Objects.equals(globalNamespaceHints(),
					thatCopy.globalNamespaceHints()))
				return false;

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
		private final QualifiedName name;

		private final Namespace defaultNamespaceHint;
		private final Set<Namespace> namespaceHints;

		private final Map<QualifiedName, BufferedDataSource> properties;
		private final BufferedDataSource content;

		private final List<BufferedStructuredData> children;
		private int childIndex;

		public BufferedStructuredData(BufferingStructuredData from) {
			name = from.name;

			defaultNamespaceHint = from.defaultNamespaceHint;
			namespaceHints = new HashSet<>(from.namespaceHints);

			children = from.children.stream().map(b -> new BufferedStructuredData(b))
					.collect(Collectors.toList());
			childIndex = 0;

			properties = new HashMap<>();
			for (Map.Entry<QualifiedName, BufferingDataTarget> property : from.properties
					.entrySet())
				properties.put(property.getKey(), property.getValue().buffer());
			content = from.content == null ? null : from.content.buffer();
		}

		public Set<Namespace> namespaceHints() {
			return namespaceHints;
		}

		public Namespace defaultNamespaceHint() {
			return defaultNamespaceHint;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BufferedStructuredData))
				return false;
			BufferedStructuredData that = (BufferedStructuredData) obj;

			return super.equals(obj) && childIndex == that.childIndex
					&& Objects.equals(defaultNamespaceHint, that.defaultNamespaceHint)
					&& Objects.equals(namespaceHints, that.namespaceHints)
					&& Objects.equals(name, that.name)
					&& Objects.equals(properties, that.properties)
					&& Objects.equals(content, that.content)
					&& Objects.equals(children, that.children);
		}

		@Override
		public int hashCode() {
			int hashCode = childIndex;
			if (name != null)
				hashCode += name.hashCode();
			if (properties != null)
				hashCode += properties.hashCode();
			if (content != null)
				hashCode += content.hashCode();
			if (children != null)
				hashCode += children.hashCode();
			return hashCode;
		}

		public BufferedStructuredData nextChild() {
			if (childIndex == children.size())
				return null;
			return children.get(childIndex++);
		}

		public QualifiedName name() {
			return name;
		}

		public Set<QualifiedName> properties() {
			return properties.keySet();
		}

		public BufferedDataSource propertyData(QualifiedName name) {
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
		private final QualifiedName name;

		private Namespace defaultNamespaceHint;
		private final Set<Namespace> namespaceHints = new HashSet<>();

		private final Map<QualifiedName, BufferingDataTarget> properties;
		private BufferingDataTarget content;

		private final List<BufferingStructuredData> children;

		public BufferingStructuredData(QualifiedName name) {
			children = new ArrayList<>();
			properties = new HashMap<>();
			this.name = name;
		}

		public BufferingDataTarget addProperty(QualifiedName name) {
			BufferingDataTarget target = new BufferingDataTarget();
			properties.put(name, target);
			return target;
		}

		public void addChild(BufferingStructuredData element) {
			children.add(element);
		}

		public BufferingDataTarget addContent() {
			content = new BufferingDataTarget();
			return content;
		}
	}

	private final Deque<BufferingStructuredData> stack = new ArrayDeque<>(
			Arrays.asList(new BufferingStructuredData(null)));

	private Namespace defaultNamespaceHint;
	private final Set<Namespace> namespaceHints = new HashSet<>();

	@Override
	public StructuredDataTarget registerDefaultNamespaceHint(Namespace namespace,
			boolean global) {
		if (global)
			if (defaultNamespaceHint != null)
				// TODO more sensible exception
				throw new UnsupportedOperationException();
			else
				defaultNamespaceHint = namespace;
		else if (stack.peek().defaultNamespaceHint != null)
			// TODO more sensible exception
			throw new UnsupportedOperationException();
		else
			stack.peek().defaultNamespaceHint = namespace;

		return this;
	}

	@Override
	public StructuredDataTarget registerNamespaceHint(Namespace namespace,
			boolean global) {
		if (global)
			namespaceHints.add(namespace);
		else
			stack.peek().namespaceHints.add(namespace);

		return this;
	}

	@Override
	public BufferingStructuredDataTarget property(QualifiedName name,
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
	public TerminatingDataTarget property(QualifiedName name) {
		return stack.peek().addProperty(name);
	}

	@Override
	public TerminatingDataTarget content() {
		return stack.peek().addContent();
	}

	@Override
	public BufferingStructuredDataTarget nextChild(QualifiedName name) {
		stack.push(new BufferingStructuredData(name));
		return this;
	}

	@Override
	public BufferingStructuredDataTarget endChild() {
		BufferingStructuredData element = stack.pop();
		stack.peek().addChild(element);
		return this;
	}

	public BufferedStructuredDataSource buffer() {
		return bufferImpl();
	}

	private BufferedStructuredDataSourceImpl bufferImpl() {
		if (stack.size() != 1)
			throw new IllegalStateException();

		return new BufferedStructuredDataSourceImpl(new BufferedStructuredData(
				stack.pop()), defaultNamespaceHint, namespaceHints);
	}
}
