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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.NavigableStructuredDataReader;
import uk.co.strangeskies.modabi.io.StructuredDataBuffer;

public class StructuredDataTest {
  private static final QualifiedName A = name("A");
  private static final QualifiedName B = name("B");
  private static final QualifiedName C = name("C");

  private static QualifiedName name(String name) {
    return new QualifiedName(name);
  }

  @Test
  public void simpleBufferTest() {
    NavigableStructuredDataReader buffer = new StructuredDataBuffer()
        .addChild(A)
        .addChild(B)
        .addChild(C)
        .reset();

    assertEquals(A, buffer.readNextChild());
    assertEquals(B, buffer.readNextChild());
    assertEquals(C, buffer.readNextChild());
  }

  @Test
  public void hasNextChildTest() {
    NavigableStructuredDataReader buffer = new StructuredDataBuffer().addChild(A).reset();

    assertTrue(buffer.getNextChild().isPresent());
  }

  @Test
  public void hasNoNextChildTest() {
    NavigableStructuredDataReader buffer = new StructuredDataBuffer().addChild(A).reset();

    buffer.readNextChild();
    assertFalse(buffer.getNextChild().isPresent());
  }

  @Test(expected = IllegalStateException.class)
  public void endChildUnderflowTest() {
    NavigableStructuredDataReader buffer = new StructuredDataBuffer().addChild(A).reset();

    buffer.readNextChild();
    buffer.endChild();
  }

  @Test(expected = IllegalStateException.class)
  public void startNextChildUnderflowTest() {
    NavigableStructuredDataReader buffer = new StructuredDataBuffer().addChild(A).reset();

    buffer.readNextChild();
    buffer.readNextChild();
  }
}
