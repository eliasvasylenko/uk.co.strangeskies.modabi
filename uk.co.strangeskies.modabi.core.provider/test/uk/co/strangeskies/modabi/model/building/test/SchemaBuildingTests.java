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
package uk.co.strangeskies.modabi.model.building.test;

import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;

import java.util.Set;
import java.util.SortedSet;

import org.junit.Test;

import uk.co.strangeskies.modabi.impl.SchemaManagerService;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;

public class SchemaBuildingTests {
	@Test
	public void buildBaseSchemataTest() {
		new SchemaManagerService();
	}

	@Test
	public void fixMeUp() {
		System.out.println(new @Infer TypeToken<SortedSet<?>>() {}
				.withConstraintTo(LOOSE_COMPATIBILILTY, new TypeToken<Set<String>>() {})
				.resolve());
		System.out.println(new TypeToken<@Infer SortedSet<?>>() {}
				.withConstraintTo(LOOSE_COMPATIBILILTY, new TypeToken<Set<String>>() {})
				.resolve());
		System.out.println(new TypeToken<SortedSet<@Infer ?>>() {}
				.withConstraintTo(LOOSE_COMPATIBILILTY, new TypeToken<Set<String>>() {})
				.resolve());
	}
}
