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

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
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
import java.util.TreeSet;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.IOException;
import uk.co.strangeskies.utilities.IdentityComparator;

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
public class BufferingStructuredDataTarget<S extends BufferingStructuredDataTarget<S>>
		extends StructuredDataTargetImpl<S> {
	public static class StructuredDataTargetBufferManager extends
			BufferingStructuredDataTarget<StructuredDataTargetBufferManager> {
		private StructuredDataTargetBufferManager(List<Integer> indexStack) {
			super(indexStack);
		}

		public BufferedStructuredDataSource openBuffer() {
			return openBuffer(false);
		}

		public StructuredDataSource openConsumableBuffer() {
			return openBuffer(true);
		}
	}

	public static class StructuredDataTargetBuffer extends
			BufferingStructuredDataTarget<StructuredDataTargetBuffer> {
		private final BufferedStructuredDataSource buffer;

		private StructuredDataTargetBuffer(List<Integer> indexStack) {
			super(indexStack);
			buffer = openBuffer(false);
		}

		public BufferedStructuredDataSource getBuffer() {
			return buffer;
		}
	}

	public static class ConsumableStructuredDataTargetBuffer extends
			BufferingStructuredDataTarget<ConsumableStructuredDataTargetBuffer> {
		private final StructuredDataSource buffer;

		private ConsumableStructuredDataTargetBuffer(List<Integer> indexStack) {
			super(indexStack);
			buffer = openBuffer(true);
		}

		public StructuredDataSource getBuffer() {
			return buffer;
		}
	}

	private final List<WeakReference<BufferedStructuredDataSourceImpl>> buffers;
	private final Deque<Integer> indexStack;

	private BufferingStructuredDataTarget(List<Integer> indexStack) {
		buffers = new ArrayList<>();
		this.indexStack = new ArrayDeque<>(indexStack);
	}

	public static StructuredDataTargetBuffer singleBuffer() {
		return singleBuffer(Collections.emptyList());
	}

	public static StructuredDataTargetBuffer singleBuffer(List<Integer> indexStack) {
		return new StructuredDataTargetBuffer(indexStack);
	}

	public static ConsumableStructuredDataTargetBuffer singleConsumableBuffer() {
		return singleConsumableBuffer(Collections.emptyList());
	}

	public static ConsumableStructuredDataTargetBuffer singleConsumableBuffer(
			List<Integer> indexStack) {
		return new ConsumableStructuredDataTargetBuffer(indexStack);
	}

	public static StructuredDataTargetBufferManager multipleBuffers() {
		return multipleBuffers(Collections.emptyList());
	}

	public static StructuredDataTargetBufferManager multipleBuffers(
			List<Integer> indexStack) {
		return new StructuredDataTargetBufferManager(indexStack);
	}

	private void forEachHead(Consumer<StructuredDataBuffer> perform) {
		if (buffers.isEmpty())
			return;
		else if (buffers.size() == 1) {
			BufferedStructuredDataSourceImpl buffer = buffers.iterator().next().get();
			if (buffer != null) {
				perform.accept(buffer.component().peekHead());
			} else {
				buffers.clear();
			}
		} else {
			Set<StructuredDataBuffer> bufferHeads = new TreeSet<>(
					new IdentityComparator<>());
			forEachBuffer(buffer -> {
				StructuredDataBuffer bufferHead = buffer.component().peekHead();
				if (bufferHeads.add(bufferHead)) {
					perform.accept(bufferHead);
				}
			});
		}
	}

	private void forEachBuffer(Consumer<BufferedStructuredDataSourceImpl> perform) {
		if (buffers.isEmpty())
			return;
		else {
			Iterator<WeakReference<BufferedStructuredDataSourceImpl>> bufferIterator = buffers
					.iterator();

			while (bufferIterator.hasNext()) {
				BufferedStructuredDataSourceImpl buffer = bufferIterator.next().get();
				if (buffer != null) {
					perform.accept(buffer);
				} else {
					bufferIterator.remove();
				}
			}
		}
	}

	@Override
	public void registerDefaultNamespaceHintImpl(Namespace namespace) {
		forEachHead(h -> h.setDefaultNamespaceHint(namespace));
	}

	@Override
	public void registerNamespaceHintImpl(Namespace namespace) {
		forEachHead(h -> h.addNamespaceHint(namespace));
	}

	@Override
	public DataTarget writePropertyImpl(QualifiedName name) {
		BufferingDataTarget target = new BufferingDataTarget();
		forEachHead(h -> h.addProperty(name, target));
		return target;
	}

	@Override
	public DataTarget writeContentImpl() {
		BufferingDataTarget target = new BufferingDataTarget();
		forEachHead(h -> h.addContent(target));
		return target;
	}

	@Override
	public void nextChildImpl(QualifiedName name) {
		StructuredDataBuffer child = new StructuredDataBuffer(name);
		forEachBuffer(b -> b.component().pushHead(child));

		indexStack.push(0);
	}

	@Override
	public void endChildImpl() {
		forEachBuffer(b -> b.component().popHead());

		indexStack.pop();
		if (!indexStack.isEmpty())
			indexStack.push(indexStack.pop() + 1);
	}

	protected BufferedStructuredDataSource openBuffer(boolean consumable) {
		return new BufferedStructuredDataSourceImpl(consumable);
	}

	@Override
	public void commentImpl(String comment) {
		forEachHead(h -> h.comment(comment));
	}

	class BufferedStructuredDataSourceImpl extends StructuredDataSourceWrapper
			implements BufferedStructuredDataSource {
		private PartialBufferedStructuredDataSource component;

		public BufferedStructuredDataSourceImpl(boolean consumable) {
			this(new PartialBufferedStructuredDataSource(consumable, indexStack));
		}

		private BufferedStructuredDataSourceImpl(
				PartialBufferedStructuredDataSource partial) {
			super(partial);
			component = partial;

			buffers.add(new WeakReference<>(this));
		}

		@Override
		public BufferedStructuredDataSourceImpl split() {
			return new BufferedStructuredDataSourceImpl(component.getSplit());
		}

		@Override
		public BufferedStructuredDataSource buffer() {
			return new BufferedStructuredDataSourceImpl(component.getBuffer());
		}

		@Override
		public BufferedStructuredDataSource copy() {
			return new BufferedStructuredDataSourceImpl(component.getCopy());
		}

		@Override
		public void reset() {
			component.reset();
		}

		public PartialBufferedStructuredDataSource component() {
			return component;
		}
	}

	private static List<StructuredDataBuffer> initialBuffer(int size) {
		List<StructuredDataBuffer> stack = new ArrayList<>(size);

		StructuredDataBuffer base = new StructuredDataBuffer((QualifiedName) null);
		stack.add(base);

		while (--size > 0) {
			StructuredDataBuffer child = new StructuredDataBuffer(
					(QualifiedName) null);
			stack.add(child);

			base.addChild(child);
			base = child;
		}

		return stack;
	}

	class PartialBufferedStructuredDataSource implements StructuredDataSource {
		/*
		 * Where new structured data is added to the front of the buffer:
		 */
		private final List<StructuredDataBuffer> headStack;
		/*
		 * Where structured data is read from the back of the buffer:
		 */
		private final List<StructuredDataBuffer> tailStack;

		private final List<Integer> startIndex;
		private final List<Integer> index;
		private final boolean consumable;

		public PartialBufferedStructuredDataSource(boolean consumable,
				Collection<Integer> index) {
			this(initialBuffer(index.size()), index, consumable);
		}

		public PartialBufferedStructuredDataSource(
				Collection<StructuredDataBuffer> stack, Collection<Integer> startIndex,
				boolean consumable) {
			this(new ArrayList<>(stack), new ArrayList<>(stack), new ArrayList<>(
					startIndex), null, consumable, false);
		}

		private PartialBufferedStructuredDataSource(
				List<StructuredDataBuffer> headStack,
				List<StructuredDataBuffer> tailStack, List<Integer> index,
				List<Integer> startIndex, boolean consumable,
				boolean consumeOnConstruction) {
			if (startIndex == null)
				startIndex = new ArrayList<>(index);

			this.headStack = headStack;
			this.tailStack = tailStack;
			this.index = index;
			this.startIndex = startIndex;
			this.consumable = consumable;

			if (consumeOnConstruction)
				consumeToIndex(startIndex);

			if (consumable)
				startIndex = index;
		}

		public void reset() {
			if (!consumable) {
				StructuredDataBuffer root = root();

				tailStack.clear();
				tailStack.add(root);
				index.clear();
				index.addAll(startIndex);

				int depth = startIndex.size();
				while (depth-- > 0)
					tailStack.add(peekTail().getChild(0, false));
			}
		}

		private void consumeToIndex(List<Integer> startIndex) {
			Deque<StructuredDataBuffer> tailStack = new ArrayDeque<>(
					this.tailStack.size() + 8);

			Iterator<Integer> indexIterator = index.iterator();
			Iterator<StructuredDataBuffer> tailIterator = this.tailStack.iterator();
			StructuredDataBuffer previousChild = new StructuredDataBuffer(
					tailIterator.next());

			boolean previouslyRemoved = false;

			int depth = 0;
			while (tailIterator.hasNext()) {
				StructuredDataBuffer child = new StructuredDataBuffer(
						tailIterator.next());
				tailStack.push(child);

				int remove = indexIterator.next();
				if (!previouslyRemoved && startIndex.size() > depth)
					remove -= startIndex.get(depth);
				if (remove > 0)
					previouslyRemoved = true;

				previousChild.children.set(0, child);

				while (remove-- > 0)
					child.children.remove(0);

				previousChild = child;
				depth++;
			}
		}

		public PartialBufferedStructuredDataSource getSplit() {
			return new PartialBufferedStructuredDataSource(headStack, tailStack,
					index, startIndex, true, true);
		}

		public PartialBufferedStructuredDataSource getBuffer() {
			return new PartialBufferedStructuredDataSource(headStack, tailStack,
					index, startIndex, false, true);
		}

		public PartialBufferedStructuredDataSource getCopy() {
			return new PartialBufferedStructuredDataSource(headStack, tailStack,
					index, startIndex, false, false);
		}

		public StructuredDataBuffer peekHead() {
			return headStack.get(headStack.size() - 1);
		}

		public StructuredDataBuffer peekTail() {
			return tailStack.get(tailStack.size() - 1);
		}

		public int peekIndex() {
			return index.get(index.size() - 1);
		}

		public void pushHead(StructuredDataBuffer child) {
			headStack.add(child);
		}

		public void popHead() {
			StructuredDataBuffer element = headStack.remove(headStack.size() - 1);
			element.endChild();

			if (headStack.isEmpty()) {
				throw new IllegalStateException();
			}

			peekHead().addChild(element);
		}

		@Override
		public Namespace getDefaultNamespaceHint() {
			return peekTail().defaultNamespaceHint();
		}

		@Override
		public Set<Namespace> getNamespaceHints() {
			return peekTail().namespaceHints();
		}

		@Override
		public List<String> getComments() {
			return peekTail().comments();
		}

		@Override
		public DataSource readProperty(QualifiedName name) {
			return peekTail().propertyData(name);
		}

		@Override
		public Set<QualifiedName> getProperties() {
			return peekTail().properties();
		}

		@Override
		public QualifiedName startNextChild() {
			StructuredDataBuffer child = peekTail().getChild(getActualTailIndex(),
					consumable);

			if (child == null)
				return null;

			tailStack.add(child);
			index.add(0);

			return child.name();
		}

		private int getActualTailIndex() {
			if (consumable || index.size() == 0)
				return 0;

			if (startIndex.size() < index.size())
				return peekIndex();

			Iterator<Integer> startIndexIterator = startIndex.iterator();
			Iterator<Integer> indexIterator = index.iterator();
			for (int i = 1; i < index.size(); i++) {
				if (startIndexIterator.next() != indexIterator.next())
					return peekIndex();
			}

			return indexIterator.next() + startIndexIterator.next();
		}

		@Override
		public QualifiedName peekNextChild() {
			StructuredDataBuffer buffer = peekTail();

			if (buffer != null) {
				buffer = peekTail().getChild(peekIndex(), false);

				if (buffer != null)
					return buffer.name();
			}

			return null;
		}

		@Override
		public boolean hasNextChild() {
			return peekTail().hasChild(peekIndex());
		}

		@Override
		public void endChild() {
			if (!peekTail().isEnded())
				throw new IllegalStateException();

			tailStack.remove(tailStack.size() - 1);
			index.remove(index.size() - 1);
			if (!index.isEmpty())
				index.set(index.size() - 1, peekIndex() + 1);

			if (consumable) {
				peekTail().children.remove(0);
			}
		}

		@Override
		public DataSource readContent() {
			DataSource content = peekTail().content();
			return content == null ? null : content;
		}

		private StructuredDataBuffer root() {
			return tailStack.get(0);
		}

		@Override
		public boolean equals(Object that) {
			if (!(that instanceof BufferedStructuredDataSource))
				return false;

			BufferedStructuredDataSource thatCopy = (BufferedStructuredDataSource) that;

			if (!index().equals(thatCopy.index()))
				return false;

			thatCopy = thatCopy.copy();
			thatCopy.reset();

			return root()
					.equals(
							((BufferingStructuredDataTarget<?>.BufferedStructuredDataSourceImpl) thatCopy
									.pipeNextChild(BufferingStructuredDataTarget.singleBuffer())
									.getBuffer()).component().root());
		}

		@Override
		public int hashCode() {
			return root().hashCode() + index().hashCode();
		}

		@Override
		public List<Integer> index() {
			return new ArrayList<>(index);
		}

		@Override
		public StructuredDataState currentState() {
			throw new AssertionError();
		}

		@Override
		public StructuredDataSource split() {
			throw new AssertionError();
		}

		@Override
		public BufferedStructuredDataSource buffer() {
			throw new AssertionError();
		}
	}

	static class StructuredDataBuffer {
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

			children = new ArrayList<>(from.children);

			namespaceHints = from.namespaceHints;
			comments = from.comments;
			ended = from.ended;
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
			if (!(obj instanceof BufferingStructuredDataTarget.StructuredDataBuffer))
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

		public StructuredDataBuffer getChild(int index, boolean consumable) {
			if (index >= children.size())
				return null;

			StructuredDataBuffer child = children.get(index);

			if (consumable && child.hasChild(0)) {
				child = new StructuredDataBuffer(child);
				children.set(index, child);
			}

			index++;

			return child;
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
}
