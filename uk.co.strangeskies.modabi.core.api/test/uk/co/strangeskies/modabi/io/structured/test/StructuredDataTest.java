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
package uk.co.strangeskies.modabi.io.structured.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.structured.NavigableStructuredDataReader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataBuffer;

public class StructuredDataTest {
  private static final QualifiedName A = name("A");
  private static final QualifiedName B = name("B");
  private static final QualifiedName C = name("C");

  private static QualifiedName name(String name) {
    return new QualifiedName(name);
  }

  @Test
  public void simpleBufferTest() {
    NavigableStructuredDataReader buffer = StructuredDataBuffer
        .singleBuffer()
        .addChild(A)
        .addChild(B)
        .addChild(C)
        .getBuffer();

    assertEquals(A, buffer.readNextChild());
    assertEquals(B, buffer.readNextChild());
    assertEquals(C, buffer.readNextChild());
  }

  @Test
  public void hasNextChildTest() {
    NavigableStructuredDataReader buffer = StructuredDataBuffer
        .singleBuffer()
        .addChild(A)
        .getBuffer();

    assertTrue(buffer.hasNextChild());
  }

  @Test
  public void hasNoNextChildTest() {
    NavigableStructuredDataReader buffer = StructuredDataBuffer
        .singleBuffer()
        .addChild(A)
        .getBuffer();

    buffer.readNextChild();
    assertFalse(buffer.hasNextChild());
  }

  @Test(expected = IllegalStateException.class)
  public void endChildUnderflowTest() {
    NavigableStructuredDataReader buffer = StructuredDataBuffer
        .singleBuffer()
        .addChild(A)
        .getBuffer();

    buffer.readNextChild();
    buffer.endChild();
  }

  @Test(expected = IllegalStateException.class)
  public void startNextChildUnderflowTest() {
    NavigableStructuredDataReader buffer = StructuredDataBuffer
        .singleBuffer()
        .addChild(A)
        .getBuffer();

    buffer.readNextChild();
    buffer.readNextChild();
  }
}
