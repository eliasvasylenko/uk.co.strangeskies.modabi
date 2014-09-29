package uk.co.strangeskies.modabi.schema.processing.impl.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
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
		return bindingNode(0);
	}

	default SchemaNode.Effective<?, ?> bindingNode(int parent) {
		return bindingNodeStack().get(bindingNodeStack().size() - (1 + parent));
	}

	List<Object> bindingTargetStack();

	default Object bindingTarget() {
		return bindingTarget(0);
	}

	default Object bindingTarget(int parent) {
		return bindingTargetStack().get(bindingTargetStack().size() - (1 + parent));
	}

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
			public List<Object> bindingTargetStack() {
				return base.bindingTargetStack();
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

		List<Object> targetStack = new ArrayList<>(base.bindingTargetStack());
		targetStack.add(target);
		List<Object> finalTargetStack = Collections.unmodifiableList(targetStack);

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
			public List<Object> bindingTargetStack() {
				return finalTargetStack;
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

	default <T> BindingContext withReplacedBindingTarget(Object target) {
		BindingContext base = this;

		List<Object> targetStack = new ArrayList<>(base.bindingTargetStack());
		targetStack.remove(targetStack.size() - 1);
		targetStack.add(target);
		List<Object> finalTargetStack = Collections.unmodifiableList(targetStack);

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
			public List<Object> bindingTargetStack() {
				return finalTargetStack;
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
			public List<Object> bindingTargetStack() {
				return base.bindingTargetStack();
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
			public List<Object> bindingTargetStack() {
				return base.bindingTargetStack();
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
