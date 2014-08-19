package uk.co.strangeskies.modabi.schema.processing.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ClassUtils;

import uk.co.strangeskies.modabi.data.io.BufferedDataSource;
import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.DataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.impl.ElementNodeWrapper;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.reference.DereferenceTarget;

class SchemaSavingContext<T> implements SchemaProcessingContext {
	private final SchemaBinderImpl schemaBinderImpl;

	private final StructuredDataTarget output;

	private final Deque<Object> bindingStack;

	private BufferingDataTarget dataTarget;
	private final DereferenceTarget dereferenceTarget;

	private final Bindings bindings;

	public SchemaSavingContext(SchemaBinderImpl schemaBinderImpl, Model<T> model,
			StructuredDataTarget output, T data) {
		this.schemaBinderImpl = schemaBinderImpl;
		bindingStack = new ArrayDeque<>();
		this.output = output;

		output.registerDefaultNamespaceHint(model.getName().getNamespace(), true);

		bindings = new Bindings();

		dereferenceTarget = new DereferenceTarget() {
			@Override
			public <U> BufferedDataSource dereference(Model<U> model,
					String idDomain, U object) {
				return new BufferingDataTarget().buffer();
				/*-
				if (!bindings.get(model).contains(object)) {
					bindings.print();

					throw new SchemaException("Cannot find any instance '" + object
							+ "' bound to model '" + model.getName());
				}

				DataNode.Effective<?> node = (DataNode.Effective<?>) model
						.effective()
						.children()
						.stream()
						.filter(
								c -> c.getName().equals(idDomain)
										&& c instanceof DataNode.Effective<?>).findAny()
						.orElseThrow(SchemaException::new);

				bindingStack.push(object);

				BufferedDataSource bufferedData = unbindDataNode(node,
						new BufferingDataTarget()).buffer();

				bindingStack.pop();

				return bufferedData;*/
			}
		};

		unbindModel(model.effective(), data);
	}

	@Override
	public <U> void accept(ElementNode.Effective<U> node) {
		for (U child : getData(node))
			unbindElement(node, child);
	}

	@Override
	public <U> void accept(DataNode.Effective<U> node) {
		if (dataTarget == null) {
			if (node.format() == null)
				throw new SchemaException(
						"Data format must be provided for data node '" + node.getName()
								+ "'.");
			dataTarget = new BufferingDataTarget();
		} else if (node.format() != null)
			throw new SchemaException(
					"Data format should be null for nested data node '" + node.getName()
							+ "'.");

		unbindDataNode(node, dataTarget);

		if (node.format() != null) {
			BufferedDataSource bufferedTarget = dataTarget.buffer();
			dataTarget = null;

			if (bufferedTarget.size() > 0)
				switch (node.format()) {
				case PROPERTY:
					bufferedTarget.pipe(output.property(node.getName())).terminate();
					break;
				case SIMPLE_ELEMENT:
					output.nextChild(node.getName());
					bufferedTarget.pipe(output.content()).terminate();
					output.endChild();
					break;
				case CONTENT:
					bufferedTarget.pipe(output.content()).terminate();
				}
		}
	}

	public <U> BufferingDataTarget unbindDataNode(DataNode.Effective<U> node,
			BufferingDataTarget target) {
		if (node.isAbstract() != null && node.isAbstract()) {
			/*
			 * TODO allow node.isAbstract() rather than only on type. choose specific
			 * concrete data type using "@<data-type-qualified-name>, <data-values>"
			 * syntax, e.g. "@intVector, 1, 2, 3". This will also help in cases where
			 * the expected data type is not abstract, but is ambiguous, for example
			 * due to a choice node in the data node tree. Also a good idea to allow,
			 * but not require, data to be grouped with braces, e.g. "{1, 2, 3}", or
			 * "{@intVector, 1, 2, 3}". This should further help resolve data
			 * structure when groupings are ambiguous.
			 */
		}

		BufferingDataTarget previousDataTarget = dataTarget;
		dataTarget = target;

		for (U item : getData(node))
			processBindingChildren(node, unbindData(node, item));

		dataTarget = previousDataTarget;

		return target;
	}

	@Override
	public void accept(InputSequenceNode.Effective node) {
		processChildren(node);
	}

	@Override
	public void accept(SequenceNode.Effective node) {
		processChildren(node);
	}

	@Override
	public void accept(ChoiceNode.Effective node) {
	}

	@SuppressWarnings("unchecked")
	private <U> U provide(Class<U> clazz) {
		if (clazz.equals(DataTarget.class))
			return (U) dataTarget;
		if (clazz.equals(DereferenceTarget.class))
			return (U) dereferenceTarget;

		return this.schemaBinderImpl.provide(clazz);
	}

	private void processChildren(SchemaNode.Effective<?, ?> node) {
		for (ChildNode.Effective<?, ?> child : node.children())
			child.process(this);
	}

	private void processBindingChildren(SchemaNode.Effective<?, ?> node,
			Object binding) {
		bindingStack.push(binding);
		processChildren(node);
		bindingStack.pop();
	}

