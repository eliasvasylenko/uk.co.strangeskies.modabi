package uk.co.strangeskies.modabi;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.model.Model;

public interface BaseSchema extends Schema {
	DataType<Object> referenceType();

	@SuppressWarnings("rawtypes")
	DataType<Range> rangeType();

	@SuppressWarnings("rawtypes")
	DataType<Enum> enumType();

	@SuppressWarnings("rawtypes")
	DataType<Class> classType();

	DataType<Boolean> booleanType();

	DataType<String> stringType();

	Model<Object> includeModel();
}
