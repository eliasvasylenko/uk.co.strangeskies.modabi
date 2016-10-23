package uk.co.strangeskies.modabi.schema;

import java.util.List;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.TypeToken;

public interface BindingPoint<T> extends Reified {
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

	boolean concrete();

	TypeToken<T> dataType();

	@Override
	TypeToken<? extends BindingPoint<T>> getThisType();

	SchemaNode node();

	List<Model<? super T>> baseModel();
}
