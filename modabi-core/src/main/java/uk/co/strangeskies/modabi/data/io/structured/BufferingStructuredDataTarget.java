package uk.co.strangeskies.modabi.data.io.structured;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.data.io.DataTarget;
import uk.co.strangeskies.modabi.data.io.IOException;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredData.BufferedStructuredData;
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
 * TODO more exceptions when things are done illegally out of order.
 *
 * @author eli
 *
 */
public class BufferingStructuredDataTarget extends
		StructuredDataTargetDecorator {
	public BufferingStructuredDataTarget() {
		super(new BufferingStructuredDataTargetImpl());
	}

	public BufferedStructuredDataSource buffer() {
		return ((BufferingStructuredDataTargetImpl) getComponent()).buffer();
	}

	@Override
	public BufferingStructuredDataTarget nextChild(QualifiedName name) {
		super.nextChild(name);
		return this;
	}

	@Override
	public BufferingStructuredDataTarget writeProperty(QualifiedName name,
			Function<DataTarget, DataTarget> targetOperation) {
		super.writeProperty(name, targetOperation);
		return this;
	}

	@Override
	public BufferingStructuredDataTarget writeContent(
			Function<DataTarget, DataTarget> targetOperation) {
		writeContent(targetOperation);
		return this;
	}

	@Override
	public BufferingStructuredDataTarget comment(String comment) {
		super.comment(comment);
		return this;
	}

	@Override
	public BufferingStructuredDataTarget endChild() {
		super.endChild();
		return this;
	}

	@Override
	public BufferingStructuredDataTarget registerDefaultNamespaceHint(
			Namespace namespace) {
		super.registerDefaultNamespaceHint(namespace);
		return this;
	}

	@Override
	public BufferingStructuredDataTarget registerNamespaceHint(Namespace namespace) {
		super.registerNamespaceHint(namespace);
		return this;
	}
}

class BufferingStructuredDataTargetImpl implements StructuredDataTarget {
	private final Deque<BufferingStructuredData> stack = new ArrayDeque<>(
			Arrays.asList(new BufferingStructuredData(null)));

	private Namespace defaultNamespaceHint;
	private final Set<Namespace> namespaceHints = new HashSet<>();

	private final Set<String> comments = new HashSet<>();

	@Override
	public StructuredDataState currentState() {
		return null;
	}

	@Override
	public StructuredDataTarget registerDefaultNamespaceHint(Namespace namespace) {
		if (stack.isEmpty())
			if (defaultNamespaceHint != null)
				throw new IOException(
						"Cannot register multiple default namespace hints at any given location.");
			else
				defaultNamespaceHint = namespace;
		else
			stack.peek().setDefaultNamespaceHint(namespace);

		return this;
	}

	@Override
	public StructuredDataTarget registerNamespaceHint(Namespace namespace) {
		if (stack.isEmpty())
			namespaceHints.add(namespace);
		else
			stack.peek().addNamespaceHint(namespace);

		return this;
	}

	@Override
	public BufferingStructuredDataTarget writeProperty(QualifiedName name,
			Function<DataTarget, DataTarget> targetOperation) {
		return (BufferingStructuredDataTarget) StructuredDataTarget.super
				.writeProperty(name, targetOperation);
	}

	@Override
	public BufferingStructuredDataTarget writeContent(
			Function<DataTarget, DataTarget> targetOperation) {
		return (BufferingStructuredDataTarget) StructuredDataTarget.super
				.writeContent(targetOperation);
	}

	@Override
	public DataTarget writeProperty(QualifiedName name) {
		return stack.peek().addProperty(name);
	}

	@Override
	public DataTarget writeContent() {
		return stack.peek().addContent();
	}

	@Override
	public StructuredDataTarget nextChild(QualifiedName name) {
		stack.push(new BufferingStructuredData(name));
		return this;
	}

	@Override
	public StructuredDataTarget endChild() {
		BufferingStructuredData element = stack.pop();
		stack.peek().addChild(element);
		return this;
	}

	public BufferedStructuredDataSourceImpl buffer() {
		if (stack.size() != 1)
			throw new IllegalStateException("Stack depth '" + stack.size()
					+ "' should be 1.");

		return new BufferedStructuredDataSourceImpl(new BufferedStructuredData(
				stack.pop()), defaultNamespaceHint, namespaceHints, comments);
	}

	@Override
	public StructuredDataTarget comment(String comment) {
		if (stack.isEmpty())
			comments.add(comment);
		else
			stack.peek().comment(comment);

		return this;
	}
}

class BufferedStructuredDataSourceImpl implements BufferedStructuredDataSource {
	private final Namespace defaultNamespaceHint;
	private final Set<Namespace> namespaceHints;

	private final Set<String> comments;

	private final BufferedStructuredData root;
	private final Deque<BufferedStructuredData> stack;

	public BufferedStructuredDataSourceImpl(BufferedStructuredData root,
			Namespace defaultNamespaceHint, Set<Namespace> namespaceHints,
			Set<String> comments) {
		this(root, new ArrayDeque<>(Arrays.asList(root)), defaultNamespaceHint,
				namespaceHints, comments);
	}

