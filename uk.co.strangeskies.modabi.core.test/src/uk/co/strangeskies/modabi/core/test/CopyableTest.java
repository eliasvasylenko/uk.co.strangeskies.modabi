/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.test.
 *
 * uk.co.strangeskies.modabi.core.test is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.test is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.test.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.core.test;

import org.junit.Test;

import uk.co.strangeskies.utilities.Copyable;

public class CopyableTest {
	@Test
	public void schemaManagerServiceTest() {
		class CopyableImpl implements Copyable<CopyableImpl> {
			private final String name;

			public CopyableImpl(String name) {
				this.name = name;
			}

			@Override
			public CopyableImpl copy() {
				return new CopyableImpl(name);
			}

			@Override
			public String toString() {
				return name;
			}
		}

		System.out.println(new CopyableImpl("copyable").copy());
	}
}
