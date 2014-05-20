package uk.co.strangeskies.modabi.data;

import java.util.function.Function;

import uk.co.strangeskies.modabi.namespace.NamedSet;
import uk.co.strangeskies.modabi.namespace.Namespace;

public class DataBindingTypes extends NamedSet<DataBindingType<?>> {
	public DataBindingTypes(Namespace namespace) {
		super(namespace, new Function<DataBindingType<?>, String>() {
			@Override
			public String apply(DataBindingType<?> t) {
				return t.getName();
			}
		});
	}
}
