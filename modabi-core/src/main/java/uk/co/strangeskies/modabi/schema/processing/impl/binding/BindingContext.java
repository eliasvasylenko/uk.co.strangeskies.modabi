package uk.co.strangeskies.modabi.schema.processing.impl.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.BindingException;
import uk.co.strangeskies.utilities.factory.Factory;

public interface BindingContext {
	default <U> U provide(Class<U> clazz) {
		return provide(clazz, this);
	}

	<U> U provide(Class<U> clazz, BindingContext headContext);

	boolean isProvided(Class<?> clazz);

	List<SchemaNode.Effective<?, ?>> bindingNodeStack();

	default SchemaNode.Effective<?, ?> bindingNode() {
		return bindingNodeStack().get(bindingNodeStack().size() - 1);
	}

	default SchemaNode.Effective<?, ?> bindingNode(int parent) {
		return bindingNodeStack().get(bindingNodeStack().size() - 1 - parent);
	}

	Object bindingTarget();

	Model.Effective<?> getModel(QualifiedName nextElement);

	StructuredDataSource input();

	Bindings bindings();

	<T> List<DataBindingType<? extends T>> getMatchingTypes(
			DataNode.Effective<T> node, Class<?> dataClass);

	default BindingException exception(String message, Exception cause) {
		return new BindingException(message, bindingNodeStack(), cause);
	}

	default BindingException exception(String message) {
		return new BindingException(message, bindingNodeStack());
	}

	default <T> BindingContext withProvision(Class<T> providedClass,
			Factory<T> provider) {
		return withProvision(providedClass, c -> provider.create());
	}

	default <T> BindingContext withProvision(Class<T> providedClass,
			Function<BindingContext, T> provider) {
		BindingContext base = this;
		return new BindingContext() {
			@SuppressWarnings("unchecked")
			@Override
			public <U> U provide(Class<U> clazz, BindingContext headContext) {
				if (clazz.equals(providedClass))
					return (U) provider.apply(headContext);

				return base.provide(clazz, headContext);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return clazz.equals(providedClass) || base.isProvided(clazz);
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

			@Override
			public <U> List<DataBindingType<? extends U>> getMatchingTypes(
					DataNode.Effective<U> node, Class<?> dataClass) {
				return base.getMatchingTypes(node, dataClass);
			}
		};
	}

	default <T> BindingContext withBindingTarget(Object target) {
		BindingContext base = this;
		return new BindingContext() {
			@Override
			public <U> U provide(Class<U> clazz, BindingContext headContext) {
				return base.provide(clazz, headContext);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return base.isProvided(clazz);
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

			@Override
			public <U> List<DataBindingType<? extends U>> getMatchingTypes(
					DataNode.Effective<U> node, Class<?> dataClass) {
				return base.getMatchingTypes(node, dataClass);
			}
		};
	}

	default <T> BindingContext withBindingNode(SchemaNode.Effective<?, ?> node) {
		BindingContext base = this;

		List<SchemaNode.Effective<?, ?>> bindingStack = new ArrayList<>(
				base.bindingNodeStack());
		bindingStack.add(node);
		List<SchemaNode.Effective<?, ?>> finalBindingStack = Collections
				.unmodifiableList(bindingStack);

		return new BindingContext() {
			@Override
			public <U> U provide(Class<U> clazz, BindingContext headContext) {
				return base.provide(clazz, headContext);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return base.isProvided(clazz);
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
				return finalBindingStack;
			}

			@Override
			public Bindings bindings() {
				return base.bindings();
			}

			@Override
			public <U> List<DataBindingType<? extends U>> getMatchingTypes(
					DataNode.Effective<U> node, Class<?> dataClass) {
				return base.getMatchingTypes(node, dataClass);
			}
		};
	}

	default BindingContext withInput(StructuredDataSource input) {
		BindingContext base = this;
		return new BindingContext() {
			@Override
			public <U> U provide(Class<U> clazz, BindingContext headContext) {
				return base.provide(clazz, headContext);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return base.isProvided(clazz);
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

			@Override
			public <U> List<DataBindingType<? extends U>> getMatchingTypes(
					DataNode.Effective<U> node, Class<?> dataClass) {
				return base.getMatchingTypes(node, dataClass);
			}
		};
	}
}
