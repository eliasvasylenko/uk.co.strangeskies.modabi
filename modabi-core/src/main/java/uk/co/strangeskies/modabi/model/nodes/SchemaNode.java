package uk.co.strangeskies.modabi.model.nodes;

import java.util.List;

import uk.co.strangeskies.gears.utilities.PropertySet;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

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

		@Override
		default boolean equalsImpl(Object object) {
			return propertySet().testEquality(object)
					&& effectivePropertySet().testEquality(object);
		}

		@Override
		default int hashCodeImpl() {
			return propertySet().generateHashCode()
					^ effectivePropertySet().generateHashCode();
		}

		default PropertySet<E> effectivePropertySet() {
			return new PropertySet<>(getEffectiveClass(), effective());
		}
	}

	default boolean equalsImpl(Object object) {
		return propertySet().testEquality(object)
				&& effective().equalsImpl(((SchemaNode<?, ?>) object).effective());
	}

	default int hashCodeImpl() {
		return propertySet().generateHashCode();
	}

	@SuppressWarnings("unchecked")
	default PropertySet<S> propertySet() {
		return new PropertySet<>(getNodeClass(), (S) this)
				.add(SchemaNode::children).add(SchemaNode::getName);
	}

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
		return children().stream().filter(c -> c.getName().equals(name)).findAny()
				.get();
	}

	default ChildNode<?, ?> child(String name) {
		return child(new QualifiedName(name, getName().getNamespace()));
	}
}
