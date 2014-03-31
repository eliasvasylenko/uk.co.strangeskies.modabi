package uk.co.strangeskies.modabi.processing.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;

import uk.co.strangeskies.gears.utilities.collection.HashSetMultiHashMap;
import uk.co.strangeskies.gears.utilities.collection.SetMultiMap;
import uk.co.strangeskies.gears.utilities.function.collection.ListTransformationFunction;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.data.DataInputBuffer;
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.data.StructuredDataInput;
import uk.co.strangeskies.modabi.data.StructuredDataOutput;
import uk.co.strangeskies.modabi.data.TerminatingDataSink;
import uk.co.strangeskies.modabi.data.impl.DataInputBufferImpl;
import uk.co.strangeskies.modabi.impl.BaseSchemaImpl;
import uk.co.strangeskies.modabi.impl.MetaSchemaImpl;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode.Format;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.InputNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;
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

		private final Deque<List<ChildNode>> elementListStack;
		private boolean processElement;

		private TerminatingDataSink sink;

		public SchemaSavingContext(Model<T> model, StructuredDataOutput output,
				T data) {
			bindingStack = new ArrayDeque<>();
			elementListStack = new ArrayDeque<>();

			this.data = data;
			this.model = model;
			this.output = output;
		}

		protected void save() {
			accept(model.effectiveModel());
		}

		@Override
		public <U> void accept(AbstractModel<U> node) {
			unbindElement(node, data);
		}

		protected void processChildren(SchemaNode node) {
			List<ChildNode> elementList = new ArrayList<>();
			elementListStack.add(elementList);

			for (ChildNode child : node.getChildren()) {
				child.process(this);
			}

			for (ChildNode element : elementListStack.pop()) {
				processElement = true;
				element.process(this);
			}
		}

		@Override
		public <U> void accept(DataNode<U> node) {
			U data = getData(node);

			if (data != null) {
				boolean dataNodeRoot = sink == null;

				if (dataNodeRoot)
					switch (node.format()) {
					case PROPERTY:
						sink = output.property(node.getId());
						break;
					case SIMPLE_ELEMENT:
						if (processElement) {
							processElement = false;
							output.childElement(node.getId());
						} else {
							elementListStack.peek().add(node);
							return;
						}
					case CONTENT:
						sink = output.content();
					}
				else {
					if (node.format() != Format.PROPERTY)
						throw new SchemaException();
				}

				// sink.string("" + data);
				unbind(node, data);

				if (dataNodeRoot) {
					sink.end();
					sink = null;

					if (node.format() == Format.SIMPLE_ELEMENT)
						output.endElement();
				}
			}
		}

		@SuppressWarnings("unchecked")
		public <U> U getData(final BindingChildNode<U> node) {
			if (node.getDataClass() != null) {
				Object parent = bindingStack.peek();

				try {
					return (U) node.getOutMethod().invoke(parent);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					throw new SchemaException(node.getId() + " @ " + parent.getClass(), e);
				}
			}
			return null;
		}

		@Override
		public void accept(ChoiceNode node) {
			// processChildren(node); TODO
		}

		@Override
		public void accept(SequenceNode node) {
			processChildren(node);
		}

		public <U> void unbindElement(BindingNode<? extends U> node, U data) {
			output.childElement(node.getId());
			unbind(node, data);
			output.endElement();
		}

		public <U> void unbind(BindingNode<? extends U> node, U data) {
			bindingStack.push(data);
			processChildren(node);
			bindingStack.pop();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <U> void accept(ElementNode<U> node2) {
			BindingChildNode<? extends U> node = node2;
			if (node2.isAbstract() != null && node2.isAbstract())
				node = registeredModels.getMatchingModel(node2, data.getClass())
						.effectiveModel();

			if (processElement) {
				processElement = false;
				Object parent = bindingStack.peek();

				if (node.getDataClass() == null)
					throw new SchemaException(node.getId());

				try {
					if (node.isOutMethodIterable() != null && node.isOutMethodIterable()) {
						Iterable<Object> iterable = null;
						if (node.getOutMethodName() == "this")
							iterable = (Iterable<Object>) parent;
						else {
							iterable = (Iterable<Object>) node.getOutMethod().invoke(parent);
						}
						for (Object child : iterable)
							unbindElement(node, (U) child);
					} else {
						unbindElement(node, (U) node.getOutMethod().invoke(parent));
					}
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
				}
			} else
				elementListStack.peek().add(node);
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
			return new Binding<T>(model, bind(model.effectiveModel()));
		}

		@Override
		public void accept(ChoiceNode node) {
		}

		@Override
		public void accept(SequenceNode node) {
			processChildren(node);
		}

		public <U> U bind(AbstractModel<U> node) {
			String name = input.nextChild();
			String namespace = input.getProperty("xmlns", null);

			bindingStack.push(provideInstance(node.getBindingClass()));
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

		protected void processChildren(SchemaNode node) {
			for (ChildNode child : node.getChildren()) {
				child.process(this);
			}
		}

		@Override
		public <U> void accept(AbstractModel<U> node) {
			// TODO Auto-generated method stub

		}

		@Override
		public <U> void accept(DataNode<U> node) {
			invokeInMethod(node, (Object) input.getData(node.type()));
		}
	}

	private final BaseSchema baseSchema;
	private final MetaSchema metaSchema;

	private final SetMultiMap<Schema, QualifiedName> unmetDependencies;
	// private final Map<QualifiedName, MockedSchema> mockedDependencies;
	private final Models registeredModels;
	private final DataTypes registeredTypes;
	private final Schemata registeredSchema;

	public SchemaBinderImpl(SchemaBuilder schemaBuilder,
			ModelBuilder modelBuilder, DataTypeBuilder dataTypeBuilder) {
		unmetDependencies = new HashSetMultiHashMap<>();
		// mockedDependencies = new HashMap<>();

		baseSchema = new BaseSchemaImpl(schemaBuilder, modelBuilder,
				dataTypeBuilder);
		metaSchema = new MetaSchemaImpl(schemaBuilder, modelBuilder,
				dataTypeBuilder, baseSchema);

		registeredSchema = new Schemata();
		Namespace namespace = metaSchema.getQualifiedName().getNamespace();
		registeredModels = new Models(namespace);
		registeredTypes = new DataTypes(namespace);

		registerSchema(baseSchema);
		registerSchema(metaSchema);
	}

	private void registerSchema(Schema schema) {
		registeredSchema.add(schema);

		for (Model<?> model : schema.getModels().getMap().values())
			registerModel(model, schema.getQualifiedName().getNamespace());

		for (DataType<?> type : schema.getDataTypes().getMap().values())
			registerDataType(type, schema.getQualifiedName().getNamespace());
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
	public MetaSchema getMetaSchema() {
		return metaSchema;
	}

	@Override
	public BaseSchema getBaseSchema() {
		return baseSchema;
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

	public static Method findMethod(List<String> names, Class<?> receiver,
			Class<?> result, Class<?>... parameter) throws NoSuchMethodException {
		for (String methodName : names) {
			try {
				Method method = receiver.getMethod(methodName, parameter);
				if (method != null && result == null
						|| ClassUtils.isAssignable(method.getReturnType(), result, true))
					return method;
			} catch (NoSuchMethodException | SecurityException e) {
			}
		}
		throw new NoSuchMethodException("For "
				+ names
				+ " in "
				+ receiver
				+ " as [ "
				+ Arrays.asList(parameter).stream().map(p -> p.getName())
						.collect(Collectors.joining(", ")) + " ] -> " + result);
	}

	public static List<String> generateInMethodNames(BindingChildNode<?> node) {
		if (node.getInMethodName() != null)
			return Arrays.asList(node.getInMethodName());
		else
			return generateInMethodNames(node.getId());

	}

	public static List<String> generateInMethodNames(String propertyName) {
		List<String> names = new ArrayList<>();

		names.add(propertyName);
		for (String name : new String[] { propertyName, "" }) {
			names.add("set" + capitalize(name));
			names.add("from" + capitalize(name));
			names.add("parse" + capitalize(name));
			names.add("add" + capitalize(name));
			names.add("put" + capitalize(name));
		}

		return names;
	}

	public static List<String> generateOutMethodNames(BindingChildNode<?> node) {
		return generateOutMethodNames(node, node.getDataClass());
	}

	public static List<String> generateOutMethodNames(BindingChildNode<?> node,
			Class<?> resultClass) {

		List<String> names = new ArrayList<>();

		if (node.getOutMethodName() != null)
			names.add(node.getOutMethodName());
		else {
			names.add(node.getId());
			if (node.isOutMethodIterable() != null && node.isOutMethodIterable()) {
				names.add(node.getId() + "s");
				names.add(node.getId() + "List");
				names.add(node.getId() + "Set");
				names.add(node.getId() + "Collection");
				names.add(node.getId() + "Array");
			}
			if (resultClass != null
					&& (resultClass.equals(Boolean.class) || resultClass
							.equals(boolean.class)))
				names.add("is" + capitalize(node.getId()));
			for (String name : new ArrayList<>(names)) {
				names.add("get" + capitalize(name));
				names.add("to" + capitalize(name));
				names.add("compose" + capitalize(name));
				names.add("create" + capitalize(name));
			}
		}

		return names;
	}

	protected static String capitalize(String string) {
		return string == "" ? "" : string.substring(0, 1).toUpperCase()
				+ string.substring(1);
	}
}
