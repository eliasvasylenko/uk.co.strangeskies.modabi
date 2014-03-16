package uk.co.strangeskies.modabi.namespace;

import java.util.Collection;
import java.util.function.Function;

public class NamedSet<T> extends QualifiedNamedSet<T> {
	private Namespace namespace;
	private final Function<T, String> namingFunction;

	public NamedSet(final Namespace namespace,
			final Function<T, String> namingFunction) {
		super(new Function<T, QualifiedName>() {
			@Override
			public QualifiedName apply(T t) {
				return new QualifiedName(namingFunction.apply(t), namespace);
			}
		});
		this.namingFunction = namingFunction;
	}

	public boolean add(T element, Namespace namespace) {
		QualifiedName name = new QualifiedName(namingFunction.apply(element),
				namespace);
		if (getElements().get(name) != null)
			return false;

		getElements().put(name, element);
		return true;
	}

	public boolean addAll(Collection<? extends T> elements, Namespace namespace) {
		boolean changed = false;
		for (T element : elements) {
			changed = add(element, namespace) || changed;
		}
		return changed;
	}

	public T get(String name) {
		return get(new QualifiedName(name, namespace));
	}
}
