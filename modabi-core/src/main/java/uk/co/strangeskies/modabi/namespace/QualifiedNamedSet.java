package uk.co.strangeskies.modabi.namespace;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.IdentityProperty;
import uk.co.strangeskies.gears.utilities.collection.SetDecorator;
import uk.co.strangeskies.gears.utilities.function.collection.SetTransformationView;

public class QualifiedNamedSet<T> extends /* @ReadOnly */SetDecorator<T> {
	private final Function<T, QualifiedName> qualifiedName;
	private final Map<QualifiedName, T> elements;

	public QualifiedNamedSet(Function<T, QualifiedName> name) {
		super(new IdentityProperty<Set<T>>());

		qualifiedName = name;
		elements = new HashMap<>();

		getComponentProperty().set(
				new SetTransformationView<T, T>(elements.values(), e -> e));
	}

	protected Map<QualifiedName, T> getElements() {
		return elements;
	}

	@Override
	public boolean add(T element) {
		return elements.put(qualifiedName.apply(element), element) == null;
	}

	@Override
	public boolean addAll(Collection<? extends T> elements) {
		boolean changed = false;
		for (T element : elements) {
			changed = add(element) || changed;
		}
		return changed;
	}

	public T get(QualifiedName name) {
		return elements.get(name);
	}

	public/* @ReadOnly */Map<QualifiedName, T> getMap() {
		return elements;
	}

	@Override
	public String toString() {
		return elements.toString();
	}
}
