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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataType;
import uk.co.strangeskies.modabi.io.structured.BufferedStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget;

public class StructuredDataTests {
	@DataProvider(name = "bufferedData")
	public Object[][] createBufferedSources() {
		return new Object[][] {

				{ new BufferingStructuredDataTarget()
						.nextChild(new QualifiedName("one")).endChild().buffer() },

				{ new BufferingStructuredDataTarget()
						.nextChild(new QualifiedName("one"))
						.writeProperty(new QualifiedName("two"),
								o -> o.put(DataType.STRING, "twoValue")).endChild().buffer() } };
	}

	@Test
	public void bufferingTargetTest() {
		createBufferedSources();
	}

	@Test(dataProvider = "bufferedData", dependsOnMethods = { "bufferingTargetTest" })
	public void equalityTest(BufferedStructuredDataSource bufferedData) {
		Assert.assertEquals(bufferedData, bufferedData.split());
	}

	@Test(dataProvider = "bufferedData", dependsOnMethods = { "bufferingTargetTest" })
	public void pipeNextChildTest(BufferedStructuredDataSource bufferedData) {
		BufferedStructuredDataSource pipedBufferedData = bufferedData
				.pipeNextChild(new BufferingStructuredDataTarget()).buffer();

		bufferedData.reset();
		pipedBufferedData.reset();

		Assert.assertEquals(bufferedData, pipedBufferedData);
	}
}
