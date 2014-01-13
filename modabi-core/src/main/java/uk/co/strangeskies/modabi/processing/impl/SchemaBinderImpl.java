package uk.co.strangeskies.modabi.processing.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.gears.utilities.collections.HashSetMultiHashMap;
import uk.co.strangeskies.gears.utilities.collections.SetMultiMap;
import uk.co.strangeskies.gears.utilities.functions.collections.ListTransformationFunction;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.data.DataInputBuffer;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.data.StructuredDataInput;
import uk.co.strangeskies.modabi.data.StructuredDataOutput;
import uk.co.strangeskies.modabi.data.impl.DataInputBufferImpl;
import uk.co.strangeskies.modabi.model.Binding;
import uk.co.strangeskies.modabi.model.BranchingNode;
import uk.co.strangeskies.modabi.model.ChoiceNode;
import uk.co.strangeskies.modabi.model.ContentNode;
import uk.co.strangeskies.modabi.model.ElementNode;
import uk.co.strangeskies.modabi.model.InputNode;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.PropertyNode;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.SequenceNode;
import uk.co.strangeskies.modabi.model.SimpleElementNode;
import uk.co.strangeskies.modabi.namespace.Namespace;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.processing.SchemaBinder;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class SchemaBinderImpl implements SchemaBinder {
	private class SchemaSavingContext<T> implements SchemaProcessingContext {
		private final T data;
		private final Model<T> model;
		private final StructuredDataOutput output;

		private final Deque<Object> bindingStack;

		public SchemaSavingContext(Model<T> model, StructuredDataOutput output,
				T data) {
			bindingStack = new ArrayDeque<>();

			this.data = data;
			this.model = model;
			this.output = output;
		}

		protected void save() {
			unbind(model, data);
		}

		protected void processChildren(BranchingNode node) {
			for (SchemaNode child : node.getChildren()) {
				child.process(this);
			}
		}

		@Override
		public <U> void accept(ContentNode<U> node) {
			output.content().string(node.getId()).end();
		}

		@Override
		public <U> void accept(PropertyNode<U> node) {
			System.out.println(node.getId() + ": ");
			output.content().string(node.getId()).end();
		}

		@Override
		public void accept(ChoiceNode node) {
			processChildren(node);
		}

		@Override
		public void accept(SequenceNode node) {
			processChildren(node);
		}

		public <U> void unbind(Model<U> node, U data) {
			output.childElement(node.getId());
			bindingStack.push(data);
			processChildren(node);
			bindingStack.pop();
			output.endElement();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <U> void accept(final ElementNode<U> node) {
			if (node.getDataClass() != null) {
				Object parent = bindingStack.peek();

				try {
					if (node.isOutMethodIterable()) {
						Iterable<Object> iterable = null;
						if (node.getOutMethod() == "this")
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
			}
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
			throw new NoSuchMethodException("For " + names + " in " + receiver);
		}

		private List<String> generateOutMethodNames(ElementNode<?> node) {
			List<String> names = new ArrayList<>();

			if (node.getOutMethod() != null) {
				names.add(node.getOutMethod());
			} else {
				names.add(node.getId());
				if (node.isOutMethodIterable()) {
					names.add(node.getId() + "s");
					names.add(node.getId() + "List");
					names.add(node.getId() + "Set");
					names.add(node.getId() + "Array");
				}
				for (String name : new ArrayList<>(names)) {
					names.add("get" + capitalize(name));
				}
			}

			return names;
		}

		private String capitalize(String string) {
			return string.substring(0, 1).toUpperCase() + string.substring(1);
		}

		@Override
		public <U> void accept(SimpleElementNode<U> node) {
			// TODO Auto-generated method stub

		}
	}

	private class SchemaLoadingContext<T> implements SchemaProcessingContext {
		private final Model<T> model;
		private final DataInputBuffer input;

		private final Deque<Object> bindingStack;

		public SchemaLoadingContext(Model<T> model, StructuredDataInput input) {
			bindingStack = new ArrayDeque<>();

			this.model = model;
			this.input = new DataInputBufferImpl(input);
		}

		protected Binding<T> load() {
			return new Binding<T>(model, bind(model));
		}

		@Override
		public <U> void accept(ContentNode<U> node) {
			invokeInMethod(node, (Object) input.getData(node.getType()));
		}

		@Override
		public <U> void accept(PropertyNode<U> node) {
			invokeInMethod(node,
					(Object) input.getProperty(node.getId(), node.getType()));
		}

		@Override
		public void accept(ChoiceNode node) {

		}

		@Override
		public void accept(SequenceNode node) {
			processChildren(node);
		}

		public <U> U bind(Model<U> node) {
			String name = input.nextChild();
			String namespace = input.getProperty("xmlns", null);

			bindingStack.push(provideInstance(node.getBuilderClass()));
			processChildren(node);
			@SuppressWarnings("unchecked")
			U boundObject = (U) bindingStack.pop();
			return boundObject;
		}

		@Override
		public <U> void accept(ElementNode<U> node) {
			invokeInMethod(node, (Object) bind(node));
		}

		private void invokeInMethod(InputNode node, Object... parameters) {
			try {
				Object object = bindingStack
						.peek()
						.getClass()
						.getMethod(
								node.getInMethod(),
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
		public <U> void accept(SimpleElementNode<U> node) {
			// TODO Auto-generated method stub

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
	public Binding<?> processInput(StructuredDataInput input) {
		Model<?> model = null;
		// input.peekNext(model);
		return new SchemaLoadingContext<>(model, input).load();
	}

	@Override
	public <T> T processInput(Model<T> model, StructuredDataInput input) {
		return new SchemaLoadingContext<>(model, input).load().getData();
	}

	@Override
	public <T> void processOutput(Model<T> model, StructuredDataOutput output,
			T data) {
		new SchemaSavingContext<>(model, output, data).save();
	}
}
