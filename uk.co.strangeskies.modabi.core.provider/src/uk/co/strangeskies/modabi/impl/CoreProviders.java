/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;

public class CoreProviders {
  public Stream<Provider> getProviders() {
    return Stream.of(
        Provider.over(ProcessingContext.class, c -> c),
        Provider.over(new @Infer TypeToken<SortedSet<?>>() {}, () -> new TreeSet<>()),
        Provider.over(new @Infer TypeToken<Set<?>>() {}, () -> new HashSet<>()),
        Provider.over(new @Infer TypeToken<LinkedHashSet<?>>() {}, () -> new LinkedHashSet<>()),
        Provider.over(new @Infer TypeToken<List<?>>() {}, () -> new ArrayList<>()),
        Provider.over(new @Infer TypeToken<Map<?, ?>>() {}, () -> new HashMap<>()));
  }
}
