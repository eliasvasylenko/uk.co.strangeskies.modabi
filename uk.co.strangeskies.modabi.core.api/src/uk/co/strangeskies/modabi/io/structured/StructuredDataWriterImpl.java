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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.ModabiIOException;

public abstract class StructuredDataWriterImpl<S extends StructuredDataWriterImpl<S>>
    implements StructuredDataWriter {
  private PositionStackItem position;

  public StructuredDataWriterImpl() {}

  @Override
  public QualifiedName getName() {
    return position.getName();
  }

  @Override
  public StructuredDataPosition getPosition() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StructuredDataWriter addChild(QualifiedName name) {
    writePropertiesIfNeeded(null);

    position = position.addChild(name);

    return this;
  }

  protected abstract void addChildImpl(QualifiedName name);

  @Override
  public StructuredDataWriter endChild() {
    writePropertiesIfNeeded(position.getPrimaryProperty());

    position = position.getParent();

    return this;
  }

  @Override
  public Stream<QualifiedName> getProperties() {
    return position.getProperties();
  }

  @Override
  public Optional<String> readProperty(QualifiedName name) {
    return Optional.ofNullable(position.getProperty(name));
  }

  @Override
  public Optional<String> readPrimaryProperty() {
    return readProperty(position.getPrimaryProperty());
  }

  @Override
  public StructuredDataWriter writeProperty(QualifiedName name, String value) {
    position.writeProperty(name, value);
    return this;
  }

  @Override
  public StructuredDataWriter setPrimaryProperty(QualifiedName name) {
    position.setPrimaryProperty(name);
    return this;
  }

  private void writePropertiesIfNeeded(QualifiedName primaryProperty) {
    if (!position.hasChildren()) {
      writePropertiesImpl(position.getPropertyMap(), primaryProperty);
    }
  }

  protected abstract void writePropertiesImpl(
      Map<QualifiedName, String> properties,
      QualifiedName primaryProperty);

  @Override
  public StructuredDataReader split() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NavigableStructuredDataReader buffer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<String> getComments() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StructuredDataWriter comment(String comment) {
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
  public StructuredDataWriter registerDefaultNamespaceHint(Namespace namespace) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StructuredDataWriter registerNamespaceHint(Namespace namespace) {
    // TODO Auto-generated method stub
    return null;
  }
}

class PositionStackItem {
  private final PositionStackItem parent;

  private final QualifiedName name;
  private final Map<QualifiedName, String> properties;
  private QualifiedName primaryProperty;
  private final int index;
  private int childIndex;

  public PositionStackItem(PositionStackItem parent, QualifiedName name, int index) {
    this.parent = parent;
    this.name = name;
    this.index = index;
    this.childIndex = 0;
    this.properties = new HashMap<>();
  }

  public String getProperty(QualifiedName name) {
    return properties.get(name);
  }

  public Stream<QualifiedName> getProperties() {
    return properties.keySet().stream();
  }

  public void setPrimaryProperty(QualifiedName primaryProperty) {
    if (hasChildren())
      throw new ModabiIOException(MESSAGES.cannotModifyPropertiesAfterChildren());
    this.primaryProperty = primaryProperty;
  }

  public QualifiedName getPrimaryProperty() {
    return primaryProperty;
  }

  public Map<QualifiedName, String> getPropertyMap() {
    return Collections.unmodifiableMap(properties);
  }

  public void writeProperty(QualifiedName name, String value) {
    if (hasChildren())
      throw new ModabiIOException(MESSAGES.cannotModifyPropertiesAfterChildren());
    properties.put(name, value);
  }

  public boolean hasChildren() {
    return childIndex > 0;
  }

  public PositionStackItem getParent() {
    return parent;
  }

  public QualifiedName getName() {
    return name;
  }

  public PositionStackItem addChild(QualifiedName name) {
    return new PositionStackItem(this, name, childIndex++);
  }
}
