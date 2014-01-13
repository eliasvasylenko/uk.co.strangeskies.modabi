package uk.co.strangeskies.modabi.namespace;

import java.util.Collection;
import java.util.function.Function;

public class NamedSet<T> extends QualifiedNamedSet<T> {
	private Namespace namespace;
	private final Function<T, String> name;

	public NamedSet(final Namespace namespace, final Function<T, String> name) {
		super(new Function<T, QualifiedName>() {
			@Override
			public QualifiedName apply(T t) {
				return new QualifiedName(name.apply(t), namespace);
			}
		});
		this.name = name;
	}

	public void add(T element, Namespace namespace) {
		getElements().put(new QualifiedName(name.apply(element), namespace),
				element);
	}

	public void addAll(Collection<? extends T> elements, Namespace namespace) {
		for (T element : elements) {
			add(element, namespace);
		}
	}

	public T get(String name) {
		return get(new QualifiedName(name, namespace));
	}
}
