package uk.co.strangeskies.modabi.data;

import java.util.function.Function;

import uk.co.strangeskies.modabi.namespace.NamedSet;
import uk.co.strangeskies.modabi.namespace.Namespace;

public class DataTypes extends NamedSet<DataType<?>> {
	public DataTypes(Namespace namespace) {
		super(namespace, new Function<DataType<?>, String>() {
			@Override
			public String apply(DataType<?> t) {
				return t.getName();
			}
		});
	}
}
