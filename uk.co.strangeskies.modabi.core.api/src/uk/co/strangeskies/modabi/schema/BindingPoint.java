package uk.co.strangeskies.modabi.schema;

import java.util.List;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.token.TypeToken;

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

	SchemaNode node();

	/**
	 * @return the set of all <em>direct</em> base models, i.e. excluding those
	 *         which are transitively implied via other more specific base models
	 */
	List<Model<? super T>> baseModel();
}
