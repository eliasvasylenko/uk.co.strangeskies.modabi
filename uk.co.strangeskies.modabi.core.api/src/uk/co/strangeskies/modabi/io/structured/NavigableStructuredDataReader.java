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

public interface NavigableStructuredDataReader extends StructuredDataReader {
  NavigableStructuredDataReader reset();

  @Override
  NavigableStructuredDataReader readNextChild();

  @Override
  default NavigableStructuredDataReader skipNextChild() {
    return (NavigableStructuredDataReader) StructuredDataReader.super.skipNextChild();
  }

  @Override
  default NavigableStructuredDataReader skipChildren() {
    return (NavigableStructuredDataReader) StructuredDataReader.super.skipChildren();
  }

  /**
   * throws an exception if there are more children, so call skipChildren() first,
   * or call endChildEarly, if you want to ignore them.
   */
  @Override
  NavigableStructuredDataReader endChild();

  @Override
  NavigableStructuredDataReader split();
}
