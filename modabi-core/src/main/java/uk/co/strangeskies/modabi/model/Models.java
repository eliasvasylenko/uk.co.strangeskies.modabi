package uk.co.strangeskies.modabi.model;

import java.util.function.Function;

import uk.co.strangeskies.modabi.namespace.NamedSet;
import uk.co.strangeskies.modabi.namespace.Namespace;

public class Models extends NamedSet<Model<?>> {
	public Models(Namespace namespace) {
		super(namespace, new Function<Model<?>, String>() {
			@Override
			public String apply(Model<?> t) {
				return t.getId();
			}
		});
	}
}
