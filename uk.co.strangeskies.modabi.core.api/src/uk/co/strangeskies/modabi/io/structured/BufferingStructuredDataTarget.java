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
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
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
	private final BufferedStructuredDataSourceImpl buffer;

	public BufferingStructuredDataTarget() {
		this(false);
	}

	public BufferingStructuredDataTarget(boolean consumable) {
		buffer = new BufferedStructuredDataSourceImpl(consumable);
	}

	@Override
	public void registerDefaultNamespaceHintImpl(Namespace namespace) {
		buffer.peekHead().setDefaultNamespaceHint(namespace);
	}

	@Override
	public void registerNamespaceHintImpl(Namespace namespace) {
		buffer.peekHead().addNamespaceHint(namespace);
	}

	@Override
	public DataTarget writePropertyImpl(QualifiedName name) {
		BufferingDataTarget target = new BufferingDataTarget();
		buffer.peekHead().addProperty(name, target);
		return target;
	}

	@Override
	public DataTarget writeContentImpl() {
		BufferingDataTarget target = new BufferingDataTarget();
		buffer.peekHead().addContent(target);
		return target;
	}

	@Override
	public void nextChildImpl(QualifiedName name) {
		buffer.pushHead(new StructuredDataBuffer(name));
	}

	@Override
	public void endChildImpl() {
		buffer.popHead();
	}

	public BufferedStructuredDataSource getBuffer() {
		return buffer;
	}

	@Override
	public void commentImpl(String comment) {
		buffer.peekHead().comment(comment);
	}
}

