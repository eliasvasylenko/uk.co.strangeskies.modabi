package uk.co.strangeskies.modabi.processing.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.collection.HashSetMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.SetMultiMap;
import uk.co.strangeskies.gears.utilities.function.collection.ListTransformationFunction;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.data.DataInputBuffer;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.data.StructuredDataInput;
import uk.co.strangeskies.modabi.data.StructuredDataOutput;
import uk.co.strangeskies.modabi.data.impl.DataInputBufferImpl;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Binding;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.nodes.BranchingNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.ContentNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.InputNode;
import uk.co.strangeskies.modabi.model.nodes.PropertyNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.model.nodes.SimpleElementNode;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.processing.SchemaBinder;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class SchemaBinderImpl implements SchemaBinder {
	private class SchemaSavingContext<T> implements SchemaProcessingContext<Void> {
		private final T data;
		private final Model<T> model;
		private final StructuredDataOutput output;

		private final Deque<Object> bindingStack;

		private final Deque<List<ElementNode<?>>> elementListStack;

		public SchemaSavingContext(Model<T> model, StructuredDataOutput output,
				T data) {
			bindingStack = new ArrayDeque<>();
			elementListStack = new ArrayDeque<>();

			this.data = data;
			this.model = model;
			this.output = output;
		}

		protected void save() {
			unbind(model.effectiveModel(), data);
		}

		protected void processChildren(BranchingNode node) {
			List<ElementNode<?>> elementList = new ArrayList<>();
			elementListStack.add(elementList);

			for (SchemaNode child : node.getChildren()) {
				child.process(this);
			}

			for (ElementNode<?> element : elementListStack.pop()) {
				process(element);
			}
		}

		@Override
		public <U> Void accept(ContentNode<U> node) {
			output.content().string(node.getId()).end();
			return null;
		}

		@Override
		public <U> Void accept(PropertyNode<U> node) {
			Object data = getData(node);
			if (data != null)
				output.property(node.getId()).string("" + data).end();
			return null;
		}

		@SuppressWarnings("unchecked")
		public <U> U getData(final DataNode<U> node) {
			if (node.getDataClass() != null) {
				Object parent = bindingStack.peek();

				try {
					return (U) getMethod(parent, node.getDataClass(),
							generateOutMethodNames(node)).invoke(parent);
				} catch (NoSuchMethodException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		public Void accept(ChoiceNode node) {
			// processChildren(node); TODO
			return null;
		}

		@Override
		public Void accept(SequenceNode node) {
			processChildren(node);
			return null;
		}

		public <U> void unbind(AbstractModel<? extends U> node, U data) {
			if (node.isAbstract() != null && node.isAbstract())
				node = registeredModels.getMatchingModel(node, data.getClass())
						.effectiveModel();

			output.childElement(node.getId());
			bindingStack.push(data);
			processChildren(node);
			bindingStack.pop();
			output.endElement();
		}

		@Override
		public <U> Void accept(final ElementNode<U> node) {
			elementListStack.peek().add(node);
			return null;
		}

		@SuppressWarnings("unchecked")
		public <U> Void process(final ElementNode<U> node) {
			Object parent = bindingStack.peek();

			if (node.getDataClass() == null)
				throw new SchemaException();

			try {
				if (node.isOutMethodIterable() != null && node.isOutMethodIterable()) {
					Iterable<Object> iterable = null;
					if (node.getOutMethodName() == "this")
						iterable = (Iterable<Object>) parent;
					else {
						iterable = (Iterable<Object>) getMethod(parent, Iterable.class,
								generateOutMethodNames(node)).invoke(parent);
					}
					for (Object child : iterable)
						unbind(node, (U) child);
				} else {
					unbind(
							node,
							(U) getMethod(parent, node.getDataClass(),
									generateOutMethodNames(node)).invoke(parent));
				}
			} catch (NoSuchMethodException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}

			return null;
		}

		private Method getMethod(Object receiver, Class<?> returns,
				List<String> names) throws NoSuchMethodException {
			for (String methodName : names) {
				try {
					Method method = receiver.getClass().getMethod(methodName);
					if (method != null && returns == null
							|| returns.isAssignableFrom(method.getReturnType()))
						return method;
				} catch (NoSuchMethodException | SecurityException e) {
				}
			}
			throw new NoSuchMethodException("For " + names + " in "
					+ receiver.getClass() + " @ " + receiver + " -> " + returns);
		}

		private List<String> generateOutMethodNames(DataNode<?> node) {
			List<String> names = new ArrayList<>();

			if (node.getOutMethodName() != null) {
				names.add(node.getOutMethodName());
			} else {
				names.add(node.getId());
				Boolean iterable = node.isOutMethodIterable();
				if (iterable != null && iterable) {
					names.add(node.getId() + "s");
					names.add(node.getId() + "List");
					names.add(node.getId() + "Set");
					names.add(node.getId() + "Array");
				}
				if (node.getDataClass().equals(Boolean.class))
					names.add("is" + capitalize(node.getId()));
				for (String name : new ArrayList<>(names))
					names.add("get" + capitalize(name));
			}

			return names;
		}

		private String capitalize(String string) {
			return string.substring(0, 1).toUpperCase() + string.substring(1);
		}

		@Override
		public <U> Void accept(SimpleElementNode<U> node) {
			// TODO Auto-generated method stub

			return null;
		}
	}

	private class SchemaLoadingContext<T> implements
			SchemaProcessingContext<Void> {
		private final Model<T> model;
		private final DataInputBuffer input;

		private final Deque<Object> bindingStack;

		public SchemaLoadingContext(Model<T> model, StructuredDataInput input) {
			bindingStack = new ArrayDeque<>();

			this.model = model;
			this.input = new DataInputBufferImpl(input);
		}

		protected Binding<T> load() {
			return new Binding<T>(model, bind(model.effectiveModel()));
		}

		@Override
		public <U> Void accept(ContentNode<U> node) {
			invokeInMethod(node, (Object) input.getData(node.getType()));
			return null;
		}

		@Override
		public <U> Void accept(PropertyNode<U> node) {
			invokeInMethod(node,
					(Object) input.getProperty(node.getId(), node.getType()));
			return null;
		}

		@Override
		public Void accept(ChoiceNode node) {
			return null;
		}

		@Override
		public Void accept(SequenceNode node) {
			processChildren(node);
			return null;
		}

		public <U> U bind(AbstractModel<U> node) {
			String name = input.nextChild();
			String namespace = input.getProperty("xmlns", null);

			bindingStack.push(provideInstance(node.getBuilderClass()));
			processChildren(node);
			@SuppressWarnings("unchecked")
			U boundObject = (U) bindingStack.pop();
			return boundObject;
		}

		@Override
		public <U> Void accept(ElementNode<U> node) {
			invokeInMethod(node, (Object) bind(node));
			return null;
		}

		private void invokeInMethod(InputNode node, Object... parameters) {
			try {
				Object object = bindingStack
						.peek()
						.getClass()
						.getMethod(
								node.getInMethodName(),
								ListTransformationFunction.<Object, Class<?>> apply(parameters,
										new Function<Object, Class<?>>() {
											@Override
											public Class<?> apply(Object input) {
												return input.getClass();
											}
										})).invoke(bindingStack.peek(), parameters);
				if (node.isInMethodChained()) {
					bindingStack.pop();
					bindingStack.push(object);
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				e.printStackTrace();
			}
		}

		private <U> U provideInstance(Class<U> builderClass) {
			return null;
		}

		protected void processChildren(BranchingNode node) {
			for (SchemaNode child : node.getChildren()) {
				child.process(this);
			}
		}

		@Override
		public <U> Void accept(SimpleElementNode<U> node) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private MetaSchema metaSchema;

	private final SetMultiMap<Schema, QualifiedName> unmetDependencies;
	// private final Map<QualifiedName, MockedSchema> mockedDependencies;
	private Models registeredModels;
	private DataTypes registeredTypes;
	private final Schemata registeredSchema;

	public SchemaBinderImpl() {
		unmetDependencies = new HashSetMultiHashMap<>();
		// mockedDependencies = new HashMap<>();

		registeredSchema = new Schemata();
	}

	public SchemaBinderImpl(MetaSchema metaSchema) {
		this();

		setMetaSchema(metaSchema);
	}

	public void setMetaSchema(MetaSchema metaSchema) {
		this.metaSchema = metaSchema;

		Namespace namespace = metaSchema.getQualifiedName().getNamespace();
		registeredModels = new Models(namespace);
		registeredTypes = new DataTypes(namespace);

		registerSchema(metaSchema);
		for (Schema schema : registeredSchema.getMap().values()) {
			registerSchema(schema);
		}
	}

	private void registerSchema(Schema schema) {
		registeredSchema.add(schema);
		for (Model<?> model : schema.getModels().getMap().values()) {
			registerModel(model, schema.getQualifiedName().getNamespace());
		}
		for (DataType<?> type : schema.getDataTypes().getMap().values()) {
			registerDataType(type, schema.getQualifiedName().getNamespace());
		}
	}

	private void registerModel(Model<?> model, Namespace namespace) {
		registeredModels.add(model, namespace);
	}

	private void registerDataType(DataType<?> type, Namespace namespace) {
		registeredTypes.add(type, namespace);
	}

	@Override
	public <T> T processInput(Model<T> model, StructuredDataInput input) {
		return new SchemaLoadingContext<>(model, input).load().getData();
	}

	@Override
	public Binding<?> processInput(StructuredDataInput input) {
		Model<?> model = null;
		// input.peekNext(model);
		return new SchemaLoadingContext<>(model, input).load();
	}

	@Override
	public <T> void processOutput(Model<T> model, StructuredDataOutput output,
			T data) {
		new SchemaSavingContext<>(model, output, data).save();
	}

	@Override
	public <T> void processOutput(StructuredDataOutput output, T data) {
		Model<T> model = null;
		// registeredModels.search(data.getClass());
		new SchemaSavingContext<>(model, output, data).save();
	}
}
