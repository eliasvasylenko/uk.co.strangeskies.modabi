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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ClassUtils;

import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.data.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.data.io.DataTarget;
import uk.co.strangeskies.modabi.data.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.impl.DataNodeWrapper;
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
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.reference.DereferenceTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.ImportDereferenceTarget;
import uk.co.strangeskies.utilities.MultiException;

class SchemaSavingContext<T> implements SchemaProcessingContext {
	private final SchemaBinderImpl schemaBinderImpl;

	private StructuredDataTarget output;

	private Deque<Object> bindingStack;
	private Deque<SchemaNode<?, ?>> nodeStack;

	private BufferingDataTarget dataTarget;
	private final DereferenceTarget dereferenceTarget;
	private final IncludeTarget includeTarget;
	private final ImportDereferenceTarget importTarget;

	private final Bindings bindings;

	public SchemaSavingContext(SchemaBinderImpl schemaBinderImpl, Model<T> model,
			StructuredDataTarget output, T data) {
		this.schemaBinderImpl = schemaBinderImpl;
		bindingStack = new ArrayDeque<>();
		nodeStack = new ArrayDeque<>();
		nodeStack.push(model);
		this.output = output;

		output.registerDefaultNamespaceHint(model.getName().getNamespace());

		bindings = new Bindings();

		dereferenceTarget = new DereferenceTarget() {
			@Override
			public <U> DataSource dereference(Model<U> model,
					QualifiedName idDomain, U object) {
				if (!bindings.get(model).contains(object))
					throw new SchemaException("Cannot find any instance '" + object
							+ "' bound to model '" + model.getName() + "'.");

				return importTarget.dereferenceImport(model, idDomain, object);
			}
		};

		includeTarget = new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, U object) {
				bindings.add(model, object);

				SchemaSavingContext.this.output.registerNamespaceHint(model.getName()
						.getNamespace());
			}
		};

		importTarget = new ImportDereferenceTarget() {
			@Override
			public <U> DataSource dereferenceImport(Model<U> model,
					QualifiedName idDomain, U object) {
				DataNode.Effective<?> node = (DataNode.Effective<?>) model
						.effective()
						.children()
						.stream()
						.filter(
								c -> c.getName().equals(idDomain)
										&& c instanceof DataNode.Effective<?>)
						.findAny()
						.orElseThrow(
								() -> new SchemaException("Can't fine child '" + idDomain
										+ "' to target for model '" + model + "'."));

				bindingStack.push(object);

				DataSource bufferedData = unbindDataNode(node,
						new BufferingDataTarget()).buffer();

				bindingStack.pop();

				return bufferedData;
			}
		};

		try {
			unbindModel(model.effective(), data);
		} catch (SchemaException e) {
			throw new SchemaException("Problem at node '"
					+ nodeStack.stream().map(n -> n.getName().toString())
							.collect(Collectors.joining(" < ")) + "' unbinding data '" + data
					+ "' with model '" + model.getName() + "'.", e);
		} catch (Exception e) {
			throw new SchemaException("Unexpected problem at node '"
					+ getNodeStackString() + "' unbinding data '" + data
					+ "' with model '" + model.getName() + "'.", e);
		}
	}

	private String getNodeStackString() {
		return nodeStack.stream().map(n -> n.getName().toString())
				.collect(Collectors.joining(" < "));
	}

	@Override
	public <U> void accept(ElementNode.Effective<U> node) {
		nodeStack.push(node);
		for (U child : getData(node))
			unbindElement(node, child);
		nodeStack.pop();
	}

	@Override
	public <U> void accept(DataNode.Effective<U> node) {
		nodeStack.push(node);
		if (node.getOutMethodName() == null
				|| !node.getOutMethodName().equals("null")) {
			if (dataTarget == null) {
				if (node.format() == null)
					throw new SchemaException(
							"Data format must be provided for data node '" + node.getName()
									+ "'.");
				dataTarget = new BufferingDataTarget();
			} else if (node.format() != null)
				throw new SchemaException(
						"Data format should be null for nested data node '"
								+ node.getName() + "'.");

			if (node.isValueProvided())
				switch (node.valueResolution()) {
				case PROCESSING_TIME:

					break;
				case REGISTRATION_TIME:
					List<U> data = getData(node);
					if (!node.providedValue().equals(data)) {
						throw new SchemaException("Provided value '" + node.providedValue()
								+ "'does not match unbinding object '" + data + "' for node '"
								+ node.getName() + "'.");
					}
					break;
				}
			else
				unbindDataNode(node, dataTarget);

			if (node.format() != null) {
				DataSource bufferedTarget = dataTarget.buffer();
				dataTarget = null;

				if (bufferedTarget.size() > 0)
					switch (node.format()) {
					case PROPERTY:
						bufferedTarget.pipe(output.writeProperty(node.getName())).terminate();
						break;
					case SIMPLE_ELEMENT:
						output.nextChild(node.getName());
						bufferedTarget.pipe(output.writeContent()).terminate();
						output.endChild();
						break;
					case CONTENT:
						bufferedTarget.pipe(output.writeContent()).terminate();
					}
			}
		}
		nodeStack.pop();
	}

	public <U> BufferingDataTarget unbindDataNode(DataNode.Effective<U> node,
			BufferingDataTarget target) {
		for (U data : getData(node)) {
			BufferingDataTarget previousDataTarget = dataTarget;
			dataTarget = target;

			if (node.isExtensible() != null && node.isExtensible()) {
				List<DataNode.Effective<? extends U>> nodes = this.schemaBinderImpl.registeredTypes
						.getMatchingTypes(node, data.getClass()).stream()
						.map(type -> new DataNodeWrapper<>(type.effective(), node))
						.collect(Collectors.toCollection(ArrayList::new));

				if (node.isAbstract() == null || !node.isAbstract())
					nodes.add(node);

				if (nodes.isEmpty())
					throw new SchemaException(
							"Unable to find concrete type to satisfy data node '"
									+ node.getName() + "' with type '"
									+ node.effective().type().getName() + "' for object '" + data
									+ "' to be unbound.");

				tryUnbindingForEach(
						nodes,
						n -> processBindingChildren(n, unbindData(node, data)),
						l -> new MultiException("Unable to unbind data node '"
								+ node.getName()
								+ "' with type candidates '"
								+ nodes.stream().map(m -> m.source().getName().toString())
										.collect(Collectors.joining(", ")) + "' for object '"
								+ data + "' to be unbound.", l));
			} else {
				processBindingChildren(node, unbindData(node, data));
			}

			dataTarget = previousDataTarget;
		}

		return target;
	}

	@Override
	public void accept(InputSequenceNode.Effective node) {
		nodeStack.push(node);
		processChildren(node);
		nodeStack.pop();
	}

	@Override
	public void accept(SequenceNode.Effective node) {
		nodeStack.push(node);
		processChildren(node);
		nodeStack.pop();
	}

	@Override
	public void accept(ChoiceNode.Effective node) {
		nodeStack.push(node);
		nodeStack.pop();
	}

	@SuppressWarnings("unchecked")
	private <U> U provide(Class<U> clazz) {
		if (clazz.equals(DataTarget.class))
			return (U) dataTarget;
		if (clazz.equals(DereferenceTarget.class))
			return (U) dereferenceTarget;
		if (clazz.equals(IncludeTarget.class))
			return (U) includeTarget;
		if (clazz.equals(ImportDereferenceTarget.class))
			return (U) importTarget;

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

	private <U> void unbindElement(ElementNode.Effective<U> node, U data) {
		if (node.isExtensible() != null && node.isExtensible()) {
			List<ElementNode.Effective<? extends U>> nodes = this.schemaBinderImpl.registeredModels
					.getMatchingModels(node, data.getClass()).stream()
					.map(n -> new ElementNodeWrapper<>(n.effective(), node))
					.collect(Collectors.toCollection(ArrayList::new));

			if (node.isAbstract() == null || !node.isAbstract())
				nodes.add(node);

			if (nodes.isEmpty())
				throw new SchemaException("Unable to find model to satisfy element '"
						+ node.getName()
						+ "' with model '"
						+ node.effective().baseModel().stream()
								.map(m -> m.source().getName().toString())
								.collect(Collectors.joining(", ")) + "' for object '" + data
						+ "' to be unbound.");

			tryUnbindingForEach(
					nodes,
					n -> {
						unbindModel(n, data);
						bindings.add(n, data);
					},
					l -> new MultiException("Unable to unbind element '"
							+ node.getName()
							+ "' with model candidates '"
							+ nodes.stream().map(m -> m.source().getName().toString())
									.collect(Collectors.joining(", ")) + "' for object '" + data
							+ "' to be unbound.", l));
		} else {
			unbindModel(node, data);
			bindings.add(node, data);
		}
	}

	private <I extends ChildNode.Effective<?, ?>> void tryUnbindingForEach(
			List<I> unbindingItems, Consumer<I> unbindingMethod,
			Function<List<SchemaException>, MultiException> onFailure) {
		if (unbindingItems.isEmpty())
			throw new IllegalArgumentException(
					"Must supply items for unbinding attempt.");

		List<SchemaException> failures = new ArrayList<>();
		Deque<SchemaNode<?, ?>> nodeStack = new ArrayDeque<>(this.nodeStack);
		Deque<Object> bindingStack = new ArrayDeque<>(this.bindingStack);

		BufferingDataTarget dataTarget = null;
		StructuredDataTarget output = null;
		for (I item : unbindingItems) {
			// mark output! (by redirecting to a new buffer)
			if (this.dataTarget != null) {
				dataTarget = this.dataTarget;
				this.dataTarget = new BufferingDataTarget();
			}
			output = this.output;
			this.output = new BufferingStructuredDataTarget();

			try {
				unbindingMethod.accept(item);
				failures.clear();
				break;
			} catch (SchemaException e) {
				failures.add(e);

				// reset output to mark! (by discarding buffer)
				this.dataTarget = dataTarget;
				this.output = output;
				this.nodeStack.clear();
				this.nodeStack.addAll(nodeStack);
				this.bindingStack.clear();
				this.bindingStack.addAll(bindingStack);
			}
		}

		if (failures.isEmpty()) {
			// remove mark! (by flushing buffer into output)
			if (dataTarget != null)
				this.dataTarget = this.dataTarget.buffer().pipe(dataTarget);
			this.output = ((BufferingStructuredDataTarget) this.output).buffer()
					.pipeNextChild(output);

			this.nodeStack = nodeStack;
			this.bindingStack = bindingStack;
		} else
			throw onFailure.apply(failures);
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

		Object parent = bindingStack.peek();

		if (node.getDataClass() == null)
			throw new SchemaException("Cannot unbind node '" + node.getName()
					+ "' with no data class.");

		if (node.isOutMethodIterable() != null && node.isOutMethodIterable()) {
			Iterable<U> iterable = null;
			if (node.getOutMethodName() != null
					&& node.getOutMethodName().equals("this"))
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

		if (node.occurances() != null
				&& !node.occurances().contains(itemList.size()))
			throw new SchemaException("Output list '" + itemList
					+ "' contains too many items to be unbound by node '" + node + "'.");

		return itemList;
	}

	private Object invokeMethod(Method method, Object receiver,
			Object... parameters) {
		try {
			return method.invoke(receiver, parameters);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | SecurityException | NullPointerException e) {
			throw new SchemaException("Cannot invoke method '" + method + "' on '"
					+ receiver + "' at node '" + getNodeStackString() + "'.", e);
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
					parameters.add(parameter.providedValue() == null ? null : parameter
							.providedValue().get(0));
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
