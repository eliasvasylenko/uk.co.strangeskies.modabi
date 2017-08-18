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
package uk.co.strangeskies.modabi.io.structured;

import static uk.co.strangeskies.modabi.io.ModabiIOException.MESSAGES;

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
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import uk.co.strangeskies.collection.EquivalenceComparator;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.ModabiIOException;

/**
 * It shouldn't matter in what order attributes are added to a child, or whether
 * they are added before, after, or between other children. Because of this,
 * {@link StructuredDataBuffer} does not produce a
 * {@link NavigableStructuredDataReader} which tries to match input order.
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
public class StructuredDataBuffer extends BufferingStructuredDataTarget {
  public static class Navigable extends BufferingStructuredDataTarget {
    private final NavigableStructuredDataReader buffer;

    private Navigable(List<Integer> indexStack) {
      super(indexStack);
      buffer = openNavigableBuffer();
    }

    public NavigableStructuredDataReader getBuffer() {
      return buffer;
    }
  }

  public static class Consumable extends BufferingStructuredDataTarget<Consumable> {
    private final StructuredDataReader buffer;

    private Consumable(List<Integer> indexStack) {
      super(indexStack);
      buffer = openBuffer();
    }

    public StructuredDataReader getBuffer() {
      return buffer;
    }
  }

  private StructuredDataBuffer(List<Integer> indexStack) {
    super(indexStack);
  }

  public static Navigable singleBuffer() {
    return singleBuffer(Collections.emptyList());
  }

  public static Navigable singleBuffer(List<Integer> indexStack) {
    return new Navigable(indexStack);
  }

  public static Consumable singleConsumableBuffer() {
    return singleConsumableBuffer(Collections.emptyList());
  }

  public static Consumable singleConsumableBuffer(List<Integer> indexStack) {
    return new Consumable(indexStack);
  }

  public static StructuredDataBuffer multipleBuffers() {
    return multipleBuffers(Collections.emptyList());
  }

  public static StructuredDataBuffer multipleBuffers(List<Integer> indexStack) {
    return new StructuredDataBuffer(indexStack);
  }
}

class BufferingStructuredDataTarget implements StructuredDataWriter {
  private final List<WeakReference<BufferedStructuredDataSourceImpl>> buffers;
  private final Deque<Integer> indexStack;

  protected BufferingStructuredDataTarget(List<Integer> indexStack) {
    buffers = new ArrayList<>();
    this.indexStack = new ArrayDeque<>(indexStack);
  }

  private void forEachHead(Consumer<StructuredData> perform) {
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
      Set<StructuredData> bufferHeads = new TreeSet<>(
          new EquivalenceComparator<>((a, b) -> a == b));
      forEachBuffer(buffer -> {
        StructuredData bufferHead = buffer.component().peekHead();
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
      Iterator<WeakReference<BufferedStructuredDataSourceImpl>> bufferIterator = buffers.iterator();

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
  public StructuredDataWriter registerDefaultNamespaceHint(Namespace namespace) {
    forEachHead(h -> h.setDefaultNamespaceHint(namespace));
    return this;
  }

  @Override
  public StructuredDataWriter registerNamespaceHint(Namespace namespace) {
    forEachHead(h -> h.addNamespaceHint(namespace));
    return this;
  }

  @Override
  public StructuredDataWriter writeProperty(QualifiedName name, String data) {
    forEachHead(h -> h.addProperty(name, data));
    return this;
  }

  @Override
  public StructuredDataWriter writeContent(String data) {
    forEachHead(h -> h.addContent(data));
    return this;
  }

  @Override
  public StructuredDataWriter addChild(QualifiedName name) {
    StructuredData child = new StructuredData(name);
    forEachBuffer(b -> b.component().pushHead(child));

    indexStack.push(0);

    return this;
  }

  @Override
  public StructuredDataWriter endChild() {
    forEachBuffer(b -> b.component().popHead());

    indexStack.pop();
    if (!indexStack.isEmpty())
      indexStack.push(indexStack.pop() + 1);

    return this;
  }

  public NavigableStructuredDataReader openNavigableBuffer() {
    return openBuffer(false);
  }

  public StructuredDataReader openBuffer() {
    return openBuffer(true);
  }

  protected NavigableStructuredDataReader openBuffer(boolean consumable) {
    return new BufferedStructuredDataSourceImpl(consumable);
  }

  @Override
  public StructuredDataWriter comment(String comment) {
    forEachHead(h -> h.comment(comment));
    return this;
  }

  class BufferedStructuredDataSourceImpl extends StructuredDataReaderWrapper
      implements NavigableStructuredDataReader {
    private PartialBufferedStructuredDataSource component;

    public BufferedStructuredDataSourceImpl(boolean consumable) {
      this(new PartialBufferedStructuredDataSource(consumable, indexStack));
    }

    private BufferedStructuredDataSourceImpl(PartialBufferedStructuredDataSource partial) {
      super(partial);
      component = partial;

      buffers.add(new WeakReference<>(this));
    }

    @Override
    public BufferedStructuredDataSourceImpl split() {
      System.out.println(" INPUT INDEX: " + index());
      return new BufferedStructuredDataSourceImpl(component.getSplit());
    }

    @Override
    public NavigableStructuredDataReader buffer() {
      return new BufferedStructuredDataSourceImpl(component.getBuffer());
    }

    @Override
    public NavigableStructuredDataReader copy() {
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

  private static List<StructuredData> initialBuffer(int size) {
    List<StructuredData> stack = new ArrayList<>(size);

    StructuredData base = new StructuredData((QualifiedName) null);
    stack.add(base);

    while (--size > 0) {
      StructuredData child = new StructuredData((QualifiedName) null);
      stack.add(child);

      base.addChild(child);
      base = child;
    }

    return stack;
  }

  class PartialBufferedStructuredDataSource implements StructuredDataReader {
    /*
     * Where new structured data is added to the front of the buffer:
     */
    private final List<StructuredData> headStack;
    /*
     * Where structured data is read from the back of the buffer:
     */
    private final List<StructuredData> tailStack;

    private final List<Integer> startIndex;
    private final List<Integer> index;
    private final boolean consumable;

    public PartialBufferedStructuredDataSource(boolean consumable, Collection<Integer> index) {
      this(initialBuffer(index.size()), index, consumable);
    }

    public PartialBufferedStructuredDataSource(
        Collection<StructuredData> stack,
        Collection<Integer> startIndex,
        boolean consumable) {
      this(
          new ArrayList<>(stack),
          new ArrayList<>(stack),
          new ArrayList<>(startIndex),
          null,
          consumable,
          false);
    }

    private PartialBufferedStructuredDataSource(
        List<StructuredData> headStack,
        List<StructuredData> tailStack,
        List<Integer> index,
        List<Integer> startIndex,
        boolean consumable,
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
        StructuredData root = root();

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
      Deque<StructuredData> tailStack = new ArrayDeque<>(this.tailStack.size() + 8);

      Iterator<Integer> indexIterator = index.iterator();
      Iterator<StructuredData> tailIterator = this.tailStack.iterator();
      StructuredData previousChild = new StructuredData(tailIterator.next());

      boolean previouslyRemoved = false;

      System.out.println();
      System.out.println(startIndex);
      System.out.println(index);
      System.out.println(this.tailStack.size());

      int depth = 0;
      System.out.println(" c: " + previousChild.name + " -> " + previousChild.children.size());
      while (tailIterator.hasNext()) {
        StructuredData child = new StructuredData(tailIterator.next());
        System.out.println(
            " c: " + child.name + " -> " + child.children.size() + " = "
                + child.children.stream().map(c -> c.name.toString()).collect(
                    Collectors.joining(", ")));

        System.out.println(
            " cc: " + child.children.get(0).name + " -> " + child.children.get(0).children.size()
                + " = "
                + child.children.get(0).children.stream().map(c -> c.name.toString()).collect(
                    Collectors.joining(", ")));

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
      return new PartialBufferedStructuredDataSource(
          headStack,
          tailStack,
          index,
          startIndex,
          true,
          true);
    }

    public PartialBufferedStructuredDataSource getBuffer() {
      return new PartialBufferedStructuredDataSource(
          headStack,
          tailStack,
          index,
          startIndex,
          false,
          true);
    }

    public PartialBufferedStructuredDataSource getCopy() {
      return new PartialBufferedStructuredDataSource(
          headStack,
          tailStack,
          index,
          startIndex,
          false,
          false);
    }

    public StructuredData peekHead() {
      return headStack.get(headStack.size() - 1);
    }

    public StructuredData peekTail() {
      return tailStack.get(tailStack.size() - 1);
    }

    public int peekIndex() {
      return index.get(index.size() - 1);
    }

    public void pushHead(StructuredData child) {
      peekHead().addChild(child);
      headStack.add(child);
    }

    public void popHead() {
      StructuredData element = headStack.remove(headStack.size() - 1);
      element.endChild();

      if (headStack.isEmpty()) {
        throw new IllegalStateException();
      }
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
    public String readProperty(QualifiedName name) {
      return peekTail().propertyData(name);
    }

    @Override
    public Set<QualifiedName> getProperties() {
      return peekTail().properties();
    }

    @Override
    public QualifiedName readNextChild() {
      StructuredData child = peekTail().getChild(getActualTailIndex(), consumable);

      if (child == null)
        throw new IllegalStateException("Next child does not exist");

      tailStack.add(child);
      index.add(0);

      return child.name();
    }

    @Override
    public StructuredDataReader endChild() {
      if (!peekTail().isEnded())
        throw new IllegalStateException("Root has already ended");

      tailStack.remove(tailStack.size() - 1);
      index.remove(index.size() - 1);
      if (!index.isEmpty())
        index.set(index.size() - 1, peekIndex() + 1);

      if (consumable) {
        peekTail().children.remove(0);
      }

      return this;
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
      StructuredData buffer = peekTail();

      if (buffer != null) {
        buffer = peekTail().getChild(peekIndex(), false);

        if (buffer != null)
          return buffer.name();
      }

      return null;
    }

    @Override
    public boolean hasNextChild() {
      return (index.isEmpty() && !root().children.isEmpty()) || peekTail().hasChild(peekIndex());
    }

    @Override
    public String readContent() {
      return peekTail().content();
    }

    private StructuredData root() {
      return tailStack.get(0);
    }

    @Override
    public boolean equals(Object that) {
      if (!(that instanceof NavigableStructuredDataReader))
        return false;

      NavigableStructuredDataReader thatCopy = (NavigableStructuredDataReader) that;

      if (!index().equals(thatCopy.index()))
        return false;

      thatCopy = thatCopy.copy();
      thatCopy.reset();

      return root().equals(
          ((BufferingStructuredDataTarget.BufferedStructuredDataSourceImpl) thatCopy
              .pipeNextChild(StructuredDataBuffer.singleBuffer())
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
    public StructuredDataReader split() {
      throw new AssertionError();
    }

    @Override
    public NavigableStructuredDataReader buffer() {
      throw new AssertionError();
    }
  }

  static class StructuredData {
    private final QualifiedName name;

    private final Map<QualifiedName, String> properties;
    private String content;

    private final List<StructuredData> children;
    private boolean ended;

    private Namespace defaultNamespaceHint;
    private final Set<Namespace> namespaceHints;

    private final List<String> comments;

    public StructuredData(QualifiedName name) {
      this.name = name;
      properties = new LinkedHashMap<>();

      children = new ArrayList<>();

      namespaceHints = new HashSet<>();
      comments = new ArrayList<>();
      ended = false;
    }

    public StructuredData(StructuredData from) {
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
        throw new ModabiIOException(MESSAGES.overlappingDefaultNamespaceHints());
      defaultNamespaceHint = namespace;
    }

    public void addProperty(QualifiedName name, String target) {
      if (ended)
        throw new IllegalStateException();

      properties.put(name, target);
    }

    public void addChild(StructuredData element) {
      if (ended)
        throw new IllegalStateException();

      children.add(element);
    }

    public void endChild() {
      ended = true;
    }

    public void addContent(String target) {
      if (ended)
        throw new IllegalStateException();

      content = target;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof StructuredDataBuffer.StructuredData))
        return false;
      StructuredData that = (StructuredData) obj;

      return ended == that.ended && Objects.equals(defaultNamespaceHint, that.defaultNamespaceHint)
          && Objects.equals(namespaceHints, that.namespaceHints) && Objects.equals(name, that.name)
          && Objects.equals(properties, that.properties) && Objects.equals(content, that.content)
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

    public List<StructuredData> children() {
      return children;
    }

    public StructuredData getChild(int index, boolean consumable) {
      if (index >= children.size())
        return null;

      StructuredData child = children.get(index);

      if (consumable && child.hasChild(0)) {
        child = new StructuredData(child);
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

    public String propertyData(QualifiedName name) {
      return properties.get(name);
    }

    public String content() {
      return content;
    }
  }

  @Override
  public List<Integer> index() {
    // TODO Auto-generated method stub
    return null;
  }
}
