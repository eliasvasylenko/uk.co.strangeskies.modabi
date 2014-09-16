package uk.co.strangeskies.modabi.schema.processing.impl.unbinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode.Effective;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.UnbindingException;
import uk.co.strangeskies.modabi.schema.processing.reference.IncludeTarget;
import uk.co.strangeskies.utilities.factory.Factory;

public interface UnbindingContext {
	<U> U provide(Class<U> clazz);

	List<SchemaNode.Effective<?, ?>> unbindingNodeStack();

	Object unbindingSource();

	StructuredDataTarget output();

	Bindings bindings();

	<T> List<Model<? extends T>> getMatchingModels(
			ElementNode.Effective<T> element, Class<?> dataClass);

	public default IncludeTarget provideIncludeTarget() {
		return new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, U object) {
				bindings().add(model, object);

				output().registerNamespaceHint(model.getName().getNamespace());
			}
		};
	}

	default UnbindingException exception(String message, Exception cause) {
		return new UnbindingException(message, unbindingNodeStack(), cause);
	}

	default UnbindingException exception(String message) {
		return new UnbindingException(message, unbindingNodeStack());
	}

	default <T> UnbindingContext withProvision(Class<T> providedClass,
			Factory<T> provider) {
		UnbindingContext base = this;
		return new UnbindingContext() {
			@SuppressWarnings("unchecked")
			@Override
			public <U> U provide(Class<U> clazz) {
				if (clazz.equals(providedClass))
					return (U) provider.create();

				return base.provide(clazz);
			}

			@Override
			public <U> List<Model<? extends U>> getMatchingModels(
					Effective<U> element, Class<?> dataClass) {
				return base.getMatchingModels(element, dataClass);
			}

			@Override
			public StructuredDataTarget output() {
				return base.output();
			}

			@Override
			public Object unbindingSource() {
				return base.unbindingSource();
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> unbindingNodeStack() {
				return base.unbindingNodeStack();
			}

			@Override
			public Bindings bindings() {
				return base.bindings();
			}
		};
	}

	default <T> UnbindingContext withUnbindingSource(Object target) {
		UnbindingContext base = this;
		return new UnbindingContext() {
			@Override
			public <U> List<Model<? extends U>> getMatchingModels(
					Effective<U> element, Class<?> dataClass) {
				return base.getMatchingModels(element, dataClass);
			}

			@Override
			public <U> U provide(Class<U> clazz) {
				return base.provide(clazz);
			}

			@Override
			public StructuredDataTarget output() {
				return base.output();
			}

			@Override
			public Object unbindingSource() {
				return target;
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> unbindingNodeStack() {
				return base.unbindingNodeStack();
			}

			@Override
			public Bindings bindings() {
				return base.bindings();
			}
		};
	}

	default <T> UnbindingContext withUnbindingNode(SchemaNode.Effective<?, ?> node) {
		UnbindingContext base = this;
		return new UnbindingContext() {
			@Override
			public <U> List<Model<? extends U>> getMatchingModels(
					Effective<U> element, Class<?> dataClass) {
				return base.getMatchingModels(element, dataClass);
			}

			@Override
			public <U> U provide(Class<U> clazz) {
				return base.provide(clazz);
			}

			@Override
			public StructuredDataTarget output() {
				return base.output();
			}

			@Override
			public Object unbindingSource() {
				return base.unbindingSource();
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> unbindingNodeStack() {
				List<SchemaNode.Effective<?, ?>> bindingStack = new ArrayList<>(
						base.unbindingNodeStack());
				bindingStack.add(node);
				return Collections.unmodifiableList(bindingStack);
			}

			@Override
			public Bindings bindings() {
				return base.bindings();
			}
		};
	}

	default UnbindingContext withOutput(StructuredDataTarget output) {
		UnbindingContext base = this;
		UnbindingContext context = new UnbindingContext() {
			@Override
			public <U> List<Model<? extends U>> getMatchingModels(
					Effective<U> element, Class<?> dataClass) {
				return base.getMatchingModels(element, dataClass);
			}

			@Override
			public StructuredDataTarget output() {
				return output;
			}

			@Override
			public Object unbindingSource() {
				return base.unbindingSource();
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> unbindingNodeStack() {
				return base.unbindingNodeStack();
			}

			@Override
			public Bindings bindings() {
				return base.bindings();
			}

			@Override
			public <U> U provide(Class<U> clazz) {
				return base.provide(clazz);
			}
		};

		return context.withProvision(IncludeTarget.class,
				context::provideIncludeTarget);
	}
}
