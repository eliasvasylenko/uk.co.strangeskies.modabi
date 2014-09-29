package uk.co.strangeskies.modabi.schema.node;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.utilities.PropertySet;

public interface SchemaNode<S extends SchemaNode<S, E>, E extends SchemaNode.Effective<S, E>> {
	interface Effective<S extends SchemaNode<S, E>, E extends Effective<S, E>>
			extends SchemaNode<S, E> {
		@Override
		List<ChildNode.Effective<?, ?>> children();

		@SuppressWarnings("unchecked")
		@Override
		default E effective() {
			return (E) this;
		}

		@Override
		S source();

		default PropertySet<E> effectivePropertySet() {
			return new PropertySet<>(getEffectiveClass(), effective(), true);
		}
	}

	@SuppressWarnings("unchecked")
	default PropertySet<S> propertySet() {
		return new PropertySet<>(getNodeClass(), (S) this, true)
				.add(SchemaNode::children).add(SchemaNode::getName)
				.add(SchemaNode::isAbstract);
	}

	Boolean isAbstract();

	QualifiedName getName();

	List<? extends ChildNode<?, ?>> children();

	E effective();

	@SuppressWarnings("unchecked")
	default S source() {
		return (S) this;
	}

	Class<E> getEffectiveClass();

	Class<S> getNodeClass();

	default ChildNode<?, ?> child(QualifiedName name) {
		return children()
				.stream()
				.filter(c -> c.getName().equals(name))
				.findAny()
				.orElseThrow(
						() -> new SchemaException("Cannot find child '"
								+ name
								+ "' for node '"
								+ getName()
								+ "' amongst children '["
								+ children().stream().map(SchemaNode::getName)
										.map(Objects::toString).collect(Collectors.joining(", "))
								+ "]."));
	}

	default ChildNode<?, ?> child(QualifiedName name, QualifiedName... names) {
		if (names.length == 0)
			return child(name);
		else
			return child(name).child(Arrays.asList(names));
	}

	default ChildNode<?, ?> child(List<QualifiedName> names) {
		if (names.isEmpty())
			throw new IllegalArgumentException();

		if (names.size() == 1)
			return child(names.get(0));
		else
			return child(names.get(0)).child(names.subList(1, names.size()));
	}

	default ChildNode<?, ?> child(String name, String... names) {
		if (names.length == 0)
			return child(new QualifiedName(name, getName().getNamespace()));
		else
			return child(name).child(names[0],
					Arrays.copyOfRange(names, 1, names.length));
	}
}
