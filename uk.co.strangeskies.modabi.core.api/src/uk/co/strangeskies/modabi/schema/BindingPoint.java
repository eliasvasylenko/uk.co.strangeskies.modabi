package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface BindingPoint<T> {
	enum Format {
		/**
		 * 
		 */
		COMPLEX,
		/**
		 * 
		 */
		PROPERTY,
		/**
		 * 
		 */
		CONTENT,
		/**
		 * Cannot be ordered
		 */
		ABSENT
	}

	QualifiedName name();

	TypeToken<T> dataType();

	SchemaNode<T> node();
}
