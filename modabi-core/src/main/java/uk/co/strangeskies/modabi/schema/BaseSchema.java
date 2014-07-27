package uk.co.strangeskies.modabi.schema;

import java.util.List;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.gears.utilities.Enumeration;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface BaseSchema extends Schema {
	public interface DerivedTypes {
		@SuppressWarnings("rawtypes")
		DataBindingType<List> listType();

		@SuppressWarnings("rawtypes")
		DataBindingType<Set> setType();

		DataBindingType<QualifiedName> qualifiedNameType();

		DataBindingType<Object> referenceType();

		DataBindingType<BufferedDataSource> bufferedDataType();

		@SuppressWarnings("rawtypes")
		DataBindingType<Range> rangeType();

		@SuppressWarnings("rawtypes")
		DataBindingType<Enum> enumType();

		@SuppressWarnings("rawtypes")
		DataBindingType<Enumeration> enumerationType();

		@SuppressWarnings("rawtypes")
		DataBindingType<Class> classType();
	}

	public interface BaseModels {
		Model<Object> includeModel();
	}

	<T> DataBindingType<T> primitiveType(DataType<T> type);

	DerivedTypes derivedTypes();

	BaseModels models();
}
