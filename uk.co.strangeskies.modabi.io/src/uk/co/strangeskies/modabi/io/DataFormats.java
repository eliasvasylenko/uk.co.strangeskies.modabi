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

import static uk.co.strangeskies.modabi.io.ModabiIOException.MESSAGES;

import uk.co.strangeskies.modabi.NamedSet;

public class DataFormats extends NamedSet<String, DataFormat> {
  public DataFormats() {
    super(DataFormat::getFormatId);
  }

  @Override
  public void add(DataFormat element) {
    super.add(element);
  }

  public static String getExtension(String resourceName) {
    int lastDot = resourceName.lastIndexOf(".");

    if (lastDot == -1) {
      throw new ModabiIOException(MESSAGES.missingFileExtension(resourceName));
    }

    return resourceName.substring(0, lastDot);
  }

  public DataFormat getFormat(String extension) {
    return getAll()
        .filter(f -> f.getFileExtensions().anyMatch(extension::equals))
        .findFirst()
        .orElseThrow(() -> new ModabiIOException(MESSAGES.noFormatFound(extension)));
  }
}
