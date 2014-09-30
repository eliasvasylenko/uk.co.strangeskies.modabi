package uk.co.strangeskies.modabi.schema.processing.unbinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.ElementNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.utilities.factory.Factory;

public interface UnbindingContext {
	default <U> U provide(Class<U> clazz) {
		return provide(clazz, this);
	}

	<U> U provide(Class<U> clazz, UnbindingContext headContext);

	boolean isProvided(Class<?> clazz);

	List<SchemaNode.Effective<?, ?>> unbindingNodeStack();

	Object unbindingSource();

	StructuredDataTarget output();

	Bindings bindings();

	<T> List<Model.Effective<T>> getMatchingModels(Class<T> dataClass);

	<T> List<Model.Effective<? extends T>> getMatchingModels(
			ElementNode.Effective<T> element, Class<? extends T> dataClass);

	<T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
			DataNode.Effective<T> node, Class<?> dataClass);

	default UnbindingException exception(String message, Exception cause) {
		return new UnbindingException(message, unbindingNodeStack(), cause);
	}

	default UnbindingException exception(String message,
			Collection<? extends Exception> cause) {
		return new UnbindingException(message, unbindingNodeStack(), cause);
	}

	default UnbindingException exception(String message) {
		return new UnbindingException(message, unbindingNodeStack());
	}

	default <T> UnbindingContext withProvision(Class<T> providedClass,
			Factory<T> provider) {
		return withProvision(providedClass, c -> provider.create());
	}

	default <T> UnbindingContext withProvision(Class<T> providedClass,
			Function<UnbindingContext, T> provider) {
		UnbindingContext base = this;
		return new UnbindingContext() {
			@SuppressWarnings("unchecked")
			@Override
			public <U> U provide(Class<U> clazz, UnbindingContext headContext) {
				if (clazz.equals(providedClass))
					return (U) provider.apply(headContext);

				return base.provide(clazz, headContext);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return clazz.equals(providedClass) || base.isProvided(clazz);
			}

			@Override
			public <U> List<Model.Effective<U>> getMatchingModels(Class<U> dataClass) {
				return base.getMatchingModels(dataClass);
			}

			@Override
			public <U> List<Model.Effective<? extends U>> getMatchingModels(
					ElementNode.Effective<U> element, Class<? extends U> dataClass) {
				return base.getMatchingModels(element, dataClass);
			}

			@Override
			public <U> List<DataBindingType.Effective<? extends U>> getMatchingTypes(
					DataNode.Effective<U> node, Class<?> dataClass) {
				return base.getMatchingTypes(node, dataClass);
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
			public <U> List<Model.Effective<U>> getMatchingModels(Class<U> dataClass) {
				return base.getMatchingModels(dataClass);
			}

			@Override
			public <U> List<Model.Effective<? extends U>> getMatchingModels(
					ElementNode.Effective<U> element, Class<? extends U> dataClass) {
				return base.getMatchingModels(element, dataClass);
			}

			@Override
			public <U> List<DataBindingType.Effective<? extends U>> getMatchingTypes(
					DataNode.Effective<U> node, Class<?> dataClass) {
				return base.getMatchingTypes(node, dataClass);
			}

			@Override
			public <U> U provide(Class<U> clazz, UnbindingContext headContext) {
				return base.provide(clazz, headContext);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return base.isProvided(clazz);
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

		List<SchemaNode.Effective<?, ?>> unbindingStack = new ArrayList<>(
				base.unbindingNodeStack());
		unbindingStack.add(node);
		List<SchemaNode.Effective<?, ?>> finalUnbindingStack = Collections
				.unmodifiableList(unbindingStack);

		return new UnbindingContext() {
			@Override
			public <U> List<Model.Effective<U>> getMatchingModels(Class<U> dataClass) {
				return base.getMatchingModels(dataClass);
			}

			@Override
			public <U> List<Model.Effective<? extends U>> getMatchingModels(
					ElementNode.Effective<U> element, Class<? extends U> dataClass) {
				return base.getMatchingModels(element, dataClass);
			}

			@Override
			public <U> List<DataBindingType.Effective<? extends U>> getMatchingTypes(
					DataNode.Effective<U> node, Class<?> dataClass) {
				return base.getMatchingTypes(node, dataClass);
			}

			@Override
			public <U> U provide(Class<U> clazz, UnbindingContext headContext) {
				return base.provide(clazz, headContext);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return base.isProvided(clazz);
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
				return finalUnbindingStack;
			}

			@Override
			public Bindings bindings() {
				return base.bindings();
			}
		};
	}

	default UnbindingContext withOutput(StructuredDataTarget output) {
		UnbindingContext base = this;
		return new UnbindingContext() {
			@Override
			public <U> List<Model.Effective<U>> getMatchingModels(Class<U> dataClass) {
				return base.getMatchingModels(dataClass);
			}

			@Override
			public <U> List<Model.Effective<? extends U>> getMatchingModels(
					ElementNode.Effective<U> element, Class<? extends U> dataClass) {
				return base.getMatchingModels(element, dataClass);
			}

			@Override
			public <U> List<DataBindingType.Effective<? extends U>> getMatchingTypes(
					DataNode.Effective<U> node, Class<?> dataClass) {
				return base.getMatchingTypes(node, dataClass);
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
			public <U> U provide(Class<U> clazz, UnbindingContext headContext) {
				return base.provide(clazz, headContext);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return base.isProvided(clazz);
			}
		};
	}
}
