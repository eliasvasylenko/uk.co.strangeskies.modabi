/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.data.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataType;
import uk.co.strangeskies.modabi.io.structured.BufferedStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget;

@RunWith(Theories.class)
public class StructuredDataTests {
	@DataPoints
	@Test
	public List<BufferedStructuredDataSource> createBufferedSourceTest() {
		List<BufferedStructuredDataSource> sources = new ArrayList<>();

		sources.add(BufferingStructuredDataTarget.singleBuffer()
				.nextChild(new QualifiedName("one")).endChild().getBuffer());

		sources.add(BufferingStructuredDataTarget
				.singleBuffer()
				.nextChild(new QualifiedName("one"))
				.writeProperty(new QualifiedName("two"),
						o -> o.put(DataType.STRING, "twoValue")).endChild().getBuffer());

		return sources;
	}

	@Theory
	public void equalityTest(BufferedStructuredDataSource bufferedData) {
		Assert.assertEquals(bufferedData, bufferedData.split());
	}

	@Theory
	public void pipeNextChildTest(BufferedStructuredDataSource bufferedData) {
		BufferedStructuredDataSource pipedBufferedData = bufferedData
				.pipeNextChild(BufferingStructuredDataTarget.singleBuffer())
				.getBuffer();

		bufferedData.reset();
		pipedBufferedData.reset();

		Assert.assertEquals(bufferedData, pipedBufferedData);
	}
}
