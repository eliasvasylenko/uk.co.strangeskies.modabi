package uk.co.strangeskies.modabi.schema;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataType;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.utilities.Enumeration;

public interface BaseSchema extends Schema {
	public interface DerivedTypes {
		DataBindingType<Object[]> arrayType();

		DataBindingType<Collection<?>> collectionType();

		DataBindingType<List<?>> listType();

		DataBindingType<Set<?>> setType();

		DataBindingType<Object> referenceType();

		DataBindingType<DataSource> bufferedDataType();

		DataBindingType<Range<?>> rangeType();

		DataBindingType<Enum<?>> enumType();

		DataBindingType<Enumeration<?>> enumerationType();

		DataBindingType<Class<?>> classType();

		DataBindingType<Type> typeType();

		/*
		 * during binding / unbinding magically adds items to bindings list (so can
		 * be referenced)
		 */
		DataBindingType<Collection<?>> includeType();

		/*
		 * retrieves objects already bound by SchemaBinder and 'includes' them, or
		 * some children of them. Blocks if we are waiting for them.
		 */
		DataBindingType<Object> importType();
	}

	public interface BaseModels {}

	<T> DataBindingType<T> primitiveType(DataType<T> type);

	DerivedTypes derivedTypes();

	BaseModels models();
}
