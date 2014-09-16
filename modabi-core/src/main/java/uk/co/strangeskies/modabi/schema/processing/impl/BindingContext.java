package uk.co.strangeskies.modabi.schema.processing.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.BindingException;
import uk.co.strangeskies.utilities.factory.Factory;

public interface BindingContext {
	<U> U provide(Class<U> clazz);

	List<SchemaNode.Effective<?, ?>> bindingNodeStack();

	Object bindingTarget();

	Model.Effective<?> getModel(QualifiedName nextElement);

	StructuredDataSource input();

	Bindings bindings();

	default BindingException exception(String message, Exception cause) {
		return new BindingException(message, bindingNodeStack(), cause);
	}

	default BindingException exception(String message) {
		return new BindingException(message, bindingNodeStack());
	}

	default <T> BindingContext withProvision(Class<T> providedClass,
			Factory<T> provider) {
		BindingContext base = this;
		return new BindingContext() {
			@SuppressWarnings("unchecked")
			@Override
			public <U> U provide(Class<U> clazz) {
				if (clazz.equals(providedClass))
					return (U) provider.create();

				return base.provide(clazz);
			}

			@Override
			public StructuredDataSource input() {
				return base.input();
			}

			@Override
			public Model.Effective<?> getModel(QualifiedName nextElement) {
				return base.getModel(nextElement);
			}

			@Override
			public Object bindingTarget() {
				return base.bindingTarget();
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> bindingNodeStack() {
				return base.bindingNodeStack();
			}

			@Override
			public Bindings bindings() {
				return base.bindings();
			}
		};
	}

	default <T> BindingContext withBindingTarget(Object target) {
		BindingContext base = this;
		return new BindingContext() {
			@Override
			public <U> U provide(Class<U> clazz) {
				return base.provide(clazz);
			}

			@Override
			public StructuredDataSource input() {
				return base.input();
			}

			@Override
			public Model.Effective<?> getModel(QualifiedName nextElement) {
				return base.getModel(nextElement);
			}

			@Override
			public Object bindingTarget() {
				return target;
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> bindingNodeStack() {
				return base.bindingNodeStack();
			}

			@Override
			public Bindings bindings() {
				return base.bindings();
			}
		};
	}

	default <T> BindingContext withBindingNode(SchemaNode.Effective<?, ?> node) {
		BindingContext base = this;
		return new BindingContext() {
			@Override
			public <U> U provide(Class<U> clazz) {
				return base.provide(clazz);
			}

			@Override
			public StructuredDataSource input() {
				return base.input();
			}

			@Override
			public Model.Effective<?> getModel(QualifiedName nextElement) {
				return base.getModel(nextElement);
			}

			@Override
			public Object bindingTarget() {
				return base.bindingTarget();
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> bindingNodeStack() {
				List<SchemaNode.Effective<?, ?>> bindingStack = new ArrayList<>(
						base.bindingNodeStack());
				bindingStack.add(node);
				return Collections.unmodifiableList(bindingStack);
			}

			@Override
			public Bindings bindings() {
				return base.bindings();
			}
		};
	}

	default BindingContext withInput(StructuredDataSource input) {
		BindingContext base = this;
		return new BindingContext() {
			@Override
			public <U> U provide(Class<U> clazz) {
				return base.provide(clazz);
			}

			@Override
			public StructuredDataSource input() {
				return input;
			}

			@Override
			public Model.Effective<?> getModel(QualifiedName nextElement) {
				return base.getModel(nextElement);
			}

			@Override
			public Object bindingTarget() {
				return base.bindingTarget();
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> bindingNodeStack() {
				return base.bindingNodeStack();
			}

			@Override
			public Bindings bindings() {
				return base.bindings();
			}
		};
	}
}
