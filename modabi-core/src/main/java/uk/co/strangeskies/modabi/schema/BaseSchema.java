package uk.co.strangeskies.modabi.schema;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.DataType;
import uk.co.strangeskies.utilities.Enumeration;

public interface BaseSchema extends Schema {
	public interface DerivedTypes {
		@SuppressWarnings("rawtypes")
		DataBindingType<Collection> collectionType();

		@SuppressWarnings("rawtypes")
		DataBindingType<List> listType();

		@SuppressWarnings("rawtypes")
		DataBindingType<Set> setType();

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

		/*
		 * during binding / unbinding magically adds items to bindings list (so can
		 * be referenced)
		 */
		DataBindingType<Object> includeType();

		/*
		 * retrieves objects already bound by SchemaBinder and 'includes' them, or
		 * some children of them. Blocks if we are waiting for them.
		 */
		DataBindingType<Object> importType();
	}

	public interface BaseModels {
	}

	<T> DataBindingType<T> primitiveType(DataType<T> type);

	DerivedTypes derivedTypes();

	BaseModels models();
}
