package uk.co.strangeskies.modabi.model.nodes;

import java.util.List;

import uk.co.strangeskies.gears.utilities.PropertySet;

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
			return propertySet().equal(object)
					&& effectivePropertySet().equals(object);
		}

		@Override
		default int hashCodeImpl() {
			return propertySet().hashCode() ^ effectivePropertySet().hashCode();
		}

		default PropertySet<E> effectivePropertySet() {
			return new PropertySet<>(getEffectiveClass(), effective());
		}
	}

	default boolean equalsImpl(Object object) {
		return propertySet().equal(object)
				&& effective().equalsImpl(((SchemaNode<?, ?>) object).effective());
	}

	default int hashCodeImpl() {
		return propertySet().hashCode();
	}

	default PropertySet<S> propertySet() {
		return new PropertySet<>(getNodeClass(), source())
				.add(SchemaNode::children).add(SchemaNode::getName);
	}

	String getName();

	List<? extends ChildNode<?, ?>> children();

	E effective();

	@SuppressWarnings("unchecked")
	default S source() {
		return (S) this;
	}

	Class<E> getEffectiveClass();

	Class<S> getNodeClass();
}