class BufferedStructuredDataSourceImpl extends StructuredDataSourceImpl
		implements BufferedStructuredDataSource {
	private int startDepth;
	/*
	 * Where new structured data is added to the front of the buffer:
	 */
	private final Deque<StructuredDataBuffer> headStack;
	/*
	 * Where structured data is read from the back of the buffer:
	 */
	private final Deque<StructuredDataBuffer> tailStack;
	private final Deque<Integer> index;
	private final boolean consumable;

	public BufferedStructuredDataSourceImpl(boolean consumable) {
		this(Arrays.asList(new StructuredDataBuffer((QualifiedName) null)), Arrays
				.asList(0), consumable);
	}

	public BufferedStructuredDataSourceImpl(List<StructuredDataBuffer> stack,
			List<Integer> index, boolean consumable) {
		this(new ArrayDeque<>(stack), new ArrayDeque<>(stack), new ArrayDeque<>(
				index), consumable);
	}

	private BufferedStructuredDataSourceImpl(
			Deque<StructuredDataBuffer> headStack,
			Deque<StructuredDataBuffer> tailStack, Deque<Integer> index,
			boolean consumable) {
		this.headStack = headStack;
		this.tailStack = tailStack;
		this.index = index;
		this.startDepth = tailStack.size() - 1;
		this.consumable = consumable;

		reset();
	}

	public StructuredDataBuffer peekHead() {
		return headStack.peek();
	}

	public void pushHead(StructuredDataBuffer child) {
		headStack.push(child);
	}

	public void popHead() {
		StructuredDataBuffer element = headStack.pop();
		element.endChild();

		if (headStack.isEmpty()) {
			StructuredDataBuffer base = new StructuredDataBuffer((QualifiedName) null);
			headStack.addFirst(base);
			tailStack.addFirst(base);
			index.addFirst(0);
			startDepth++;
		}

		headStack.peek().addChild(element);
	}

	@Override
	public Namespace getDefaultNamespaceHintImpl() {
		return tailStack.peek().defaultNamespaceHint();
	}

	@Override
	public Set<Namespace> getNamespaceHintsImpl() {
		return tailStack.peek().namespaceHints();
	}

	@Override
	public List<String> getCommentsImpl() {
		return tailStack.peek().comments();
	}

	@Override
	public DataSource readPropertyImpl(QualifiedName name) {
		return tailStack.peek().propertyData(name);
	}

	@Override
	public Set<QualifiedName> getProperties() {
		return tailStack.peek().properties();
	}

	@Override
	public QualifiedName startNextChildImpl() {
		StructuredDataBuffer child = tailStack.peek().getChild(
				consumable ? 0 : index.peek());

		index.push(index.pop() + 1);

		if (child == null)
			return null;

		tailStack.push(child);
		index.push(0);

		return child.name();
	}

	@Override
	public QualifiedName peekNextChild() {
		StructuredDataBuffer buffer = tailStack.peek();

		if (buffer != null) {
			buffer = tailStack.peek().getChild(index.peek());

			if (buffer != null)
				return buffer.name();
		}

		return null;
	}

	@Override
	public boolean hasNextChild() {
		return tailStack.peek().hasChild(index.peek());
	}

	@Override
	public void endChildImpl() {
		if (!tailStack.peek().isEnded())
			throw new IllegalStateException();

		tailStack.pop();
		index.pop();

		if (consumable) {
			tailStack.push(tailStack.pop().consumeFirst());
		}
	}

	@Override
	public DataSource readContentImpl() {
		DataSource content = tailStack.peek().content();
		return content == null ? null : content;
	}

	@Override
	public void reset() {
		StructuredDataBuffer root = root();

		tailStack.clear();
		tailStack.push(root);
		index.clear();
		index.push(0);

		int depth = startDepth;
		while (depth > 0)
			startNextChild();
	}

	@Override
	public BufferedStructuredDataSourceImpl split() {
		BufferedStructuredDataSourceImpl copy = new BufferedStructuredDataSourceImpl(
				headStack, tailStack, index, true);
		throw new UnsupportedOperationException();
	}

	@Override
	public BufferedStructuredDataSource buffer() {
		throw new UnsupportedOperationException();
	}

	private StructuredDataBuffer root() {
		return tailStack.getLast();
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
						new BufferingStructuredDataTarget(false)).getBuffer()).root());
	}

	@Override
	public int hashCode() {
		return root().hashCode() + depth() + indexAtDepth();
	}

	@Override
	public int depth() {
		return tailStack.size();
	}

	@Override
	public int indexAtDepth() {
		return index.peek();
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

	public StructuredDataBuffer(QualifiedName name) {
		this.name = name;
		properties = new LinkedHashMap<>();

		children = new ArrayList<>();

		namespaceHints = new HashSet<>();
		comments = new ArrayList<>();
		ended = false;
	}

	public StructuredDataBuffer(StructuredDataBuffer from) {
		name = from.name;
		properties = from.properties;

		if (from.children.isEmpty())
			children = Collections.emptyList();
		else {
			Iterator<StructuredDataBuffer> childrenIterator = from.children
					.iterator();
			childrenIterator.next();
			children = new ArrayList<>(from.children.size() - 1);
			while (childrenIterator.hasNext()) {
				children.add(childrenIterator.next());
			}
		}

		namespaceHints = from.namespaceHints;
		comments = from.comments;
		ended = from.ended;
	}

	public StructuredDataBuffer consumeFirst() {
		return new StructuredDataBuffer(this);
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

	public void addProperty(QualifiedName name, BufferingDataTarget target) {
		if (ended)
			throw new IllegalStateException();

		properties.put(name, target);
	}

	public void addChild(StructuredDataBuffer element) {
		if (ended)
			throw new IllegalStateException();

		children.add(element);
	}

	public void endChild() {
		ended = true;
	}

	public void addContent(BufferingDataTarget target) {
		if (ended)
			throw new IllegalStateException();

		content = target;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StructuredDataBuffer))
			return false;
		StructuredDataBuffer that = (StructuredDataBuffer) obj;

		return ended == that.ended
				&& Objects.equals(defaultNamespaceHint, that.defaultNamespaceHint)
				&& Objects.equals(namespaceHints, that.namespaceHints)
				&& Objects.equals(name, that.name)
				&& Objects.equals(properties, that.properties)
				&& Objects.equals(content, that.content)
				&& Objects.equals(children, that.children);
	}

	@Override
	public int hashCode() {
		int hashCode = (ended ? 1 : 0);
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

	public StructuredDataBuffer getChild(int index) {
		if (index == children.size())
			return null;
		return children.get(index++);
	}

	public boolean hasChild(int index) {
		return index < children.size() && index >= 0;
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
}
