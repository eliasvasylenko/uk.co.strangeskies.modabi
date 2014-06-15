package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

public interface BaseSchema extends Schema {
	public interface BuiltInTypes {
		DataBindingType<QualifiedName> qualifiedNameType();

		DataBindingType<Object> relativeReferenceType();

		DataBindingType<Object> referenceType();

		DataBindingType<BufferedDataSource> bufferedDataType();
	}

	public interface DerivedTypes {
		@SuppressWarnings("rawtypes")
		DataBindingType<Range> rangeType();

		@SuppressWarnings("rawtypes")
		DataBindingType<Enum> enumType();

		@SuppressWarnings("rawtypes")
		DataBindingType<Class> classType();
	}

	public interface BaseModels {
		Model<Object> includeModel();
	}

	<T> DataBindingType<T> primitiveType(DataType<T> type);

	BuiltInTypes builtInTypes();

	DerivedTypes derivedTypes();

	BaseModels models();
}
