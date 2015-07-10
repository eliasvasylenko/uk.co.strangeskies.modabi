/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.io.structured;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.IOException;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredData.BufferedStructuredData;

/**
 * It shouldn't matter in what order attributes are added to a child, or whether
 * they are added before, after, or between other children. Because of this,
 * {@link BufferingStructuredDataTarget} does not produce a
 * {@link BufferedStructuredDataSource} which tries to match input order.
 * Instead, in an effort to make it easier for consumers to deal with stream
 * order, it adds a guarantee that buffered attributes will appear before any
 * other children types when piped. Similarly, it guarantees that all global
 * namespace hints will be piped before the rest of the document begins, and
 * non-global hints will be piped before any children of the child they occur
 * in.
 *
 * @author Elias N Vasylenko
 *
 */
public class BufferingStructuredDataTarget extends
		StructuredDataTargetImpl<BufferingStructuredDataTarget> {
	private int startDepth = 0;
	private final Deque<BufferingStructuredData> stack = new ArrayDeque<>(
			Arrays.asList(new BufferingStructuredData(null)));

	@Override
	public void registerDefaultNamespaceHintImpl(Namespace namespace) {
		stack.peek().setDefaultNamespaceHint(namespace);
	}

	@Override
	public void registerNamespaceHintImpl(Namespace namespace) {
		stack.peek().addNamespaceHint(namespace);
	}

	@Override
	public DataTarget writePropertyImpl(QualifiedName name) {
		return stack.peek().addProperty(name);
	}

	@Override
	public DataTarget writeContentImpl() {
		return stack.peek().addContent();
	}

	@Override
	public void nextChildImpl(QualifiedName name) {
		stack.push(new BufferingStructuredData(name));
	}

	@Override
	public void endChildImpl() {
		BufferingStructuredData element = stack.pop();
		if (stack.isEmpty()) {
			startDepth++;
			stack.addFirst(new BufferingStructuredData(null));
		}
		stack.peek().addChild(element);
	}

	public BufferedStructuredDataSource buffer(boolean linked, boolean consumable) {
		return new BufferedStructuredDataSourceImpl(stack.getFirst().buffer(linked,
				consumable), startDepth);
	}

	public BufferedStructuredDataSource buffer() {
		return buffer(false, false);
	}

	@Override
	public void commentImpl(String comment) {
		stack.peek().comment(comment);
	}
}

class BufferedStructuredDataSourceImpl extends StructuredDataSourceImpl
		implements BufferedStructuredDataSource {
	private final int startDepth;
	private final BufferedStructuredData root;
	private final Deque<BufferedStructuredData> stack;

	public BufferedStructuredDataSourceImpl(BufferedStructuredData root,
			int startDepth) {
		this(root, new ArrayDeque<>(Arrays.asList(root)), startDepth);
	}

	public BufferedStructuredDataSourceImpl(BufferedStructuredData root,
			Deque<BufferedStructuredData> stack, int startDepth) {
		this.root = root;
		this.stack = stack;
		this.startDepth = startDepth;

		reset();
	}

	@Override
	public Namespace getDefaultNamespaceHintImpl() {
		return stack.peek().defaultNamespaceHint();
	}

	@Override
	public Set<Namespace> getNamespaceHintsImpl() {
		return stack.peek().namespaceHints();
	}

	@Override
	public List<String> getCommentsImpl() {
		return stack.peek().comments();
	}

	@Override
	public DataSource readPropertyImpl(QualifiedName name) {
		return stack.peek().propertyData(name);
	}

	@Override
	public Set<QualifiedName> getProperties() {
		return stack.peek().properties();
	}

	@Override
	public QualifiedName startNextChildImpl() {
		BufferedStructuredData child = stack.peek().nextChild();

		if (child == null)
			return null;

		stack.push(child);
		return stack.peek().name();
	}

	@Override
	public QualifiedName peekNextChild() {
		return stack.peek() == null ? null : stack.peek().peekNextChild();
	}

	@Override
	public boolean hasNextChild() {
		return stack.peek().hasNextChild();
	}

	@Override
	public void endChildImpl() {
		stack.pop();
	}

	@Override
	public DataSource readContentImpl() {
		DataSource content = stack.peek().content();
		return content == null ? null : content;
	}

	@Override
	public void reset() {
		stack.clear();
		stack.push(root);
		root.reset();

		int depth = startDepth;
		while (depth > 0)
			startNextChild();
	}

	@Override
	public BufferedStructuredDataSourceImpl split() {
		BufferedStructuredDataSourceImpl copy = new BufferedStructuredDataSourceImpl(
				root, stack, startDepth);
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

		thatCopy = thatCopy.split();
		thatCopy.reset();

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
}

class BufferingStructuredData {
	private final QualifiedName name;

	private Namespace defaultNamespaceHint;
	private final Set<Namespace> namespaceHints;

	private final Map<QualifiedName, BufferingDataTarget> properties;
	private BufferingDataTarget content;

	private final List<BufferingStructuredData> children;

	private final List<String> comments;

	private final Set<Consumer<BufferedStructuredData>> childListeners;

	public BufferingStructuredData(QualifiedName name) {
		namespaceHints = new HashSet<>();
		comments = new ArrayList<>();

		children = new ArrayList<>();
		properties = new LinkedHashMap<>();
		this.name = name;
		childListeners = new HashSet<>();
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

	public BufferedStructuredData buffer(boolean linked, boolean consumable) {
		return new BufferedStructuredData(linked, consumable);
	}

	public void addChildListener(Consumer<BufferedStructuredData> childConsumer) {
		childListeners.add(childConsumer);
	}

	public void endChild() {}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BufferingStructuredData))
			return false;
		BufferingStructuredData that = (BufferingStructuredData) obj;

		return super.equals(obj)
				&& Objects.equals(defaultNamespaceHint, that.defaultNamespaceHint)
				&& Objects.equals(namespaceHints, that.namespaceHints)
				&& Objects.equals(name, that.name)
				&& Objects.equals(properties, that.properties)
				&& Objects.equals(content, that.content)
				&& Objects.equals(children, that.children);
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
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

	public class BufferedStructuredData {
		private int childIndex;

		public BufferedStructuredData(boolean linked, boolean consumable) {
			childIndex = 0;
		}

		public Set<Namespace> namespaceHints() {
			return namespaceHints;
		}

		public Namespace defaultNamespaceHint() {
			return defaultNamespaceHint;
		}

		public List<String> comments() {
			return comments;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof BufferedStructuredData))
				return false;
			BufferedStructuredData that = (BufferedStructuredData) obj;

			return super.equals(obj) && childIndex == that.childIndex
					&& Objects.equals(base(), that.base());
		}

		private Object base() {
			return BufferingStructuredData.this;
		}

		@Override
		public int hashCode() {
			return childIndex += base().hashCode();
		}

		public List<BufferedStructuredData> children() {
			return children.stream()
					.map(c -> c.new BufferedStructuredData(false, false))
					.collect(Collectors.toList());
		}

		public BufferedStructuredData nextChild() {
			if (childIndex == children.size())
				return null;
			return children.get(childIndex++).new BufferedStructuredData(false, false);
		}

		public QualifiedName peekNextChild() {
			if (childIndex == children.size())
				return null;
			return children.get(childIndex).name;
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
			return Optional.ofNullable(properties.get(name)).map(p -> p.buffer())
					.orElse(null);
		}

		public DataSource content() {
			return Optional.ofNullable(content).map(p -> p.buffer()).orElse(null);
		}

		public int childIndex() {
			return childIndex;
		}

		public void reset() {
			childIndex = 0;
		}
	}
}