	public BufferedStructuredDataSourceImpl(BufferedStructuredData root,
			Deque<BufferedStructuredData> stack, Namespace defaultNamespaceHint,
			Set<Namespace> namespaceHints, Set<String> comments) {
		this.root = root;
		this.stack = stack;
		this.defaultNamespaceHint = defaultNamespaceHint;
		this.namespaceHints = namespaceHints;
		this.comments = comments;
	}

	@Override
	public Namespace getDefaultNamespaceHint() {
		if (stack.isEmpty())
			return defaultNamespaceHint;
		else
			return stack.peek().defaultNamespaceHint();
	}

	@Override
	public Set<Namespace> getNamespaceHints() {
		if (stack.isEmpty())
			return namespaceHints;
		else
			return stack.peek().namespaceHints();
	}

	@Override
	public Set<String> getComments() {
		if (stack.isEmpty())
			return comments;
		else
			return stack.peek().comments();
	}

	@Override
	public DataSource readProperty(QualifiedName name) {
		return stack.peek().propertyData(name);
	}

	@Override
	public Set<QualifiedName> getProperties() {
		return stack.peek().properties();
	}

	@Override
	public QualifiedName startNextChild() {
		BufferedStructuredData child = stack.peek().nextChild();

		if (child == null)
			return null;

		stack.push(child);
		return stack.peek().name();
	}

	@Override
	public QualifiedName peekNextChild() {
		return stack.peek().peekNextChild();
	}

	@Override
	public boolean hasNextChild() {
		return stack.peek().hasNextChild();
	}

	@Override
	public void endChild() {
		stack.pop();
	}

	@Override
	public DataSource readContent() {
		DataSource content = stack.peek().content();
		return content == null ? null : content;
	}

	@Override
	public void reset() {
		stack.clear();
		stack.push(root);
		root.reset();
	}

	@Override
	public BufferedStructuredDataSourceImpl copy() {
		BufferedStructuredDataSourceImpl copy = new BufferedStructuredDataSourceImpl(
				root, stack, defaultNamespaceHint, namespaceHints, comments);
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

		if (!Objects.equals(defaultNamespaceHint,
				thatCopy.getDefaultNamespaceHint()))
			return false;
		if (!Objects.equals(namespaceHints, thatCopy.getNamespaceHints()))
			return false;

		return root.equals(((BufferedStructuredDataSourceImpl) thatCopy
				.pipeNextChild(new BufferingStructuredDataTarget()).buffer()).root);
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
		return stack.peek().childIndex();
	}

	@Override
	public StructuredDataState currentState() {
		return null;
	}
}

class BufferingStructuredData {
	private final QualifiedName name;

	private Namespace defaultNamespaceHint;
	private final Set<Namespace> namespaceHints;

	private final Map<QualifiedName, BufferingDataTarget> properties;
	private BufferingDataTarget content;

	private final List<BufferingStructuredData> children;

	private final Set<String> comments;

	public BufferingStructuredData(QualifiedName name) {
		namespaceHints = new HashSet<>();
		comments = new HashSet<>();

		children = new ArrayList<>();
		properties = new LinkedHashMap<>();
		this.name = name;
	}

	public void comment(String comment) {
		comments.add(comment);
	}

	public void addNamespaceHint(Namespace namespace) {
		namespaceHints.add(namespace);
	}

	public void setDefaultNamespaceHint(Namespace namespace) {
		if (defaultNamespaceHint != null)
			throw new IOException(
					"Cannot register multiple default namespace hints at any given location.");
		defaultNamespaceHint = namespace;
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

	public BufferedStructuredData buffer() {
		return new BufferedStructuredData(this);
	}

	public static class BufferedStructuredData {
		private final QualifiedName name;

		private final Namespace defaultNamespaceHint;
		private final Set<Namespace> namespaceHints;

		private final Set<String> comments;

		private final Map<QualifiedName, DataSource> properties;
		private final DataSource content;

		private final List<BufferedStructuredData> children;
		private int childIndex;

		public BufferedStructuredData(BufferingStructuredData from) {
			name = from.name;

			defaultNamespaceHint = from.defaultNamespaceHint;
			namespaceHints = new HashSet<>(from.namespaceHints);

			comments = new HashSet<>(from.comments);

			children = from.children.stream().map(b -> new BufferedStructuredData(b))
					.collect(Collectors.toList());
			childIndex = 0;

			properties = new LinkedHashMap<>();
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

		public Set<String> comments() {
			return comments;
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

		public QualifiedName peekNextChild() {
			if (childIndex == children.size())
				return null;
			return children.get(childIndex).name();
		}

		public boolean hasNextChild() {
			return childIndex < children.size();
		}

		public QualifiedName name() {
			return name;
		}

		public Set<QualifiedName> properties() {
			return properties.keySet();
		}

		public DataSource propertyData(QualifiedName name) {
			return properties.get(name);
		}

		public DataSource content() {
			return content;
		}

		public int childIndex() {
			return childIndex;
		}

		public void reset() {
			if (content != null)
				content.reset();

			for (DataSource property : properties.values())
				property.reset();

			childIndex = 0;
			for (BufferedStructuredData child : children)
				child.reset();
		}
	}
}
