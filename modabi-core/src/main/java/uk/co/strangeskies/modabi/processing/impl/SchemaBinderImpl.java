package uk.co.strangeskies.modabi.processing.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
import uk.co.strangeskies.modabi.data.DataType;
import uk.co.strangeskies.modabi.data.DataTypeBuilder;
import uk.co.strangeskies.modabi.data.DataTypes;
import uk.co.strangeskies.modabi.data.impl.DataTypeBuilderImpl;
import uk.co.strangeskies.modabi.data.io.DataTarget;
import uk.co.strangeskies.modabi.data.io.TerminatingDataTarget;
import uk.co.strangeskies.modabi.data.io.structured.StructuredInput;
import uk.co.strangeskies.modabi.data.io.structured.StructuredOutput;
import uk.co.strangeskies.modabi.impl.BaseSchemaImpl;
import uk.co.strangeskies.modabi.impl.MetaSchemaImpl;
import uk.co.strangeskies.modabi.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.Models;
import uk.co.strangeskies.modabi.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.model.impl.ElementNodeWrapper;
import uk.co.strangeskies.modabi.model.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
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
		private class StackContext {
			private final Object binding;

			public StackContext(Object binding) {
				this.binding = binding;
			}
		}

		private final StructuredOutput output;

		private final Deque<StackContext> bindingStack;

		private TerminatingDataTarget sink;

		public SchemaSavingContext(Model<T> model, StructuredOutput output, T data) {
			bindingStack = new ArrayDeque<>();

			this.output = output;

			unbindModel(model.effectiveModel(), data);
		}

		@Override
		public <U> void accept(ElementNode<U> node) {
			for (Object child : unbindData(node, getData(node)))
				unbindElement(node, child);
		}

		@Override
		public <U> void accept(DataNode<U> node) {
			List<U> data = getData(node);

			if (!data.isEmpty()) {
				if (sink != null && node.format() != null)
					throw new SchemaException();

				switch (node.format()) {
				case PROPERTY:
					sink = output.property(node.getId());
					break;
				case SIMPLE_ELEMENT:
					output.childElement(node.getId());
				case CONTENT:
					sink = output.content();
				}

				for (Object item : unbindData(node, data))
					processBindingChildren(node, item);

				if (node.format() != null) {
					sink.terminate();
					sink = null;
					if (node.format() == Format.SIMPLE_ELEMENT)
						output.endElement();
				}
			}
		}

		@Override
		public void accept(SequenceNode node) {
			processChildren(node);
		}

		@Override
		public void accept(ChoiceNode node) {
			// processChildren(node); TODO
		}

		@SuppressWarnings("unchecked")
		private <U> U provide(Class<U> clazz) {
			if (clazz.equals(DataTarget.class))
				return (U) sink;

			return SchemaBinderImpl.this.provide(clazz);
		}

		private void processChildren(SchemaNode node) {
			for (ChildNode child : node.getChildren())
				child.process(this);
		}

		private void processBindingChildren(SchemaNode node, Object binding) {
			bindingStack.push(new StackContext(binding));

			processChildren(node);

			bindingStack.pop();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		// TODO should work without second type parameter & warning suppression
		private <U, V extends U> void unbindElement(ElementNode<V> node, U data) {
			if (node.isAbstract() != null && node.isAbstract())
				node = new ElementNodeWrapper(registeredModels
						.getMatchingModels(node, data.getClass()).get(0).effectiveModel(),
						node);
			unbindModel(node, data);
		}

		private <U> void unbindModel(AbstractModel<? extends U> node, U data) {
			output.childElement(node.getId());
			processBindingChildren(node, data);
			output.endElement();
		}

		@SuppressWarnings("unchecked")
		public <U> List<U> getData(BindingChildNode<U> node) {
			Object parent = bindingStack.peek().binding;

			if (node.getDataClass() == null)
				throw new SchemaException(node.getId());

			List<U> itemList;

			try {
				if (node.isOutMethodIterable() != null && node.isOutMethodIterable()) {
					Iterable<U> iterable = null;
					if (node.getOutMethodName() == "this")
						iterable = (Iterable<U>) parent;
					else
						iterable = (Iterable<U>) node.getOutMethod().invoke(parent);

					itemList = StreamSupport.stream(iterable.spliterator(), false)
							.filter(Objects::nonNull).collect(Collectors.toList());
				} else {
					U item = (U) node.getOutMethod().invoke(parent);
					if (item == null)
						itemList = new ArrayList<>();
					else
						itemList = Arrays.asList(item);
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new SchemaException(node.getId() + " @ " + parent.getClass(), e);
			}
			return itemList;
		}

		public <U> List<Object> unbindData(BindingChildNode<U> node, List<U> data) {
			Function<Object, Object> supplier = Function.identity();
			if (node.getUnbindingStrategy() != null) {
				switch (node.getUnbindingStrategy()) {
				case SIMPLE:
					break;
				case PROVIDED:
					supplier = u -> {
						try {
							Object o = provide(node.getUnbindingClass());
							if (o == null)
								throw new IllegalArgumentException(node.getUnbindingClass()
										.getName());
							node.getUnbindingMethod().invoke(o, u);
							return o;
						} catch (IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | SecurityException e) {
							throw new SchemaException(e);
						}
					};
					break;
				case CONSTRUCTOR:
					supplier = u -> {
						try {
							return node.getUnbindingClass().getConstructor(u.getClass())
									.newInstance(u);
						} catch (InstantiationException | IllegalAccessException
								| IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException e) {
							throw new SchemaException(e);
						}
					};
					break;
				case STATIC_FACTORY:
					supplier = u -> {
						try {
							return node.getUnbindingMethod().invoke(null, u);
						} catch (IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | SecurityException e) {
							throw new SchemaException(e);
						}
					};
					break;
				}
			}
			List<Object> itemList = data.stream().map(supplier::apply)
					.collect(Collectors.toList());

			return itemList;
		}
	}

	private static class SchemaLoadingContext<T> implements
			SchemaProcessingContext {
		private final Model<T> model;

		private final Deque<Object> bindingStack;

		public SchemaLoadingContext(Model<T> model, StructuredInput input) {
			bindingStack = new ArrayDeque<>();

			this.model = model;
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
			// String name = input.nextChild();
			// String namespace = input.getProperty("xmlns", null);

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
			for (ChildNode child : node.getChildren())
				child.process(this);
		}

		@Override
		public <U> void accept(DataNode<U> node) {
			// invokeInMethod(node, (Object) input.getData(node.type()));
		}
	}

	private final BaseSchema baseSchema;
	private final MetaSchema metaSchema;

	private final SetMultiMap<Schema, QualifiedName> unmetDependencies;
	// private final Map<QualifiedName, MockedSchema> mockedDependencies;
	private final Models registeredModels;
	private final DataTypes registeredTypes;
	private final Schemata registeredSchema;

	public SchemaBinderImpl() {
		this(new SchemaBuilderImpl(), new ModelBuilderImpl(),
				new DataTypeBuilderImpl());
	}

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
	public <T> T processInput(Model<T> model, StructuredInput input) {
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
	public Binding<?> processInput(StructuredInput input) {
		Model<?> model = null;
		// input.peekNext(model);
		return new SchemaLoadingContext<>(model, input).load();
	}

	@Override
	public <T> void processOutput(Model<T> model, StructuredOutput output, T data) {
		new SchemaSavingContext<>(model, output, data);
	}

	@Override
	public <T> void processOutput(StructuredOutput output, T data) {
		Model<T> model = null;
		// registeredModels.search(data.getClass());
		new SchemaSavingContext<>(model, output, data);
	}

	@Override
	public <T> void registerProvider(Class<T> providedClass, Supplier<T> provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerProvider(Function<Class<?>, ?> provider) {
		// TODO Auto-generated method stub

	}

	protected <T> T provide(Class<T> clazz) {
		return null;
	}
}
