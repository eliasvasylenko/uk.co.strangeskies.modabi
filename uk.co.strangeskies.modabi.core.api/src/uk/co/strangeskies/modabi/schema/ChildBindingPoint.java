package uk.co.strangeskies.modabi.schema;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

public interface ChildBindingPoint<T> extends BindingPoint<T> {
	/**
	 * If a binding point is specified to be ordered then the order in which items
	 * are iterated for output, or formatted for input, is considered semantically
	 * significant. It must therefore be retained through the binding process.
	 * 
	 * <p>
	 * If a binding point is specified to be unordered then the ordering of items
	 * is not semantically significant and may be discarded by the binding
	 * process.
	 * 
	 * <p>
	 * For a binding point with zero or one {@link #bindingCondition()
	 * occurrences} whether or not it is ordered makes no difference.
	 * 
	 * <p>
	 * Binding points which are unordered may not override binding points which
	 * are ordered.
	 * 
	 * @return true if the binding point is ordered, false if it is unordered
	 */
	boolean ordered();

	/**
	 * The binding condition of a binding point describes if and when that point
	 * can be processed during some binding process. By this mechanism it is
	 * possible to
	 * 
	 * @return
	 */
	BindingCondition<? super T> bindingCondition();

	boolean extensible();

	@Override
	TypeToken<ChildBindingPoint<T>> getThisType();

	List<T> providedValues();

	default List<TypedObject<T>> typedProvidedValues() {
		return providedValues().stream().map(dataType()::typedObject).collect(Collectors.toList());
	}

	default T providedValue() {
		if (providedValues() == null || providedValues().isEmpty())
			return null;

		if (providedValues().size() > 1)
			throw new ModabiException(t -> t.cannotProvideSingleValue(name(), providedValues().size()));

		return providedValues().get(0);
	}

	default TypedObject<T> typedProvidedValue() {
		return dataType().typedObject(providedValue());
	}

	default boolean isValueProvided() {
		return providedValues() != null;
	}
}