package uk.co.strangeskies.modabi.schema;

import java.util.List;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.Reified;
import uk.co.strangeskies.reflection.TypeToken;

public interface BindingPoint<T> extends Reified {
	enum Format {
		COMPLEX, SIMPLE, CONTENT, PROPERTY, INVISIBLE /*
																									 * TODO obviously invisible is
																									 * a bad word... think of
																									 * something better, eli.
																									 * Something that means "not
																									 * bound to io", e.g. provided
																									 * values etc.
																									 */
	}

	QualifiedName name();

	boolean concrete();

	TypeToken<T> dataType();

	@Override
	TypeToken<? extends BindingPoint<T>> getThisType();

	SchemaNode node();

	List<Model<? super T>> baseModel();
}