	// TODO should work with generics & without warning suppression
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <U> void unbindElement(ElementNode.Effective<U> node, U data) {
		if (node.isAbstract() != null && node.isAbstract()) {
			List<Model<? extends U>> models = this.schemaBinderImpl.registeredModels
					.getMatchingModels(node, data.getClass());

			if (models.isEmpty())
				throw new SchemaException("Unable to find model to satisfy element '"
						+ node.getName()
						+ "' with model '"
						+ node.effective().baseModel().stream()
								.map(m -> m.source().getName().toString())
								.collect(Collectors.joining(", ")) + "' for object '" + data
						+ "' to be unbound");

			node = new ElementNodeWrapper(models.get(0).effective(), node);
		}

		bindings.add(node, data);
		unbindModel(node, data);
	}

	private <U> void unbindModel(AbstractModel.Effective<? extends U, ?, ?> node,
			U data) {
		output.nextChild(node.getName());
		processBindingChildren(node, unbindData(node, data));
		output.endChild();
	}

	@SuppressWarnings("unchecked")
	public <U> List<U> getData(BindingChildNode.Effective<U, ?, ?> node) {
		List<U> itemList;

		if (node.getOutMethodName() != "null") {
			Object parent = bindingStack.peek();

			if (node.getDataClass() == null)
				return (List<U>) Arrays.asList("");

			if (node.isOutMethodIterable() != null && node.isOutMethodIterable()) {
				Iterable<U> iterable = null;
				if (node.getOutMethodName().equals("this"))
					iterable = (Iterable<U>) parent;
				else
					iterable = (Iterable<U>) invokeMethod(node.getOutMethod(), parent);

				itemList = StreamSupport.stream(iterable.spliterator(), false)
						.filter(Objects::nonNull).collect(Collectors.toList());
				U failedCast = itemList
						.stream()
						.filter(
								o -> !ClassUtils.isAssignable(o.getClass(), node.getDataClass()))
						.findAny().orElse(null);
				if (failedCast != null)
					throw new ClassCastException("Cannot cast " + failedCast.getClass()
							+ " to " + node.getDataClass());
			} else {
				U item;
				if (node.getOutMethodName() != null
						&& node.getOutMethodName().equals("this"))
					item = (U) parent;
				else
					item = (U) invokeMethod(node.getOutMethod(), parent);

				if (item == null)
					itemList = new ArrayList<>();
				else {
					if (!ClassUtils.isAssignable(item.getClass(), node.getDataClass()))
						throw new ClassCastException("Cannot cast " + item.getClass()
								+ " to " + node.getDataClass());
					itemList = Arrays.asList(item);
				}
			}
		} else
			itemList = new ArrayList<>();

		return itemList;
	}

	private Object invokeMethod(Method method, Object receiver,
			Object... parameters) {
		try {
			return method.invoke(receiver, parameters);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | SecurityException | NullPointerException e) {
			throw new SchemaException("Cannot invoke method " + method + " on "
					+ receiver, e);
		}
	}

	public <U> Object unbindData(BindingNode.Effective<? extends U, ?, ?> node,
			U data) {
		Function<Object, Object> supplier = Function.identity();
		if (node.getUnbindingStrategy() != null) {
			switch (node.getUnbindingStrategy()) {
			case SIMPLE:
				break;
			case PASS_TO_PROVIDED:
				supplier = u -> {
					Object o = provide(node.getUnbindingClass());
					invokeMethod(node.getUnbindingMethod(), o,
							prepareUnbingingParameterList(node, u));
					return o;
				};
				break;
			case ACCEPT_PROVIDED:
				supplier = u -> {
					Object o = provide(node.getUnbindingClass());
					invokeMethod(node.getUnbindingMethod(), u,
							prepareUnbingingParameterList(node, o));
					return o;
				};
				break;
			case CONSTRUCTOR:
				supplier = u -> {
					Constructor<?> c = null;
					try {
						c = node.getUnbindingClass().getConstructor(u.getClass());
						return c.newInstance(prepareUnbingingParameterList(node, u));
					} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						throw new SchemaException("Cannot invoke constructor " + c + " on "
								+ node.getUnbindingClass(), e);
					}
				};
				break;
			case STATIC_FACTORY:
				supplier = u -> invokeMethod(node.getUnbindingMethod(), null,
						prepareUnbingingParameterList(node, u));

				break;
			case PROVIDED_FACTORY:
				supplier = u -> invokeMethod(node.getUnbindingMethod(),
						provide(node.getUnbindingFactoryClass()),
						prepareUnbingingParameterList(node, u));

				break;
			}
		}

		return supplier.apply(data);
	}

	private Object[] prepareUnbingingParameterList(
			BindingNode.Effective<?, ?, ?> node, Object data) {
		List<Object> parameters = new ArrayList<>();

		boolean addedData = false;
		if (node.getProvidedUnbindingMethodParameters() != null)
			for (DataNode.Effective<?> parameter : node
					.getProvidedUnbindingMethodParameters()) {
				if (parameter != null)
					parameters.add(parameter.providedValue());
				else {
					parameters.add(data);
					addedData = true;
				}
			}
		if (!addedData)
			parameters.add(0, data);

		return parameters.toArray();
	}
}
