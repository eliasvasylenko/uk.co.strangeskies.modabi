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
package uk.co.strangeskies.modabi.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.reflection.Imports;

public class NamedSetTest {
	private class EmptySchema implements Schema {
		private final QualifiedName name;

		public EmptySchema(String name) {
			this.name = new QualifiedName(name);
		}

		@Override
		public QualifiedName qualifiedName() {
			return name;
		}

		@Override
		public Imports imports() {
			return Imports.empty();
		}

		@Override
		public Schemata dependencies() {
			return new Schemata();
		}

		@Override
		public Models models() {
			return new Models();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof EmptySchema))
				return false;

			return name.equals(((EmptySchema) obj).name);
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public String toString() {
			return name.toString();
		}
	}

	private Set<Schema> abcSet() {
		return new HashSet<>(Arrays.asList(new EmptySchema("A"), new EmptySchema("B"), new EmptySchema("C")));
	}

	@Test
	public void childScopeTest() {
		Schemata schemata = new Schemata();

		schemata.add(new EmptySchema("A"));
		schemata.add(new EmptySchema("B"));
		schemata.add(new EmptySchema("C"));

		Assert.assertEquals(abcSet(), new HashSet<>(schemata.nestChildScope()));
	}
}
