package uk.co.strangeskies.modabi.data.test;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import uk.co.strangeskies.modabi.io.DataType;
import uk.co.strangeskies.modabi.io.structured.BufferedStructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

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
	public void pipeNextChildTest(BufferedStructuredDataSource bufferedData) {
		BufferedStructuredDataSource pipedBufferedData = bufferedData
				.pipeNextChild(new BufferingStructuredDataTarget()).buffer();

		Assert.assertEquals(bufferedData, pipedBufferedData);
	}
}
