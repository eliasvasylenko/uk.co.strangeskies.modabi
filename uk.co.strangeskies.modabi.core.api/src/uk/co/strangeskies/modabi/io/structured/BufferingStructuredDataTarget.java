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

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.IOException;

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
	private final Deque<StructuredDataBuffer> stack;
	private final BufferedStructuredDataSourceImpl buffer;

	public BufferingStructuredDataTarget() {
		StructuredDataBuffer root = new StructuredDataBuffer(null);
		stack = new ArrayDeque<>(Arrays.asList(root));
		buffer = new BufferedStructuredDataSourceImpl(stack);
	}

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
		stack.push(new StructuredDataBuffer(name));
	}

	@Override
	public void endChildImpl() {
		StructuredDataBuffer element = stack.pop();
		element.endChild();

		if (stack.isEmpty()) {
			StructuredDataBuffer base = new StructuredDataBuffer(null);
			stack.addFirst(base);
			buffer.addBase(base);
		}

		stack.peek().addChild(element);
	}

	public BufferedStructuredDataSource buffer() {
		return buffer;
	}

	@Override
	public void commentImpl(String comment) {
		stack.peek().comment(comment);
	}
}

class BufferedStructuredDataSourceImpl extends StructuredDataSourceImpl
		implements BufferedStructuredDataSource {
	private int startDepth;
	private final Deque<StructuredDataBuffer> stack;

	public BufferedStructuredDataSourceImpl(Deque<StructuredDataBuffer> stack) {
		this.stack = new ArrayDeque<>(stack);
		this.startDepth = stack.size() - 1;

		reset();
	}

	public void addBase(StructuredDataBuffer base) {
		stack.addFirst(base);
		startDepth++;
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
		StructuredDataBuffer child = stack.peek().nextChild();

		if (child == null)
			return null;

		stack.push(child);
		return child.name();
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
		if (!stack.peek().isEnded())
			throw new IllegalStateException();

		stack.pop();
	}

	@Override
	public DataSource readContentImpl() {
		DataSource content = stack.peek().content();
		return content == null ? null : content;
	}

	@Override
	public void reset() {
		StructuredDataBuffer root = root();

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
				stack);
		throw new UnsupportedOperationException();
	}

	@Override
	public BufferedStructuredDataSource buffer() {
		throw new UnsupportedOperationException();
	}

	private StructuredDataBuffer root() {
		return stack.getLast();
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

		return root().equals(
				((BufferedStructuredDataSourceImpl) thatCopy.pipeNextChild(
						new BufferingStructuredDataTarget()).buffer()).root());
	}

	@Override
	public int hashCode() {
		return root().hashCode() + depth() + indexAtDepth();
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

class StructuredDataBuffer {
	private final QualifiedName name;

	private final Map<QualifiedName, BufferingDataTarget> properties;
	private BufferingDataTarget content;

	private final List<StructuredDataBuffer> children;
	private boolean ended;

	private Namespace defaultNamespaceHint;
	private final Set<Namespace> namespaceHints;

	private final List<String> comments;

	private int childIndex;

	public StructuredDataBuffer(QualifiedName name) {
		namespaceHints = new HashSet<>();
		comments = new ArrayList<>();

		children = new ArrayList<>();

		properties = new LinkedHashMap<>();
		this.name = name;

		childIndex = 0;
		ended = false;
	}

	public boolean isEnded() {
		return ended;
	}

	public void comment(String comment) {
		if (ended)
			throw new IllegalStateException();

		comments.add(comment);
	}

	public void addNamespaceHint(Namespace namespace) {
		if (ended)
			throw new IllegalStateException();

		namespaceHints.add(namespace);
	}

	public void setDefaultNamespaceHint(Namespace namespace) {
		if (ended)
			throw new IllegalStateException();

		if (defaultNamespaceHint != null)
			throw new IOException(
					"Cannot register multiple default namespace hints at any given location.");
		defaultNamespaceHint = namespace;
	}

	public BufferingDataTarget addProperty(QualifiedName name) {
		if (ended)
			throw new IllegalStateException();

		BufferingDataTarget target = new BufferingDataTarget();
		properties.put(name, target);
		return target;
	}

	public void addChild(StructuredDataBuffer element) {
		if (ended)
			throw new IllegalStateException();

		children.add(element);
	}

	public void endChild() {
		ended = true;
	}

	public BufferingDataTarget addContent() {
		if (ended)
			throw new IllegalStateException();

		content = new BufferingDataTarget();
		return content;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StructuredDataBuffer))
			return false;
		StructuredDataBuffer that = (StructuredDataBuffer) obj;

		return childIndex == that.childIndex && ended == that.ended
				&& Objects.equals(defaultNamespaceHint, that.defaultNamespaceHint)
				&& Objects.equals(namespaceHints, that.namespaceHints)
				&& Objects.equals(name, that.name)
				&& Objects.equals(properties, that.properties)
				&& Objects.equals(content, that.content)
				&& Objects.equals(children, that.children);
	}

	@Override
	public int hashCode() {
		int hashCode = childIndex + (ended ? 1 : 0);
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

	public Set<Namespace> namespaceHints() {
		return namespaceHints;
	}

	public Namespace defaultNamespaceHint() {
		return defaultNamespaceHint;
	}

	public List<String> comments() {
		return comments;
	}

	public List<StructuredDataBuffer> children() {
		return children;
	}

	public StructuredDataBuffer nextChild() {
		if (childIndex == children.size())
			return null;
		return children.get(childIndex++);
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
		for (StructuredDataBuffer child : children)
			child.reset();
	}
}
