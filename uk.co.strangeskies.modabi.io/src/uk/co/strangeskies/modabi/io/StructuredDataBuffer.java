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
package uk.co.strangeskies.modabi.io;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;

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
public class StructuredDataBuffer implements NavigableStructuredDataWriter {
  private final StructuredDataBufferPosition position;

  public StructuredDataBuffer() {
    position = new StructuredDataBufferPosition(emptyList(), emptyList());
  }

  @Override
  public StructuredDataPosition getPosition() {
    return position;
  }

  @Override
  public QualifiedName getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataReader split() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Namespace getDefaultNamespaceHint() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<Namespace> getNamespaceHints() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<String> getComments() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<QualifiedName> getProperties() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<String> readProperty(QualifiedName name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<String> readPrimaryProperty() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataReader buffer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataWriter reset() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataWriter readNextChild() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataWriter endChild() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataWriter registerDefaultNamespaceHint(Namespace namespace) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataWriter registerNamespaceHint(Namespace namespace) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataWriter addChild(QualifiedName name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataWriter writeProperty(QualifiedName name, String value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataWriter setPrimaryProperty(QualifiedName name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataWriter comment(String comment) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<QualifiedName> getNextChild() {
    // TODO Auto-generated method stub
    return null;
  }
}

class StructuredDataBufferPosition implements StructuredDataPosition {
  private final List<StructuredDataBufferNode> stackPosition;
  private List<StructuredDataBufferNode> previousChildren;

  public StructuredDataBufferPosition(
      List<StructuredDataBufferNode> positionStack,
      List<StructuredDataBufferNode> previousChildren) {
    this.stackPosition = positionStack;
  }

  public StructuredDataBufferNode getCurrentNode() {
    return stackPosition.get(getDepth() - 1);
  }

  private void refreshPreviousChildren() {
    if (previousChildren.size() > 0)
      for (int i = previousChildren.size(); i > 0; --i) {
        int previousChildIndex = previousChildren.get(i).getIndex();
        if (previousChildIndex >= 0) {
          previousChildren = getCurrentNode().getChildren().subList(0, previousChildIndex + 1);
        }
      }
  }

  @Override
  public int getDepth() {
    return stackPosition.size();
  }

  @Override
  public int getIndex(int depth) {
    if (depth == getDepth()) {
      refreshPreviousChildren();
      return previousChildren.size();
    } else {
      return stackPosition.get(depth).getIndex();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (!(obj instanceof StructuredDataPosition))
      return false;

    StructuredDataPosition that = (StructuredDataPosition) obj;

    if (this.getDepth() != that.getDepth())
      return false;

    for (int i = 0; i <= getDepth(); i++)
      if ((this.getIndex(i) != that.getIndex(i)))
        return false;

    return true;
  }

  @Override
  public int hashCode() {
    return stackPosition.hashCode();
  }
}

class StructuredDataBufferNode {
  private final QualifiedName name;
  private final StructuredDataBufferNode parent;

  private final Map<QualifiedName, String> properties;
  private String content;

  private final List<StructuredDataBufferNode> children;

  public StructuredDataBufferNode(QualifiedName name, StructuredDataBufferNode parent) {
    this.name = name;
    this.parent = parent;

    children = new ArrayList<>();
    properties = new HashMap<>();
  }

  public QualifiedName getName() {
    return name;
  }

  public StructuredDataBufferNode getParent() {
    return parent;
  }

  public int getIndex() {
    if (parent == null)
      return 0;
    return parent.children.indexOf(this);
  }

  public List<StructuredDataBufferNode> getChildren() {
    return children;
  }
}
